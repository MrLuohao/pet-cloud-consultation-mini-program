package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 消息VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 类型
     */
    private String type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色主题
     */
    private String color;

    /**
     * 额外数据
     */
    private String extraData;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
