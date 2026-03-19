package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.PointsHistory;
import com.petcloud.user.domain.entity.TaskDefinition;
import com.petcloud.user.domain.entity.UserPoints;
import com.petcloud.user.domain.entity.UserTask;
import com.petcloud.user.domain.service.TaskService;
import com.petcloud.user.domain.vo.PointsHistoryVO;
import com.petcloud.user.domain.vo.TaskVO;
import com.petcloud.user.domain.vo.UserPointsVO;
import com.petcloud.user.infrastructure.persistence.mapper.PointsHistoryMapper;
import com.petcloud.user.infrastructure.persistence.mapper.TaskDefinitionMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserPointsMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskDefinitionMapper taskDefinitionMapper;
    private final UserTaskMapper userTaskMapper;
    private final UserPointsMapper userPointsMapper;
    private final PointsHistoryMapper pointsHistoryMapper;

    @Override
    public List<TaskVO> getTodayTasks(Long userId) {
        LocalDate today = LocalDate.now();

        // 获取所有启用的每日任务
        LambdaQueryWrapper<TaskDefinition> taskQuery = new LambdaQueryWrapper<>();
        taskQuery.eq(TaskDefinition::getStatus, TaskDefinition.Status.ENABLED.getCode())
                .eq(TaskDefinition::getTaskType, TaskDefinition.TaskType.DAILY.getCode())
                .orderByAsc(TaskDefinition::getSortOrder);
        List<TaskDefinition> taskDefinitions = taskDefinitionMapper.selectList(taskQuery);

        if (taskDefinitions.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取用户今日已完成的任务
        LambdaQueryWrapper<UserTask> userTaskQuery = new LambdaQueryWrapper<>();
        userTaskQuery.eq(UserTask::getUserId, userId)
                .eq(UserTask::getTaskDate, today)
                .eq(UserTask::getStatus, UserTask.Status.COMPLETED.getCode());
        List<UserTask> completedTasks = userTaskMapper.selectList(userTaskQuery);

        // 转换为已完成的任务ID集合
        Set<Long> completedTaskIds = completedTasks.stream()
                .map(UserTask::getTaskId)
                .collect(Collectors.toSet());

        // 构建返回结果
        return taskDefinitions.stream()
                .map(task -> convertToVO(task, completedTaskIds.contains(task.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer completeTask(Long userId, Long taskId) {
        LocalDate today = LocalDate.now();

        // 查询任务定义
        TaskDefinition taskDef = taskDefinitionMapper.selectById(taskId);
        if (taskDef == null || !TaskDefinition.Status.ENABLED.getCode().equals(taskDef.getStatus())) {
            throw new BusinessException(RespType.TASK_NOT_FOUND);
        }

        // 检查是否已完成
        LambdaQueryWrapper<UserTask> checkQuery = new LambdaQueryWrapper<>();
        checkQuery.eq(UserTask::getUserId, userId)
                .eq(UserTask::getTaskId, taskId)
                .eq(UserTask::getTaskDate, today);
        UserTask existingTask = userTaskMapper.selectOne(checkQuery);

        if (existingTask != null && UserTask.Status.COMPLETED.getCode().equals(existingTask.getStatus())) {
            throw new BusinessException(RespType.TASK_ALREADY_COMPLETED);
        }

        // 创建或更新用户任务记录
        UserTask userTask;
        if (existingTask != null) {
            userTask = existingTask;
            userTask.setStatus(UserTask.Status.COMPLETED.getCode());
            userTask.setCompleteTime(new Date());
            userTask.setPoints(taskDef.getPoints());
            userTaskMapper.updateById(userTask);
        } else {
            userTask = new UserTask();
            userTask.setUserId(userId);
            userTask.setTaskId(taskId);
            userTask.setTaskCode(taskDef.getTaskCode());
            userTask.setPoints(taskDef.getPoints());
            userTask.setStatus(UserTask.Status.COMPLETED.getCode());
            userTask.setCompleteTime(new Date());
            userTask.setTaskDate(today);
            userTaskMapper.insert(userTask);
        }

        // 增加用户积分
        addPoints(userId, taskDef.getPoints(), PointsHistory.Type.TASK_REWARD, taskId, "完成任务: " + taskDef.getTaskName());

        log.info("用户 {} 完成任务 {}, 获得 {} 积分", userId, taskDef.getTaskName(), taskDef.getPoints());
        return taskDef.getPoints();
    }

    @Override
    public UserPointsVO getUserPoints(Long userId) {
        UserPoints userPoints = getOrCreateUserPoints(userId);
        return UserPointsVO.builder()
                .total(userPoints.getTotalPoints())
                .used(userPoints.getUsedPoints())
                .available(userPoints.getTotalPoints() - userPoints.getUsedPoints())
                .level(userPoints.getLevel())
                .build();
    }

    @Override
    public List<PointsHistoryVO> getPointsHistory(Long userId, Integer page, Integer size) {
        // 使用 MyBatis-Plus 分页，避免 SQL 拼接
        Page<PointsHistory> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PointsHistory> query = new LambdaQueryWrapper<>();
        query.eq(PointsHistory::getUserId, userId)
                .orderByDesc(PointsHistory::getCreateTime);

        Page<PointsHistory> resultPage = pointsHistoryMapper.selectPage(pageParam, query);
        return resultPage.getRecords().stream()
                .map(this::convertToHistoryVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTaskByCode(Long userId, String taskCode) {
        // 根据编码查找任务
        LambdaQueryWrapper<TaskDefinition> query = new LambdaQueryWrapper<>();
        query.eq(TaskDefinition::getTaskCode, taskCode)
                .eq(TaskDefinition::getStatus, TaskDefinition.Status.ENABLED.getCode());
        TaskDefinition taskDef = taskDefinitionMapper.selectOne(query);

        if (taskDef == null) {
            log.warn("任务编码 {} 不存在或已禁用", taskCode);
            return;
        }

        try {
            completeTask(userId, taskDef.getId());
        } catch (BusinessException e) {
            // 任务已完成，忽略异常
            log.debug("任务 {} 已经完成", taskCode);
        }
    }

    /**
     * 获取或创建用户积分记录
     */
    private UserPoints getOrCreateUserPoints(Long userId) {
        LambdaQueryWrapper<UserPoints> query = new LambdaQueryWrapper<>();
        query.eq(UserPoints::getUserId, userId);
        UserPoints userPoints = userPointsMapper.selectOne(query);

        if (userPoints == null) {
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setUsedPoints(0);
            userPoints.setLevel(1);
            userPointsMapper.insert(userPoints);
        }
        return userPoints;
    }

    /**
     * 增加用户积分
     */
    private void addPoints(Long userId, Integer points, PointsHistory.Type type, Long relatedId, String remark) {
        // 更新用户积分
        UserPoints userPoints = getOrCreateUserPoints(userId);
        userPoints.setTotalPoints(userPoints.getTotalPoints() + points);

        // 根据积分计算等级（每1000分升一级）
        int newLevel = userPoints.getTotalPoints() / 1000 + 1;
        userPoints.setLevel(newLevel);

        userPointsMapper.updateById(userPoints);

        // 记录积分流水
        PointsHistory history = new PointsHistory();
        history.setUserId(userId);
        history.setPoints(points);
        history.setBalance(userPoints.getTotalPoints() - userPoints.getUsedPoints());
        history.setType(type.getCode());
        history.setRelatedId(relatedId);
        history.setRemark(remark);
        pointsHistoryMapper.insert(history);
    }

    /**
     * 转换为VO
     */
    private TaskVO convertToVO(TaskDefinition task, boolean completed) {
        return TaskVO.builder()
                .id(task.getId())
                .code(task.getTaskCode())
                .name(task.getTaskName())
                .desc(task.getTaskDesc())
                .icon(task.getTaskIcon())
                .points(task.getPoints())
                .completed(completed)
                .type(task.getTaskType())
                .build();
    }

    /**
     * 转换为积分历史VO
     */
    private PointsHistoryVO convertToHistoryVO(PointsHistory history) {
        return PointsHistoryVO.builder()
                .id(history.getId())
                .points(history.getPoints())
                .balance(history.getBalance())
                .type(history.getType())
                .typeDesc(getTypeDesc(history.getType()))
                .remark(history.getRemark())
                .createTime(history.getCreateTime())
                .build();
    }

    /**
     * 获取类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) {
            return "";
        }
        return Arrays.stream(TaskDefinition.TaskType.values())
                .filter(t -> t.getCode().equals(type))
                .findFirst()
                .map(TaskDefinition.TaskType::getDesc)
                .orElse("");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetTodayTasks(Long userId) {
        LocalDate today = LocalDate.now();

        // 删除今日的任务完成记录
        LambdaQueryWrapper<UserTask> query = new LambdaQueryWrapper<>();
        query.eq(UserTask::getUserId, userId)
                .eq(UserTask::getTaskDate, today);
        userTaskMapper.delete(query);

        log.info("重置用户 {} 今日任务", userId);
    }
}
