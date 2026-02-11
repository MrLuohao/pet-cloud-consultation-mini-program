package com.petcloud.user.domain.service;

import com.petcloud.user.domain.entity.BeautyBooking;
import com.petcloud.user.domain.vo.AvailableSlotVO;
import com.petcloud.user.domain.vo.BeautyBookingVO;
import com.petcloud.user.domain.vo.BeautyServiceItemVO;
import com.petcloud.user.domain.vo.BeautyStoreVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 美容服务接口
 *
 * @author luohao
 */
public interface BeautyService {

    /**
     * 获取门店列表
     */
    List<BeautyStoreVO> getStoreList();

    /**
     * 获取门店详情
     */
    BeautyStoreVO getStoreDetail(Long storeId);

    /**
     * 创建预约
     */
    Long createBooking(BeautyBooking booking);

    /**
     * 获取我的预约列表
     *
     * @param userId 用户ID
     * @param status 状态筛选（null=全部）
     */
    List<BeautyBookingVO> getBookingList(Long userId, Integer status);

    /**
     * 获取预约详情
     *
     * @param bookingId 预约ID
     * @param userId    用户ID（用于权限校验）
     */
    BeautyBookingVO getBookingDetail(Long bookingId, Long userId);

    /**
     * 取消预约
     *
     * @param bookingId 预约ID
     * @param userId    用户ID（用于权限校验）
     */
    void cancelBooking(Long bookingId, Long userId);

    /**
     * 获取门店服务项目列表
     *
     * @param storeId 门店ID
     */
    List<BeautyServiceItemVO> getStoreServices(Long storeId);

    /**
     * 获取可预约时间段
     *
     * @param storeId 门店ID
     * @param date    预约日期
     */
    List<AvailableSlotVO> getAvailableSlots(Long storeId, LocalDate date);

    /**
     * 更新预约状态（服务方使用）
     *
     * @param bookingId  预约ID
     * @param status     新状态
     * @param operatorId 操作人ID
     */
    void updateBookingStatus(Long bookingId, Integer status, Long operatorId);

    /**
     * 上传服务照片
     *
     * @param bookingId     预约ID
     * @param beforePhoto   服务前照片URL
     * @param afterPhoto    服务后照片URL
     * @param servicePhotos 服务过程照片JSON
     */
    void uploadBookingPhotos(Long bookingId, String beforePhoto, String afterPhoto, String servicePhotos);
}
