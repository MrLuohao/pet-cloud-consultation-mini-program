package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.FeaturedContentVO;

import java.util.List;

public interface FeaturedContentService {

    List<FeaturedContentVO> getPublishedContents(int limit);
}
