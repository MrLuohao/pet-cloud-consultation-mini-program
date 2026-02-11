package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 创建地址请求DTO
 *
 * @author luohao
 */
@Data
public class AddressCreateDTO {
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
}
