package com.petcloud.user.interfaces.controller.pet;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.AddressCreateDTO;
import com.petcloud.user.domain.dto.AddressUpdateDTO;
import com.petcloud.user.domain.service.AddressService;
import com.petcloud.user.domain.vo.UserAddressVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址管理控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserContextHolderWeb userContextHolderWeb;

    /**
     * 获取地址列表
     */
    @GetMapping("/list")
    public Response<List<UserAddressVO>> getAddressList(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取地址列表，userId: {}", userId);
        List<UserAddressVO> addresses = addressService.getAddressList(userId);
        return Response.succeed(addresses);
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/{id}")
    public Response<UserAddressVO> getAddressDetail(HttpServletRequest request,
                                                  @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取地址详情，userId: {}, addressId: {}", userId, id);
        UserAddressVO address = addressService.getAddressDetail(userId, id);
        return Response.succeed(address);
    }

    /**
     * 创建地址
     */
    @PostMapping("/create")
    public Response<Long> createAddress(HttpServletRequest request,
                                       @RequestBody AddressCreateDTO addressRequest) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("创建地址，userId: {}", userId);
        Long addressId = addressService.createAddress(userId, addressRequest);
        return Response.succeed(addressId);
    }

    /**
     * 更新地址
     */
    @PutMapping("/update")
    public Response<Void> updateAddress(HttpServletRequest request,
                                       @RequestBody AddressUpdateDTO addressRequest) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("更新地址，userId: {}, addressId: {}", userId, addressRequest.getId());
        addressService.updateAddress(addressRequest.getId(), userId, addressRequest);
        return Response.succeed();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete")
    public Response<Void> deleteAddress(HttpServletRequest request,
                                       @RequestParam Long addressId) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("删除地址，userId: {}, addressId: {}", userId, addressId);
        addressService.deleteAddress(addressId, userId);
        return Response.succeed();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    public Response<Void> setDefaultAddress(HttpServletRequest request,
                                          @RequestParam Long addressId) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("设置默认地址，userId: {}, addressId: {}", userId, addressId);
        addressService.setDefaultAddress(addressId, userId);
        return Response.succeed();
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public Response<UserAddressVO> getDefaultAddress(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取默认地址，userId: {}", userId);
        UserAddressVO address = addressService.getDefaultAddress(userId);
        return Response.succeed(address);
    }
}
