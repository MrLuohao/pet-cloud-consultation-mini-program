package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.HealthRecord;
import com.petcloud.user.domain.entity.UserPet;
import com.petcloud.user.domain.service.HealthRecordService;
import com.petcloud.user.domain.vo.HealthRecordVO;
import com.petcloud.user.infrastructure.persistence.mapper.HealthRecordMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserPetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健康档案服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordMapper healthRecordMapper;
    private final UserPetMapper userPetMapper;

    @Override
    public List<HealthRecordVO> getHealthRecordList(Long userId) {
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getUserId, userId)
                .orderByDesc(HealthRecord::getRecordDate);
        List<HealthRecord> records = healthRecordMapper.selectList(queryWrapper);

        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HealthRecordVO> getHealthRecordsByPet(Long userId, Long petId) {
        // 验证宠物所有权
        UserPet pet = userPetMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(RespType.PET_NOT_FOUND);
        }

        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getUserId, userId)
                .eq(HealthRecord::getPetId, petId)
                .orderByDesc(HealthRecord::getRecordDate);
        List<HealthRecord> records = healthRecordMapper.selectList(queryWrapper);

        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Long createHealthRecord(Long userId, Long petId, String recordType, String title,
                                    String content, String hospitalName, String doctorName,
                                    LocalDate recordDate, LocalDate nextDate, String images) {
        // 验证宠物所有权
        UserPet pet = userPetMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(RespType.PET_NOT_FOUND);
        }

        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setPetId(petId);
        record.setPetName(pet.getName());
        record.setRecordType(recordType);
        record.setTitle(title);
        record.setContent(content);
        record.setHospitalName(hospitalName);
        record.setDoctorName(doctorName);
        record.setRecordDate(recordDate != null ? recordDate : LocalDate.now());
        record.setNextDate(nextDate);
        record.setImages(images);

        healthRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void updateHealthRecord(Long recordId, Long userId, String recordType, String title,
                                    String content, String hospitalName, String doctorName,
                                    LocalDate recordDate, LocalDate nextDate, String images) {
        HealthRecord record = healthRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(RespType.HEALTH_RECORD_NOT_FOUND);
        }

        record.setRecordType(recordType);
        record.setTitle(title);
        record.setContent(content);
        record.setHospitalName(hospitalName);
        record.setDoctorName(doctorName);
        record.setRecordDate(recordDate);
        record.setNextDate(nextDate);
        record.setImages(images);

        healthRecordMapper.updateById(record);
    }

    @Override
    public void deleteHealthRecord(Long recordId, Long userId) {
        HealthRecord record = healthRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(RespType.HEALTH_RECORD_NOT_FOUND);
        }
        healthRecordMapper.deleteById(recordId);
    }

    private HealthRecordVO convertToVO(HealthRecord record) {
        return HealthRecordVO.builder()
                .id(record.getId())
                .petId(record.getPetId())
                .petName(record.getPetName())
                .recordType(record.getRecordType())
                .recordTypeDesc(getRecordTypeDesc(record.getRecordType()))
                .title(record.getTitle())
                .content(record.getContent())
                .hospitalName(record.getHospitalName())
                .doctorName(record.getDoctorName())
                .recordDate(record.getRecordDate())
                .nextDate(record.getNextDate())
                .images(record.getImages() != null ? Arrays.asList(record.getImages().split(",")) : null)
                .build();
    }

    private String getRecordTypeDesc(String recordType) {
        if (recordType == null) {
            return "";
        }
        switch (recordType) {
            case "vaccine":
                return "疫苗接种";
            case "checkup":
                return "健康检查";
            case "medicine":
                return "用药记录";
            case "surgery":
                return "手术记录";
            case "other":
                return "其他";
            default:
                return recordType;
        }
    }
}
