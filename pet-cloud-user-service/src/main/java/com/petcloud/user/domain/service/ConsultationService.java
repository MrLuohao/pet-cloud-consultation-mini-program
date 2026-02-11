package com.petcloud.user.domain.service;

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
    Long createConsultation(Long userId, Long petId, Long doctorId, Integer type, String description, String images);

    /**
     * 创建咨询（支持紧急类型）
     *
     * @param urgentType  normal / urgent
     */
    Long createConsultation(Long userId, Long petId, Long doctorId, Integer type, String description, String images, String urgentType);

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
     * @param userId         用户ID
     * @param consultationId 咨询ID
     * @param messageType    消息类型：1文字 2图片 3语音
     * @param content        消息内容
     * @param mediaUrl       媒体URL
     * @return 消息ID
     */
    Long sendMessage(Long userId, Long consultationId, Integer messageType, String content, String mediaUrl);

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
