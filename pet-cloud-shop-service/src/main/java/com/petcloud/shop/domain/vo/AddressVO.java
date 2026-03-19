package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 收货地址VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressVO {

    private Long id;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String fullAddress;
    private Boolean isDefault;
    private String receiverName;
    private String receiverPhone;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String businessArea;
    private String doorNo;
    private String mapAddress;
}
