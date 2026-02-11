package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.UserPet;
import com.petcloud.user.domain.entity.HealthRecord;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.vo.PetMonthlyReportVO;
import com.petcloud.user.domain.vo.PetTimelineVO;
import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.infrastructure.persistence.mapper.HealthRecordMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserPetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物管理服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final UserPetMapper userPetMapper;
    private final HealthRecordMapper healthRecordMapper;

    @Override
    public List<UserPetVO> getPetList(Long userId) {
        LambdaQueryWrapper<UserPet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPet::getUserId, userId)
                .orderByDesc(UserPet::getCreateTime);
        List<UserPet> pets = userPetMapper.selectList(queryWrapper);

        return pets.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserPetVO getPetDetail(Long userId, Long petId) {
        UserPet pet = userPetMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(RespType.PET_NOT_FOUND);
        }
        return convertToVO(pet);
    }

    @Override
    public Long createPet(Long userId, String name, Integer type, String breed, Integer gender,
                          String birthday, String weight, String avatarUrl, String healthStatus,
                          String personality, String motto) {
        // 检查宠物数量限制
        Long count = userPetMapper.selectCount(
                new LambdaQueryWrapper<UserPet>().eq(UserPet::getUserId, userId)
        );
        if (count >= 20) {
            throw new BusinessException(RespType.PET_LIMIT_EXCEEDED);
        }

        UserPet pet = new UserPet();
        pet.setUserId(userId);
        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setGender(gender);
        pet.setBirthday(birthday != null && !birthday.isEmpty() ? LocalDate.parse(birthday) : null);
        pet.setWeight(weight != null && !weight.isEmpty() ? new BigDecimal(weight) : null);
        pet.setAvatarUrl(avatarUrl);
        pet.setHealthStatus(healthStatus);
        pet.setPersonality(personality);
        pet.setMotto(motto);

        userPetMapper.insert(pet);
        return pet.getId();
    }

    @Override
    public void updatePet(Long petId, Long userId, String name, Integer type, String breed, Integer gender,
                          String birthday, String weight, String avatarUrl, String healthStatus,
                          String personality, String motto) {
        UserPet pet = userPetMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(RespType.PET_NOT_FOUND);
        }

        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setGender(gender);
        pet.setBirthday(birthday != null && !birthday.isEmpty() ? LocalDate.parse(birthday) : null);
        pet.setWeight(weight != null && !weight.isEmpty() ? new BigDecimal(weight) : null);
        pet.setAvatarUrl(avatarUrl);
        pet.setHealthStatus(healthStatus);
        pet.setPersonality(personality);
        pet.setMotto(motto);

        userPetMapper.updateById(pet);
    }

    @Override
    public void deletePet(Long petId, Long userId) {
        UserPet pet = userPetMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(RespType.PET_NOT_FOUND);
        }
        userPetMapper.deleteById(petId);
    }

    @Override
    public List<PetTimelineVO> getPetTimeline(Long userId, Long petId) {
        // 验证宠物归属
        UserPet pet = userPetMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(RespType.PET_NOT_FOUND);
        }

        List<PetTimelineVO> timeline = new ArrayList<>();

        // 从健康档案构建时间轴
        LambdaQueryWrapper<HealthRecord> hq = new LambdaQueryWrapper<>();
        hq.eq(HealthRecord::getPetId, petId)
          .orderByDesc(HealthRecord::getRecordDate);
        healthRecordMapper.selectList(hq).forEach(r -> {
            String icon = getRecordIcon(r.getRecordType());
            String color = getRecordColor(r.getRecordType());
            timeline.add(PetTimelineVO.builder()
                    .eventType("health_record")
                    .eventId(r.getId())
                    .title(r.getTitle() != null ? r.getTitle() : r.getRecordType())
                    .content(r.getContent())
                    .eventDate(r.getRecordDate())
                    .icon(icon)
                    .color(color)
                    .build());
        });

        // 按日期降序排列
        timeline.sort(Comparator.comparing(PetTimelineVO::getEventDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return timeline;
    }

    @Override
    public PetMonthlyReportVO getMonthlyReport(Long userId, Long petId, Integer year, Integer month) {
        UserPet pet = userPetMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(RespType.PET_NOT_FOUND);
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        LambdaQueryWrapper<HealthRecord> hq = new LambdaQueryWrapper<>();
        hq.eq(HealthRecord::getPetId, petId)
          .between(HealthRecord::getRecordDate, start, end);
        List<HealthRecord> records = healthRecordMapper.selectList(hq);

        int vaccineCount = (int) records.stream().filter(r -> "vaccine".equals(r.getRecordType())).count();
        int checkupCount = (int) records.stream().filter(r -> "checkup".equals(r.getRecordType())).count();
        int medicineCount = (int) records.stream().filter(r -> "medicine".equals(r.getRecordType())).count();
        int surgeryCount = (int) records.stream().filter(r -> "surgery".equals(r.getRecordType())).count();

        return PetMonthlyReportVO.builder()
                .petId(petId)
                .petName(pet.getName())
                .year(year)
                .month(month)
                .vaccineCount(vaccineCount)
                .checkupCount(checkupCount)
                .medicineCount(medicineCount)
                .surgeryCount(surgeryCount)
                .consultationCount(0)
                .reminderDoneCount(0)
                .totalEvents(records.size())
                .build();
    }

    private String getRecordIcon(String recordType) {
        if (recordType == null) return "📋";
        switch (recordType) {
            case "vaccine": return "💉";
            case "checkup": return "🏥";
            case "medicine": return "💊";
            case "surgery": return "🔬";
            default: return "📋";
        }
    }

    private String getRecordColor(String recordType) {
        if (recordType == null) return "gray";
        switch (recordType) {
            case "vaccine": return "blue";
            case "checkup": return "green";
            case "medicine": return "orange";
            case "surgery": return "red";
            default: return "gray";
        }
    }

    private UserPetVO convertToVO(UserPet pet) {
        return UserPetVO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .typeName(getTypeDesc(pet.getType()))
                .breed(pet.getBreed())
                .gender(pet.getGender())
                .birthday(pet.getBirthday())
                .weight(pet.getWeight())
                .avatarUrl(pet.getAvatarUrl())
                .healthStatus(pet.getHealthStatus())
                .personality(pet.getPersonality())
                .motto(pet.getMotto())
                .build();
    }

    private String getTypeDesc(Integer type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case 1:
                return "狗";
            case 2:
                return "猫";
            case 3:
                return "其他";
            default:
                return "";
        }
    }

    private String getGenderDesc(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "公";
            case 2:
                return "母";
            default:
                return "未知";
        }
    }
}
