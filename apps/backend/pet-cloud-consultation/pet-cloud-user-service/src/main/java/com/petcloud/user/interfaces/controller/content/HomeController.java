package com.petcloud.user.interfaces.controller.content;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.HomeSummaryService;
import com.petcloud.user.domain.vo.HomeSummaryVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeController {

    private final UserContextHolderWeb userContextHolderWeb;
    private final HomeSummaryService homeSummaryService;

    @GetMapping("/home/summary")
    public Response<HomeSummaryVO> getHomeSummary(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getCurrentUserId(request);
        return Response.succeed(homeSummaryService.getHomeSummary(userId));
    }

    @GetMapping("/pets/current-card")
    public Response<HomeSummaryVO.PetCardVO> getCurrentPetCard(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(homeSummaryService.getCurrentPetCard(userId));
    }
}
