package com.petcloud.user.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class FeaturedContentVO {

    private Long id;

    private Long draftId;

    private String title;

    private String summary;

    private String coverUrl;

    private String tag;

    private String reasonLabel;

    private String targetPage;

    private Long targetId;

    private Integer positionNo;

    private Date startTime;

    private Date endTime;
}
