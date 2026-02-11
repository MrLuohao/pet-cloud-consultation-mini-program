package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 更新地址请求DTO
 *
 * @author luohao
 */
@Data
public class AddressUpdateDTO {
    private Long id;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
}
