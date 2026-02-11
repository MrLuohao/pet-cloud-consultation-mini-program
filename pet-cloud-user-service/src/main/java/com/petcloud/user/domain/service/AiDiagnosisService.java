package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.AiDiagnosisDTO;

/**
 * AI诊断服务接口
 *
 * @author luohao
 */
public interface AiDiagnosisService {

    /**
     * 宠物AI健康诊断
     *
     * @param diagnosisDTO 诊断请求
     * @return 诊断结果
     */
    String diagnose(AiDiagnosisDTO diagnosisDTO);
}
