package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

/**
 * 美容预约VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeautyBookingVO {

    private Long id;

    private Long storeId;

    private String storeName;

    private Long petId;

    private String petName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate bookingDate;

    private String bookingTime;

    private String services;

    private String remark;

    private Integer status;

    private String statusText;

    private String beforePhoto;

    private String afterPhoto;

    private String servicePhotos;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
