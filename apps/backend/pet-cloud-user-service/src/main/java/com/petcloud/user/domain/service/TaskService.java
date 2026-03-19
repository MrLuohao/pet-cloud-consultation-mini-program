package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.PointsHistoryVO;
import com.petcloud.user.domain.vo.TaskVO;
import com.petcloud.user.domain.vo.UserPointsVO;

import java.util.List;

/**
 * 任务服务接口
 *
 * @author luohao
 */
public interface TaskService {

    /**
     * 获取今日任务列表
     *
     * @param userId 用户ID
     * @return 今日任务列表（含完成状态）
     */
    List<TaskVO> getTodayTasks(Long userId);

    /**
     * 完成任务
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 获得的积分
     */
    Integer completeTask(Long userId, Long taskId);

    /**
     * 获取用户积分信息
     *
     * @param userId 用户ID
     * @return 积分信息
     */
    UserPointsVO getUserPoints(Long userId);

    /**
     * 获取积分历史记录
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 积分历史列表
     */
    List<PointsHistoryVO> getPointsHistory(Long userId, Integer page, Integer size);

    /**
     * 检查并完成指定编码的任务
     * 用于其他模块触发任务完成
     *
     * @param userId   用户ID
     * @param taskCode 任务编码
     */
    void completeTaskByCode(Long userId, String taskCode);

    /**
     * 重置今日任务（测试用）
     *
     * @param userId 用户ID
     */
    void resetTodayTasks(Long userId);
}
