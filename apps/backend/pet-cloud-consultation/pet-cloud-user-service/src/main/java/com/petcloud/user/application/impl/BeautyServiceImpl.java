package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.*;
import com.petcloud.user.domain.service.BeautyService;
import com.petcloud.user.domain.vo.*;
import com.petcloud.user.infrastructure.persistence.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 美容服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeautyServiceImpl implements BeautyService {

    private static final List<String> FIXED_SLOTS = Arrays.asList(
            "09:00-11:00", "11:00-13:00", "14:00-16:00", "16:00-18:00"
    );
    private static final int MAX_CAPACITY_PER_SLOT = 3;

    private final BeautyStoreMapper beautyStoreMapper;
    private final BeautyBookingMapper beautyBookingMapper;
    private final BeautyServiceItemMapper beautyServiceItemMapper;
    private final BeautyBookingLogMapper beautyBookingLogMapper;
    private final UserPetMapper userPetMapper;

    @Override
    public List<BeautyStoreVO> getStoreList() {
        LambdaQueryWrapper<BeautyStore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BeautyStore::getStatus, BeautyStore.Status.OPEN.getCode())
                .orderByDesc(BeautyStore::getRating);
        List<BeautyStore> stores = beautyStoreMapper.selectList(queryWrapper);
        return stores.stream()
                .map(this::convertStoreToVO)
                .collect(Collectors.toList());
    }

    @Override
    public BeautyStoreVO getStoreDetail(Long storeId) {
        BeautyStore store = beautyStoreMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException(RespType.STORE_NOT_FOUND);
        }
        return convertStoreToVO(store);
    }

    @Override
    public Long createBooking(BeautyBooking booking) {
        BeautyStore store = beautyStoreMapper.selectById(booking.getStoreId());
        if (store == null) {
            throw new BusinessException(RespType.STORE_NOT_FOUND);
        }
        booking.setStatus(BeautyBooking.Status.PENDING.getCode());
        beautyBookingMapper.insert(booking);
        log.info("创建预约成功，bookingId: {}", booking.getId());
        return booking.getId();
    }

    @Override
    public List<BeautyBookingVO> getBookingList(Long userId, Integer status) {
        LambdaQueryWrapper<BeautyBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BeautyBooking::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(BeautyBooking::getStatus, status);
        }
        queryWrapper.orderByDesc(BeautyBooking::getCreateTime);
        List<BeautyBooking> bookings = beautyBookingMapper.selectList(queryWrapper);

        // 批量查询门店名称
        Set<Long> storeIds = bookings.stream().map(BeautyBooking::getStoreId).collect(Collectors.toSet());
        Map<Long, String> storeNameMap = new HashMap<>();
        if (!storeIds.isEmpty()) {
            beautyStoreMapper.selectBatchIds(storeIds).forEach(s -> storeNameMap.put(s.getId(), s.getName()));
        }

        // 批量查询宠物名称
        Set<Long> petIds = bookings.stream()
                .filter(b -> b.getPetId() != null)
                .map(BeautyBooking::getPetId)
                .collect(Collectors.toSet());
        Map<Long, String> petNameMap = new HashMap<>();
        if (!petIds.isEmpty()) {
            userPetMapper.selectBatchIds(petIds).forEach(p -> petNameMap.put(p.getId(), p.getName()));
        }

        return bookings.stream()
                .map(b -> convertBookingToVO(b, storeNameMap, petNameMap))
                .collect(Collectors.toList());
    }

    @Override
    public BeautyBookingVO getBookingDetail(Long bookingId, Long userId) {
        BeautyBooking booking = beautyBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(RespType.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(RespType.BOOKING_NOT_FOUND);
        }
        BeautyStore store = beautyStoreMapper.selectById(booking.getStoreId());
        String storeName = store != null ? store.getName() : "";

        String petName = "";
        if (booking.getPetId() != null) {
            UserPet pet = userPetMapper.selectById(booking.getPetId());
            petName = pet != null ? pet.getName() : "";
        }

        Map<Long, String> storeNameMap = Collections.singletonMap(booking.getStoreId(), storeName);
        Map<Long, String> petNameMap = booking.getPetId() != null
                ? Collections.singletonMap(booking.getPetId(), petName)
                : Collections.emptyMap();
        return convertBookingToVO(booking, storeNameMap, petNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId, Long userId) {
        BeautyBooking booking = beautyBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(RespType.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(RespType.BOOKING_NOT_FOUND);
        }
        if (!BeautyBooking.Status.PENDING.getCode().equals(booking.getStatus())) {
            throw new BusinessException(RespType.BOOKING_TIME_UNAVAILABLE);
        }
        booking.setStatus(BeautyBooking.Status.CANCELLED.getCode());
        beautyBookingMapper.updateById(booking);

        BeautyBookingLog bookingLog = new BeautyBookingLog();
        bookingLog.setBookingId(bookingId);
        bookingLog.setStatus(BeautyBooking.Status.CANCELLED.getCode());
        bookingLog.setStatusText(BeautyBooking.Status.CANCELLED.getDesc());
        bookingLog.setOperatorId(userId);
        beautyBookingLogMapper.insert(bookingLog);
        log.info("取消预约成功，bookingId: {}, userId: {}", bookingId, userId);
    }

    @Override
    public List<BeautyServiceItemVO> getStoreServices(Long storeId) {
        BeautyStore store = beautyStoreMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException(RespType.STORE_NOT_FOUND);
        }
        LambdaQueryWrapper<BeautyServiceItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BeautyServiceItem::getStoreId, storeId)
                .eq(BeautyServiceItem::getStatus, 1)
                .orderByAsc(BeautyServiceItem::getSortOrder);
        return beautyServiceItemMapper.selectList(queryWrapper).stream()
                .map(s -> BeautyServiceItemVO.builder()
                        .id(s.getId())
                        .storeId(s.getStoreId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .suitableWeight(s.getSuitableWeight())
                        .duration(s.getDuration())
                        .price(s.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AvailableSlotVO> getAvailableSlots(Long storeId, LocalDate date) {
        // 查询当天该门店所有非取消预约
        LambdaQueryWrapper<BeautyBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BeautyBooking::getStoreId, storeId)
                .eq(BeautyBooking::getBookingDate, date)
                .ne(BeautyBooking::getStatus, BeautyBooking.Status.CANCELLED.getCode());
        List<BeautyBooking> bookings = beautyBookingMapper.selectList(queryWrapper);

        // 统计每个时间段预约数
        Map<String, Long> slotCount = bookings.stream()
                .filter(b -> b.getBookingTime() != null)
                .collect(Collectors.groupingBy(BeautyBooking::getBookingTime, Collectors.counting()));

        return FIXED_SLOTS.stream().map(slot -> {
            long booked = slotCount.getOrDefault(slot, 0L);
            int remaining = (int) Math.max(0, MAX_CAPACITY_PER_SLOT - booked);
            return AvailableSlotVO.builder()
                    .slot(slot)
                    .available(remaining > 0)
                    .remaining(remaining)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBookingStatus(Long bookingId, Integer status, Long operatorId) {
        BeautyBooking booking = beautyBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(RespType.BOOKING_NOT_FOUND);
        }
        booking.setStatus(status);
        beautyBookingMapper.updateById(booking);

        String statusText = Arrays.stream(BeautyBooking.Status.values())
                .filter(s -> s.getCode().equals(status))
                .map(BeautyBooking.Status::getDesc)
                .findFirst().orElse("");

        BeautyBookingLog bookingLog = new BeautyBookingLog();
        bookingLog.setBookingId(bookingId);
        bookingLog.setStatus(status);
        bookingLog.setStatusText(statusText);
        bookingLog.setOperatorId(operatorId);
        beautyBookingLogMapper.insert(bookingLog);
        log.info("更新预约状态，bookingId: {}, status: {}", bookingId, status);
    }

    @Override
    public void uploadBookingPhotos(Long bookingId, String beforePhoto, String afterPhoto, String servicePhotos) {
        BeautyBooking booking = beautyBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(RespType.BOOKING_NOT_FOUND);
        }
        if (beforePhoto != null) {
            booking.setBeforePhoto(beforePhoto);
        }
        if (afterPhoto != null) {
            booking.setAfterPhoto(afterPhoto);
        }
        if (servicePhotos != null) {
            booking.setServicePhotos(servicePhotos);
        }
        beautyBookingMapper.updateById(booking);
        log.info("上传服务照片成功，bookingId: {}", bookingId);
    }

    private BeautyStoreVO convertStoreToVO(BeautyStore store) {
        return BeautyStoreVO.builder()
                .id(store.getId())
                .name(store.getName())
                .coverUrl(store.getCoverUrl())
                .rating(store.getRating())
                .distance(store.getDistance())
                .address(store.getAddress())
                .tags(store.getTags())
                .phone(store.getPhone())
                .businessHours(store.getBusinessHours())
                .build();
    }

    private BeautyBookingVO convertBookingToVO(BeautyBooking booking,
                                               Map<Long, String> storeNameMap,
                                               Map<Long, String> petNameMap) {
        String statusText = Arrays.stream(BeautyBooking.Status.values())
                .filter(s -> s.getCode().equals(booking.getStatus()))
                .map(BeautyBooking.Status::getDesc)
                .findFirst().orElse("");
        return BeautyBookingVO.builder()
                .id(booking.getId())
                .storeId(booking.getStoreId())
                .storeName(storeNameMap.getOrDefault(booking.getStoreId(), ""))
                .petId(booking.getPetId())
                .petName(booking.getPetId() != null ? petNameMap.getOrDefault(booking.getPetId(), "") : "")
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .services(booking.getServices())
                .remark(booking.getRemark())
                .status(booking.getStatus())
                .statusText(statusText)
                .beforePhoto(booking.getBeforePhoto())
                .afterPhoto(booking.getAfterPhoto())
                .servicePhotos(booking.getServicePhotos())
                .createTime(booking.getCreateTime())
                .build();
    }
}
