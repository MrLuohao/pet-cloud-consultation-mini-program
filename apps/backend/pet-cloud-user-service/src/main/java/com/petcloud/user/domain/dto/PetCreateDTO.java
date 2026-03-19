package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 创建宠物请求DTO
 *
 * @author luohao
 */
@Data
public class PetCreateDTO {
    private String name;
    private Integer type;
    private String breed;
    private Integer gender;
    private String birthday;
    private String weight;
    private String avatarUrl;
    private String healthStatus;
    private String personality;
    private String motto;
}
