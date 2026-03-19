package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.HealthRecordVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 健康档案服务接口
 *
 * @author luohao
 */
public interface HealthRecordService {

    /**
     * 获取健康档案列表
     *
     * @param userId 用户ID
     * @return 健康档案列表
     */
    List<HealthRecordVO> getHealthRecordList(Long userId);

    /**
     * 获取指定宠物的健康档案
     *
     * @param userId 用户ID
     * @param petId  宠物ID
     * @return 健康档案列表
     */
    List<HealthRecordVO> getHealthRecordsByPet(Long userId, Long petId);

    /**
     * 创建健康档案
     *
     * @param userId      用户ID
     * @param petId       宠物ID
     * @param recordType  记录类型
     * @param title       标题
     * @param content     详细内容
     * @param hospitalName 医院名称
     * @param doctorName  医生姓名
     * @param recordDate  记录日期
     * @param nextDate    下次日期
     * @param images      相关图片
     * @return 健康档案ID
     */
    Long createHealthRecord(Long userId, Long petId, String recordType, String title,
                            String content, String hospitalName, String doctorName,
                            LocalDate recordDate, LocalDate nextDate, String images);

    /**
     * 更新健康档案
     *
     * @param recordId    健康档案ID
     * @param userId      用户ID
     * @param recordType  记录类型
     * @param title       标题
     * @param content     详细内容
     * @param hospitalName 医院名称
     * @param doctorName  医生姓名
     * @param recordDate  记录日期
     * @param nextDate    下次日期
     * @param images      相关图片
     */
    void updateHealthRecord(Long recordId, Long userId, String recordType, String title,
                            String content, String hospitalName, String doctorName,
                            LocalDate recordDate, LocalDate nextDate, String images);

    /**
     * 删除健康档案
     *
     * @param recordId 健康档案ID
     * @param userId   用户ID
     */
    void deleteHealthRecord(Long recordId, Long userId);
}
