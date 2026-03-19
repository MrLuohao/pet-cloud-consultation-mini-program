package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.PetCreateDTO;
import com.petcloud.user.domain.dto.PetUpdateDTO;
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
     * @param userId 用户ID
     * @param dto    宠物创建DTO
     * @return 宠物ID
     */
    Long createPet(Long userId, PetCreateDTO dto);

    /**
     * 更新宠物
     *
     * @param petId  宠物ID
     * @param userId 用户ID
     * @param dto    宠物更新DTO
     */
    void updatePet(Long petId, Long userId, PetUpdateDTO dto);

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
