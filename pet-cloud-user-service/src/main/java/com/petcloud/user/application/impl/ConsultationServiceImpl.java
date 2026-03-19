package com.petcloud.user.application.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.utils.DateUtils;
import com.petcloud.user.domain.dto.ConsultationCreateDTO;
import com.petcloud.user.domain.dto.ConsultationMessageDTO;
import com.petcloud.user.domain.entity.*;
import com.petcloud.user.domain.service.ConsultationService;
import com.petcloud.user.domain.vo.ConsultationMessageVO;
import com.petcloud.user.domain.vo.ConsultationVO;
import com.petcloud.user.infrastructure.persistence.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 咨询服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationMapper consultationMapper;
    private final ConsultationMessageMapper consultationMessageMapper;
    private final DoctorMapper doctorMapper;
    private final UserPetMapper userPetMapper;
    private final WxUserMapper wxUserMapper;

    @Override
    public Long createConsultation(Long userId, ConsultationCreateDTO dto) {
        boolean isUrgent = "urgent".equals(dto.getUrgentType());
        Long assignedDoctorId = dto.getDoctorId();
        String waitingMessage = null;

        // 紧急问诊：优先从在线医生中分配
        if (isUrgent) {
            LambdaQueryWrapper<Doctor> onlineQuery = new LambdaQueryWrapper<>();
            onlineQuery.eq(Doctor::getOnlineStatus, 1)
                       .eq(Doctor::getStatus, 1)
                       .orderByAsc(Doctor::getAvgResponseMinutes);
            Doctor onlineDoctor = doctorMapper.selectOne(onlineQuery.last("LIMIT 1"));
            if (onlineDoctor != null) {
                assignedDoctorId = onlineDoctor.getId();
            } else {
                assignedDoctorId = null;
                waitingMessage = "暂无在线医生，您的问题已进入优先队列";
            }
        } else {
            // 普通问诊：验证指定医生
            if (assignedDoctorId != null) {
                Doctor doctor = doctorMapper.selectById(assignedDoctorId);
                if (doctor == null) {
                    throw new BusinessException(RespType.DOCTOR_NOT_FOUND);
                }
            }
        }

        // 获取用户信息
        WxUser user = wxUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(RespType.USER_NOT_FOUND);
        }

        // 获取宠物信息
        String petName = null;
        Integer petType = null;
        if (dto.getPetId() != null) {
            UserPet pet = userPetMapper.selectById(dto.getPetId());
            if (pet != null && pet.getUserId().equals(userId)) {
                petName = pet.getName();
                petType = pet.getType();
            }
        }

        // 获取医生信息（用于冗余字段）
        Doctor doctor = assignedDoctorId != null ? doctorMapper.selectById(assignedDoctorId) : null;

        // 创建咨询
        Consultation consultation = new Consultation();
        consultation.setOrderNo("CON" + System.currentTimeMillis() + IdUtil.getSnowflakeNextIdStr());
        consultation.setUserId(userId);
        consultation.setUserNickname(user.getNickname());
        consultation.setUserAvatar(user.getAvatarUrl());
        consultation.setPetId(dto.getPetId());
        consultation.setPetName(petName);
        consultation.setPetType(petType);
        consultation.setDoctorId(assignedDoctorId);
        consultation.setDoctorName(doctor != null ? doctor.getName() : null);
        consultation.setDoctorAvatar(doctor != null ? doctor.getAvatar() : null);
        consultation.setType(dto.getType());
        consultation.setStatus(Consultation.Status.PENDING.getCode());
        consultation.setDescription(dto.getDescription());
        consultation.setImages(dto.getImages());
        consultation.setFee(doctor != null ? doctor.getConsultationFee() : null);
        consultation.setIsUrgent(isUrgent ? 1 : 0);

        consultationMapper.insert(consultation);

        return consultation.getId();
    }

    @Override
    public List<ConsultationVO> getConsultationList(Long userId) {
        LambdaQueryWrapper<Consultation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Consultation::getUserId, userId)
                .orderByDesc(Consultation::getCreateTime);
        List<Consultation> consultations = consultationMapper.selectList(queryWrapper);

        return consultations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultationVO getConsultationDetail(Long userId, Long consultationId) {
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null || !consultation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONSULTATION_NOT_FOUND);
        }
        return convertToVO(consultation);
    }

    @Override
    public Long sendMessage(Long userId, ConsultationMessageDTO dto) {
        Consultation consultation = consultationMapper.selectById(dto.getConsultationId());
        if (consultation == null || !consultation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONSULTATION_NOT_FOUND);
        }

        if (!Consultation.Status.IN_PROGRESS.getCode().equals(consultation.getStatus())
                && !Consultation.Status.PENDING.getCode().equals(consultation.getStatus())) {
            throw new BusinessException(RespType.CONSULTATION_STATUS_ERROR);
        }

        // 获取用户信息
        WxUser user = wxUserMapper.selectById(userId);

        ConsultationMessage message = new ConsultationMessage();
        message.setConsultationId(dto.getConsultationId());
        message.setSenderId(userId);
        message.setSenderType(ConsultationMessage.SenderType.USER.getCode());
        message.setSenderName(user != null ? user.getNickname() : "用户");
        message.setSenderAvatar(user != null ? user.getAvatarUrl() : null);
        message.setMessageType(dto.getMessageType());
        message.setContent(dto.getContent());
        message.setMediaUrl(dto.getMediaUrl());
        message.setIsRead(0);

        consultationMessageMapper.insert(message);

        // 如果是待接单状态，自动转为进行中
        if (Consultation.Status.PENDING.getCode().equals(consultation.getStatus())) {
            consultation.setStatus(Consultation.Status.IN_PROGRESS.getCode());
            consultation.setAcceptTime(DateUtils.now());
            consultationMapper.updateById(consultation);
        }

        return message.getId();
    }

    @Override
    public List<ConsultationMessageVO> getMessages(Long userId, Long consultationId) {
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null || !consultation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONSULTATION_NOT_FOUND);
        }

        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getConsultationId, consultationId)
                .orderByAsc(ConsultationMessage::getCreateTime);
        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);

        return messages.stream()
                .map(msg -> ConsultationMessageVO.builder()
                        .id(msg.getId())
                        .consultationId(msg.getConsultationId())
                        .senderId(msg.getSenderId())
                        .senderType(msg.getSenderType())
                        .senderName(msg.getSenderName())
                        .senderAvatar(msg.getSenderAvatar())
                        .messageType(msg.getMessageType())
                        .content(msg.getContent())
                        .mediaUrl(msg.getMediaUrl())
                        .isRead(msg.getIsRead())
                        .createTime(msg.getCreateTime() != null ? msg.getCreateTime().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void finishConsultation(Long userId, Long consultationId) {
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null || !consultation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONSULTATION_NOT_FOUND);
        }

        if (!Consultation.Status.IN_PROGRESS.getCode().equals(consultation.getStatus())) {
            throw new BusinessException(RespType.CONSULTATION_STATUS_ERROR);
        }

        consultation.setStatus(Consultation.Status.COMPLETED.getCode());
        consultation.setFinishTime(DateUtils.now());
        consultationMapper.updateById(consultation);
    }

    @Override
    public void cancelConsultation(Long userId, Long consultationId) {
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null || !consultation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONSULTATION_NOT_FOUND);
        }

        if (Consultation.Status.COMPLETED.getCode().equals(consultation.getStatus())
                || Consultation.Status.CANCELLED.getCode().equals(consultation.getStatus())) {
            throw new BusinessException(RespType.CONSULTATION_STATUS_ERROR);
        }

        consultation.setStatus(Consultation.Status.CANCELLED.getCode());
        consultation.setCancelTime(DateUtils.now());
        consultationMapper.updateById(consultation);
    }

    @Override
    public void payConsultation(Long userId, Long consultationId) {
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null || !consultation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONSULTATION_NOT_FOUND);
        }

        if (!Consultation.Status.PENDING.getCode().equals(consultation.getStatus())) {
            throw new BusinessException(RespType.CONSULTATION_STATUS_ERROR);
        }

        // 模拟支付成功：直接转为进行中
        consultation.setStatus(Consultation.Status.IN_PROGRESS.getCode());
        consultation.setAcceptTime(DateUtils.now());
        consultationMapper.updateById(consultation);
    }

    private ConsultationVO convertToVO(Consultation consultation) {
        return ConsultationVO.builder()
                .id(consultation.getId())
                .orderNo(consultation.getOrderNo())
                .doctorId(consultation.getDoctorId())
                .doctorName(consultation.getDoctorName())
                .doctorAvatar(consultation.getDoctorAvatar())
                .petId(consultation.getPetId())
                .petName(consultation.getPetName())
                .petType(consultation.getPetType())
                .type(consultation.getType())
                .typeDesc(Integer.valueOf(1).equals(consultation.getType()) ? "图文" : "视频")
                .status(consultation.getStatus())
                .statusDesc(getStatusDesc(consultation.getStatus()))
                .description(consultation.getDescription())
                .images(consultation.getImages() != null ? Arrays.asList(consultation.getImages().split(",")) : null)
                .fee(consultation.getFee())
                .isUrgent(consultation.getIsUrgent())
                .createTime(consultation.getCreateTime())
                .build();
    }

    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        return Arrays.stream(Consultation.Status.values())
                .filter(s -> s.getCode().equals(status))
                .findFirst()
                .map(Consultation.Status::getDesc)
                .orElse("");
    }
}
