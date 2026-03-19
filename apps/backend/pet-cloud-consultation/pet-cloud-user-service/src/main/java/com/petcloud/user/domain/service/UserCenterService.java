package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.domain.vo.UserAddressVO;

import java.util.List;

/**
 * 用户中心服务接口
 *
 * @author luohao
 */
public interface UserCenterService {

    /**
     * 获取用户宠物列表
     *
     * @param userId 用户ID
     * @return 宠物VO列表
     */
    List<UserPetVO> getUserPets(Long userId);

    /**
     * 添加宠物
     *
     * @param userId 用户ID
     * @param petName 宠物名称
     * @param petType 宠物类型
     * @param breed 品种
     * @param gender 性别
     * @param birthday 生日
     * @param weight 体重
     * @return 宠物ID
     */
    Long addPet(Long userId, String petName, Integer petType, String breed, Integer gender, String birthday, String weight);

    /**
     * 获取收货地址列表
     *
     * @param userId 用户ID
     * @return 地址VO列表
     */
    List<UserAddressVO> getUserAddresses(Long userId);

    /**
     * 添加收货地址
     *
     * @param userId 用户ID
     * @param contactName 联系人
     * @param contactPhone 联系电话
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detailAddress 详细地址
     * @param isDefault 是否默认
     * @return 地址ID
     */
    Long addAddress(Long userId, String contactName, String contactPhone, String province, String city, String district, String detailAddress, Integer isDefault);

    /**
     * 更新收货地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @param contactName 联系人
     * @param contactPhone 联系电话
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detailAddress 详细地址
     * @param isDefault 是否默认
     */
    void updateAddress(Long addressId, Long userId, String contactName, String contactPhone, String province, String city, String district, String detailAddress, Integer isDefault);
}
