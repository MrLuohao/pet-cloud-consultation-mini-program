package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.ConsultationCreateDTO;
import com.petcloud.user.domain.dto.ConsultationMessageDTO;
import com.petcloud.user.domain.vo.ConsultationVO;
import com.petcloud.user.domain.vo.ConsultationMessageVO;

import java.util.List;

/**
 * 咨询服务接口
 *
 * @author luohao
 */
public interface ConsultationService {

    /**
     * 创建咨询
     */
    Long createConsultation(Long userId, ConsultationCreateDTO dto);

    /**
     * 获取我的咨询列表
     *
     * @param userId 用户ID
     * @return 咨询列表
     */
    List<ConsultationVO> getConsultationList(Long userId);

    /**
     * 获取咨询详情
     *
     * @param userId        用户ID
     * @param consultationId 咨询ID
     * @return 咨询详情
     */
    ConsultationVO getConsultationDetail(Long userId, Long consultationId);

    /**
     * 发送消息
     *
     * @param userId 用户ID
     * @param dto    消息请求DTO
     * @return 消息ID
     */
    Long sendMessage(Long userId, ConsultationMessageDTO dto);

    /**
     * 获取聊天记录
     *
     * @param userId         用户ID
     * @param consultationId 咨询ID
     * @return 聊天记录
     */
    List<ConsultationMessageVO> getMessages(Long userId, Long consultationId);

    /**
     * 完成咨询
     *
     * @param userId         用户ID
     * @param consultationId 咨询ID
     */
    void finishConsultation(Long userId, Long consultationId);

    /**
     * 取消咨询
     *
     * @param userId         用户ID
     * @param consultationId 咨询ID
     */
    void cancelConsultation(Long userId, Long consultationId);

    /**
     * 支付咨询费用（模拟支付）
     *
     * @param userId         用户ID
     * @param consultationId 咨询ID
     */
    void payConsultation(Long userId, Long consultationId);
}
