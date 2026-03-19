package com.petcloud.shop.domain.enums;

import com.petcloud.common.core.response.IRespType;
import lombok.Getter;

@Getter
public enum ShopRespType implements IRespType {
    PRODUCT_IMAGE_UPLOAD_FAILED(false, "43000001", "图片上传失败: {}");

    private final boolean success;
    private final String code;
    private final String message;

    ShopRespType(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
