package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.UserAddress;
import com.petcloud.user.domain.entity.UserPet;
import com.petcloud.user.domain.service.UserCenterService;
import com.petcloud.user.domain.vo.UserAddressVO;
import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.infrastructure.persistence.mapper.UserAddressMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserPetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户中心服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCenterServiceImpl implements UserCenterService {

    private final UserPetMapper userPetMapper;
    private final UserAddressMapper userAddressMapper;

    @Override
    public List<UserPetVO> getUserPets(Long userId) {
        LambdaQueryWrapper<UserPet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPet::getUserId, userId)
                .orderByDesc(UserPet::getCreateTime);
        List<UserPet> pets = userPetMapper.selectList(queryWrapper);
        return pets.stream()
                .map(this::convertToPetVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addPet(Long userId, String petName, Integer petType, String breed, Integer gender, String birthday, String weight) {
        UserPet pet = new UserPet();
        pet.setUserId(userId);
        pet.setName(petName);
        pet.setType(petType);
        pet.setBreed(breed);
        pet.setGender(gender != null ? gender : 0);
        if (birthday != null && !birthday.isEmpty()) {
            pet.setBirthday(LocalDate.parse(birthday));
        }
        if (weight != null && !weight.isEmpty()) {
            pet.setWeight(new BigDecimal(weight));
        }
        userPetMapper.insert(pet);
        log.info("添加宠物成功，userId: {}, petName: {}", userId, petName);
        return pet.getId();
    }

    @Override
    public List<UserAddressVO> getUserAddresses(Long userId) {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        List<UserAddress> addresses = userAddressMapper.selectList(queryWrapper);
        return addresses.stream()
                .map(this::convertToAddressVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(Long userId, String contactName, String contactPhone, String province, String city, String district, String detailAddress, Integer isDefault) {
        // 如果设置为默认地址，先取消其他默认地址
        if (isDefault != null && isDefault == 1) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, 0);
            userAddressMapper.update(null, updateWrapper);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setContactName(contactName);
        address.setContactPhone(contactPhone);
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailAddress(detailAddress);
        address.setIsDefault(isDefault != null ? isDefault : 0);
        userAddressMapper.insert(address);
        log.info("添加收货地址成功，userId: {}", userId);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long addressId, Long userId, String contactName, String contactPhone, String province, String city, String district, String detailAddress, Integer isDefault) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NO_PERMISSION);
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (isDefault != null && isDefault == 1) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                    .ne(UserAddress::getId, addressId)
                    .set(UserAddress::getIsDefault, 0);
            userAddressMapper.update(null, updateWrapper);
        }

        if (contactName != null) {
            address.setContactName(contactName);
        }
        if (contactPhone != null) {
            address.setContactPhone(contactPhone);
        }
        if (province != null) {
            address.setProvince(province);
        }
        if (city != null) {
            address.setCity(city);
        }
        if (district != null) {
            address.setDistrict(district);
        }
        if (detailAddress != null) {
            address.setDetailAddress(detailAddress);
        }
        if (isDefault != null) {
            address.setIsDefault(isDefault);
        }

        userAddressMapper.updateById(address);
        log.info("更新收货地址成功，addressId: {}", addressId);
    }

    private UserPetVO convertToPetVO(UserPet pet) {
        String typeName = switch (pet.getType()) {
            case 1 -> "狗";
            case 2 -> "猫";
            case 3 -> "其他";
            default -> "未知";
        };

        return UserPetVO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .typeName(typeName)
                .breed(pet.getBreed())
                .gender(pet.getGender())
                .birthday(pet.getBirthday())
                .weight(pet.getWeight())
                .avatarUrl(pet.getAvatarUrl())
                .healthStatus(pet.getHealthStatus())
                .build();
    }

    private UserAddressVO convertToAddressVO(UserAddress address) {
        return UserAddressVO.builder()
                .id(address.getId())
                .contactName(address.getContactName())
                .contactPhone(address.getContactPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .fullAddress(address.getFullAddress())
                .isDefault(address.getIsDefault())
                .build();
    }
}
