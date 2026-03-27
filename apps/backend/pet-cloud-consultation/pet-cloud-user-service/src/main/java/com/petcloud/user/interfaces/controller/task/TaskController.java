package com.petcloud.user.interfaces.controller.task;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.TaskService;
import com.petcloud.user.domain.vo.PointsHistoryVO;
import com.petcloud.user.domain.vo.TaskVO;
import com.petcloud.user.domain.vo.UserPointsVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserContextHolderWeb userContextHolderWeb;

    /**
     * 获取今日任务列表
     */
    @GetMapping("/today")
    public Response<List<TaskVO>> getTodayTasks(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取今日任务列表，userId: {}", userId);
        List<TaskVO> tasks = taskService.getTodayTasks(userId);
        return Response.succeed(tasks);
    }

    /**
     * 完成任务
     */
    @PostMapping("/{taskId}/complete")
    public Response<Integer> completeTask(HttpServletRequest request,
                                          @PathVariable Long taskId) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("完成任务，userId: {}, taskId: {}", userId, taskId);
        Integer points = taskService.completeTask(userId, taskId);
        return Response.succeed(points);
    }

    /**
     * 获取用户积分
     */
    @GetMapping("/points")
    public Response<UserPointsVO> getUserPoints(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取用户积分，userId: {}", userId);
        UserPointsVO points = taskService.getUserPoints(userId);
        return Response.succeed(points);
    }

    /**
     * 获取积分历史
     */
    @GetMapping("/history")
    public Response<List<PointsHistoryVO>> getPointsHistory(HttpServletRequest request,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取积分历史，userId: {}, page: {}, size: {}", userId, page, size);
        List<PointsHistoryVO> history = taskService.getPointsHistory(userId, page, size);
        return Response.succeed(history);
    }

    /**
     * 重置今日任务（测试用）
     */
    @PostMapping("/reset")
    public Response<Void> resetTodayTasks(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("重置今日任务，userId: {}", userId);
        taskService.resetTodayTasks(userId);
        return Response.succeed();
    }
}
