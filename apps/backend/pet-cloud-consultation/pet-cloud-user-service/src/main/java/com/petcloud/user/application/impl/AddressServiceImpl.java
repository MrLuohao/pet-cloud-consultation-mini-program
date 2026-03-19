package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.dto.AddressCreateDTO;
import com.petcloud.user.domain.dto.AddressUpdateDTO;
import com.petcloud.user.domain.entity.UserAddress;
import com.petcloud.user.domain.service.AddressService;
import com.petcloud.user.domain.vo.UserAddressVO;
import com.petcloud.user.infrastructure.persistence.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddressVO> getAddressList(Long userId) {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        return userAddressMapper.selectList(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressVO getAddressDetail(Long userId, Long addressId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }
        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(Long userId, AddressCreateDTO request) {
        Long count = userAddressMapper.selectCount(
                new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)
        );
        if (count != null && count >= 20) {
            throw new BusinessException(RespType.ADDRESS_LIMIT_EXCEEDED);
        }

        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            resetDefaultAddresses(userId, null);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        applyAddressFields(address, request);
        userAddressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long addressId, Long userId, AddressUpdateDTO request) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }

        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            resetDefaultAddresses(userId, addressId);
        }

        applyAddressFields(address, request);
        userAddressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Long addressId, Long userId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }
        userAddressMapper.deleteById(addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId, Long userId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }

        resetDefaultAddresses(userId, null);
        address.setIsDefault(1);
        userAddressMapper.updateById(address);
    }

    @Override
    public UserAddressVO getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1);
        UserAddress address = userAddressMapper.selectOne(queryWrapper);
        if (address == null) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getCreateTime)
                    .last("LIMIT 1");
            address = userAddressMapper.selectOne(queryWrapper);
        }
        return address != null ? convertToVO(address) : null;
    }

    private void resetDefaultAddresses(Long userId, Long excludeAddressId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        if (excludeAddressId != null) {
            wrapper.ne(UserAddress::getId, excludeAddressId);
        }
        UserAddress defaultRecord = new UserAddress();
        defaultRecord.setIsDefault(0);
        userAddressMapper.update(defaultRecord, wrapper);
    }

    private void applyAddressFields(UserAddress address, AddressCreateDTO request) {
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0);
        address.setLongitude(request.getLongitude());
        address.setLatitude(request.getLatitude());
        address.setBusinessArea(request.getBusinessArea());
        address.setDoorNo(request.getDoorNo());
        address.setRawText(request.getRawText());
        address.setParsedName(request.getParsedName());
        address.setParsedPhone(request.getParsedPhone());
        address.setMapAddress(request.getMapAddress());
        address.setAddressTag(request.getAddressTag());
    }

    private void applyAddressFields(UserAddress address, AddressUpdateDTO request) {
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(request.getIsDefault());
        address.setLongitude(request.getLongitude());
        address.setLatitude(request.getLatitude());
        address.setBusinessArea(request.getBusinessArea());
        address.setDoorNo(request.getDoorNo());
        address.setRawText(request.getRawText());
        address.setParsedName(request.getParsedName());
        address.setParsedPhone(request.getParsedPhone());
        address.setMapAddress(request.getMapAddress());
        address.setAddressTag(request.getAddressTag());
    }

    private UserAddressVO convertToVO(UserAddress address) {
        return UserAddressVO.builder()
                .id(address.getId())
                .contactName(address.getContactName())
                .contactPhone(address.getContactPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .fullAddress(address.getFullAddress())
                .isDefault(Integer.valueOf(1).equals(address.getIsDefault()) ? 1 : 0)
                .longitude(address.getLongitude())
                .latitude(address.getLatitude())
                .businessArea(address.getBusinessArea())
                .doorNo(address.getDoorNo())
                .rawText(address.getRawText())
                .parsedName(address.getParsedName())
                .parsedPhone(address.getParsedPhone())
                .mapAddress(address.getMapAddress())
                .addressTag(address.getAddressTag())
                .build();
    }
}
