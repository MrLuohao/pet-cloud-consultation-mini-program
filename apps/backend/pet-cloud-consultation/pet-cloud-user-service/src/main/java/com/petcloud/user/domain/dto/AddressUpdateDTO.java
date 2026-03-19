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
    private java.math.BigDecimal longitude;
    private java.math.BigDecimal latitude;
    private String businessArea;
    private String doorNo;
    private String rawText;
    private String parsedName;
    private String parsedPhone;
    private String mapAddress;
    private String addressTag;
}
