package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
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

/**
 * 地址管理服务实现类
 *
 * @author luohao
 */
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
        List<UserAddress> addresses = userAddressMapper.selectList(queryWrapper);

        return addresses.stream()
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
    public Long createAddress(Long userId, String contactName, String contactPhone,
                               String province, String city, String district,
                               String detailAddress, Integer isDefault) {
        // 检查地址数量限制
        Long count = userAddressMapper.selectCount(
                new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)
        );
        if (count >= 20) {
            throw new BusinessException(RespType.ADDRESS_LIMIT_EXCEEDED);
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (isDefault != null && isDefault == 1) {
            LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserAddress::getUserId, userId);
            UserAddress defaultRecord = new UserAddress();
            defaultRecord.setIsDefault(0);
            userAddressMapper.update(defaultRecord, wrapper);
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
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long addressId, Long userId, String contactName, String contactPhone,
                               String province, String city, String district,
                               String detailAddress, Integer isDefault) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (isDefault != null && isDefault == 1) {
            LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserAddress::getUserId, userId)
                    .ne(UserAddress::getId, addressId);
            UserAddress defaultRecord = new UserAddress();
            defaultRecord.setIsDefault(0);
            userAddressMapper.update(defaultRecord, wrapper);
        }

        address.setContactName(contactName);
        address.setContactPhone(contactPhone);
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailAddress(detailAddress);
        address.setIsDefault(isDefault);

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

        // 取消所有默认地址
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        UserAddress defaultRecord = new UserAddress();
        defaultRecord.setIsDefault(0);
        userAddressMapper.update(defaultRecord, wrapper);

        // 设置新默认地址
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
            // 如果没有默认地址，返回第一个地址
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getCreateTime)
                    .last("LIMIT 1");
            address = userAddressMapper.selectOne(queryWrapper);
        }

        return address != null ? convertToVO(address) : null;
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
                .fullAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress())
                .isDefault(address.getIsDefault() != null && address.getIsDefault() == 1 ? 1 : 0)
                .build();
    }
}
