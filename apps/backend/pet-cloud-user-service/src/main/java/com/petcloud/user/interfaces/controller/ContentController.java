package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.service.FeaturedContentService;
import com.petcloud.user.domain.vo.FeaturedContentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController {

    private final FeaturedContentService featuredContentService;

    @GetMapping("/featured")
    public Response<List<FeaturedContentVO>> getFeaturedContents(
            @RequestParam(defaultValue = "6") int limit) {
        return Response.succeed(featuredContentService.getPublishedContents(limit));
    }
}
