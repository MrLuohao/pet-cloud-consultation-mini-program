package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 创建咨询请求DTO
 *
 * @author luohao
 */
@Data
public class ConsultationCreateDTO {
    private Long petId;
    private Long doctorId;
    private Integer type;
    private String description;
    private String images;
    /** 是否紧急问诊：urgent / normal，默认 normal */
    private String urgentType;
}
