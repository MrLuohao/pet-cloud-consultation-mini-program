package com.petcloud.user.interfaces.controller.user;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.UserCenterService;
import com.petcloud.user.domain.vo.UserAddressVO;
import com.petcloud.user.domain.vo.UserPetVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户中心控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserCenterController {

    private final UserCenterService userCenterService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 获取用户宠物列表
     *
     * @param request HttpServletRequest
     * @return 宠物列表
     */
    @GetMapping("/pets")
    public Response<List<UserPetVO>> getUserPets(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取用户宠物列表，userId: {}", userId);
        List<UserPetVO> pets = userCenterService.getUserPets(userId);
        return Response.succeed(pets);
    }

    /**
     * 添加宠物
     *
     * @param request HttpServletRequest
     * @param petName 宠物名称
     * @param petType 宠物类型
     * @param breed 品种
     * @param gender 性别
     * @param birthday 生日
     * @param weight 体重
     * @return 宠物ID
     */
    @PostMapping("/pet")
    public Response<Long> addPet(
            HttpServletRequest request,
            @RequestParam String petName,
            @RequestParam Integer petType,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false, defaultValue = "0") Integer gender,
            @RequestParam(required = false) String birthday,
            @RequestParam(required = false) String weight) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("添加宠物，userId: {}, petName: {}", userId, petName);
        Long petId = userCenterService.addPet(userId, petName, petType, breed, gender, birthday, weight);
        return Response.succeed(petId);
    }

    /**
     * 获取收货地址列表
     *
     * @param request HttpServletRequest
     * @return 地址列表
     */
    @GetMapping("/address")
    public Response<List<UserAddressVO>> getUserAddresses(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取收货地址列表，userId: {}", userId);
        List<UserAddressVO> addresses = userCenterService.getUserAddresses(userId);
        return Response.succeed(addresses);
    }

    /**
     * 添加收货地址
     *
     * @param request HttpServletRequest
     * @param contactName 联系人
     * @param contactPhone 联系电话
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detailAddress 详细地址
     * @param isDefault 是否默认
     * @return 地址ID
     */
    @PostMapping("/address")
    public Response<Long> addAddress(
            HttpServletRequest request,
            @RequestParam String contactName,
            @RequestParam String contactPhone,
            @RequestParam String province,
            @RequestParam String city,
            @RequestParam String district,
            @RequestParam String detailAddress,
            @RequestParam(required = false, defaultValue = "0") Integer isDefault) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("添加收货地址，userId: {}", userId);
        Long addressId = userCenterService.addAddress(userId, contactName, contactPhone, province, city, district, detailAddress, isDefault);
        return Response.succeed(addressId);
    }

    /**
     * 更新收货地址
     *
     * @param id 地址ID
     * @param request HttpServletRequest
     * @param contactName 联系人
     * @param contactPhone 联系电话
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detailAddress 详细地址
     * @param isDefault 是否默认
     * @return 操作结果
     */
    @PutMapping("/address/{id}")
    public Response<Void> updateAddress(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String detailAddress,
            @RequestParam(required = false) Integer isDefault) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("更新收货地址，addressId: {}", id);
        userCenterService.updateAddress(id, userId, contactName, contactPhone, province, city, district, detailAddress, isDefault);
        return Response.succeed();
    }
}
