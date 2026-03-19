package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.HomeSummaryVO;

public interface HomeSummaryService {

    HomeSummaryVO getHomeSummary(Long userId);

    HomeSummaryVO.PetCardVO getCurrentPetCard(Long userId);
}
