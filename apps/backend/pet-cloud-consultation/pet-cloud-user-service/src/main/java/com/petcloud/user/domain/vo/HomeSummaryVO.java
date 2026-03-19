package com.petcloud.user.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomeSummaryVO {

    private Boolean loggedIn;
    private PetCardVO petCard;
    private ReminderSummaryVO reminderSummary;
    private TodayCareSummaryVO todayCareSummary;
    private List<FeaturedContentVO> featuredContents;

    @Data
    @Builder
    public static class PetCardVO {
        private Long currentPetId;
        private Integer petCount;
        private Integer currentIndex;
        private List<PetCardItemVO> pets;
    }

    @Data
    @Builder
    public static class PetCardItemVO {
        private Long petId;
        private String name;
        private String avatarUrl;
        private String breed;
        private String ageText;
        private String statusSummary;
        private String signature;
    }

    @Data
    @Builder
    public static class ReminderSummaryVO {
        private Boolean hasPending;
        private Integer pendingCount;
        private String nextTitle;
        private String nextPetName;
        private String nextRemindDateText;
    }

    @Data
    @Builder
    public static class TodayCareSummaryVO {
        private Integer totalCount;
        private Integer completedCount;
        private Integer totalPoints;
        private Integer completionRate;
        private List<TodayCareTaskVO> tasks;
    }

    @Data
    @Builder
    public static class TodayCareTaskVO {
        private Long taskId;
        private String code;
        private String name;
        private String desc;
        private String icon;
        private Integer points;
        private Boolean completed;
    }

}
