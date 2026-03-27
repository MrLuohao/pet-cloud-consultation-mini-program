package com.petcloud.user.interfaces.controller.content;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.CourseProgressDTO;
import com.petcloud.user.domain.dto.CourseReviewDTO;
import com.petcloud.user.domain.service.CourseProgressService;
import com.petcloud.user.domain.service.CourseReviewService;
import com.petcloud.user.domain.service.CourseService;
import com.petcloud.user.domain.vo.CourseProgressVO;
import com.petcloud.user.domain.vo.CourseReviewVO;
import com.petcloud.user.domain.vo.CourseVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseProgressService courseProgressService;
    private final CourseReviewService courseReviewService;
    private final UserContextHolderWeb userContextHolderWeb;

    @GetMapping("/list")
    public Response<List<CourseVO>> getCourseList() {
        return Response.succeed(courseService.getCourseList());
    }

    @GetMapping("/{id}")
    public Response<CourseVO> getCourseDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = null;
        try {
            userId = userContextHolderWeb.getCurrentUserId(request);
        } catch (Exception e) {
            // 未登录用户不加载进度
        }
        return Response.succeed(courseService.getCourseDetail(id, userId));
    }

    @GetMapping("/{id}/progress")
    public Response<CourseProgressVO> getProgress(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(courseProgressService.getProgress(userId, id));
    }

    @PutMapping("/{id}/progress")
    public Response<Void> updateProgress(HttpServletRequest request,
                                         @PathVariable Long id,
                                         @RequestBody CourseProgressDTO dto) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        courseProgressService.updateProgress(userId, id, dto);
        return Response.succeed();
    }

    @PostMapping("/{id}/review")
    public Response<Void> submitReview(HttpServletRequest request,
                                       @PathVariable Long id,
                                       @RequestBody CourseReviewDTO dto) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        courseReviewService.submitReview(userId, "", id, dto.getRating(), dto.getContent());
        return Response.succeed();
    }

    @GetMapping("/{id}/reviews")
    public Response<List<CourseReviewVO>> getReviews(@PathVariable Long id) {
        return Response.succeed(courseReviewService.getReviews(id));
    }
}
