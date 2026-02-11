package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.UserAddressVO;

import java.util.List;

/**
 * 地址管理服务接口
 *
 * @author luohao
 */
public interface AddressService {

    /**
     * 获取地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddressVO> getAddressList(Long userId);

    /**
     * 获取地址详情
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址详情
     */
    UserAddressVO getAddressDetail(Long userId, Long addressId);

    /**
     * 创建地址
     *
     * @param userId       用户ID
     * @param contactName  联系人
     * @param contactPhone 联系电话
     * @param province     省份
     * @param city         城市
     * @param district     区县
     * @param detailAddress 详细地址
     * @param isDefault    是否默认
     * @return 地址ID
     */
    Long createAddress(Long userId, String contactName, String contactPhone,
                       String province, String city, String district,
                       String detailAddress, Integer isDefault);

    /**
     * 更新地址
     *
     * @param addressId    地址ID
     * @param userId       用户ID
     * @param contactName  联系人
     * @param contactPhone 联系电话
     * @param province     省份
     * @param city         城市
     * @param district     区县
     * @param detailAddress 详细地址
     * @param isDefault    是否默认
     */
    void updateAddress(Long addressId, Long userId, String contactName, String contactPhone,
                       String province, String city, String district,
                       String detailAddress, Integer isDefault);

    /**
     * 删除地址
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     */
    void deleteAddress(Long addressId, Long userId);

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     */
    void setDefaultAddress(Long addressId, Long userId);

    /**
     * 获取默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    UserAddressVO getDefaultAddress(Long userId);
}
