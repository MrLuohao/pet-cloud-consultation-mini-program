package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.domain.vo.PetTimelineVO;
import com.petcloud.user.domain.vo.PetMonthlyReportVO;

import java.util.List;

/**
 * 宠物管理服务接口
 *
 * @author luohao
 */
public interface PetService {

    /**
     * 获取宠物列表
     *
     * @param userId 用户ID
     * @return 宠物列表
     */
    List<UserPetVO> getPetList(Long userId);

    /**
     * 获取宠物详情
     *
     * @param userId 用户ID
     * @param petId  宠物ID
     * @return 宠物详情
     */
    UserPetVO getPetDetail(Long userId, Long petId);

    /**
     * 创建宠物
     *
     * @param userId    用户ID
     * @param name      宠物名称
     * @param type      宠物类型
     * @param breed     品种
     * @param gender    性别
     * @param birthday  生日
     * @param weight    体重
     * @param avatarUrl 头像URL
     * @param healthStatus 健康状况
     * @param personality 性格
     * @param motto     座右铭
     * @return 宠物ID
     */
    Long createPet(Long userId, String name, Integer type, String breed, Integer gender,
                   String birthday, String weight, String avatarUrl, String healthStatus,
                   String personality, String motto);

    /**
     * 更新宠物
     *
     * @param petId     宠物ID
     * @param userId    用户ID
     * @param name      宠物名称
     * @param type      宠物类型
     * @param breed     品种
     * @param gender    性别
     * @param birthday  生日
     * @param weight    体重
     * @param avatarUrl 头像URL
     * @param healthStatus 健康状况
     * @param personality 性格
     * @param motto     座右铭
     */
    void updatePet(Long petId, Long userId, String name, Integer type, String breed, Integer gender,
                   String birthday, String weight, String avatarUrl, String healthStatus,
                   String personality, String motto);

    /**
     * 删除宠物
     *
     * @param petId  宠物ID
     * @param userId 用户ID
     */
    void deletePet(Long petId, Long userId);

    List<PetTimelineVO> getPetTimeline(Long userId, Long petId);

    PetMonthlyReportVO getMonthlyReport(Long userId, Long petId, Integer year, Integer month);
}
