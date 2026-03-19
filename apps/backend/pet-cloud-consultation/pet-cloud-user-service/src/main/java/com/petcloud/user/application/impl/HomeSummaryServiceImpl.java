package com.petcloud.user.application.impl;

import com.petcloud.user.domain.service.FeaturedContentService;
import com.petcloud.user.domain.service.HealthReminderService;
import com.petcloud.user.domain.service.HomeSummaryService;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.service.TaskService;
import com.petcloud.user.domain.vo.FeaturedContentVO;
import com.petcloud.user.domain.vo.HealthReminderVO;
import com.petcloud.user.domain.vo.HomeSummaryVO;
import com.petcloud.user.domain.vo.TaskVO;
import com.petcloud.user.domain.vo.UserPointsVO;
import com.petcloud.user.domain.vo.UserPetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeSummaryServiceImpl implements HomeSummaryService {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("M月d日");

    private final PetService petService;
    private final HealthReminderService healthReminderService;
    private final TaskService taskService;
    private final FeaturedContentService featuredContentService;

    @Override
    public HomeSummaryVO getHomeSummary(Long userId) {
        boolean loggedIn = userId != null;
        HomeSummaryVO.PetCardVO petCard = loggedIn ? getCurrentPetCard(userId) : null;
        HomeSummaryVO.ReminderSummaryVO reminderSummary = loggedIn ? buildReminderSummary(userId) : null;
        HomeSummaryVO.TodayCareSummaryVO todayCareSummary = loggedIn ? buildTodayCareSummary(userId) : null;

        return HomeSummaryVO.builder()
                .loggedIn(loggedIn)
                .petCard(petCard)
                .reminderSummary(reminderSummary)
                .todayCareSummary(todayCareSummary)
                .featuredContents(buildFeaturedContents())
                .build();
    }

    @Override
    public HomeSummaryVO.PetCardVO getCurrentPetCard(Long userId) {
        List<UserPetVO> pets = petService.getPetList(userId);
        if (pets.isEmpty()) {
            return null;
        }
        List<HomeSummaryVO.PetCardItemVO> items = pets.stream()
                .map(this::toPetCardItem)
                .toList();
        return HomeSummaryVO.PetCardVO.builder()
                .currentPetId(items.get(0).getPetId())
                .petCount(items.size())
                .currentIndex(0)
                .pets(items)
                .build();
    }

    private HomeSummaryVO.PetCardItemVO toPetCardItem(UserPetVO pet) {
        return HomeSummaryVO.PetCardItemVO.builder()
                .petId(pet.getId())
                .name(pet.getName())
                .avatarUrl(pet.getAvatarUrl())
                .breed(pet.getBreed())
                .ageText(buildAgeText(pet))
                .statusSummary(buildStatusSummary(pet))
                .signature(buildSignature(pet))
                .build();
    }

    private String buildAgeText(UserPetVO pet) {
        if (pet.getBirthday() == null) {
            return "年龄待补充";
        }
        Period period = Period.between(pet.getBirthday(), LocalDate.now());
        if (period.getYears() > 0) {
            return period.getYears() + "岁" + Math.max(period.getMonths(), 0) + "个月";
        }
        if (period.getMonths() > 0) {
            return period.getMonths() + "个月";
        }
        return "幼年期";
    }

    private String buildStatusSummary(UserPetVO pet) {
        if (pet.getHealthStatus() != null && !pet.getHealthStatus().isBlank()) {
            return "当前状态" + pet.getHealthStatus() + "，建议继续保持日常观察。";
        }
        return "今日状态待补充，建议记录饮食、饮水和精神变化。";
    }

    private String buildSignature(UserPetVO pet) {
        if (pet.getMotto() != null && !pet.getMotto().isBlank()) {
            return pet.getMotto();
        }
        if (pet.getPersonality() != null && !pet.getPersonality().isBlank()) {
            return pet.getPersonality();
        }
        return "记录日常状态，建立连续健康档案。";
    }

    private HomeSummaryVO.ReminderSummaryVO buildReminderSummary(Long userId) {
        List<HealthReminderVO> reminders = healthReminderService.getList(userId).stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsDone()))
                .toList();
        if (reminders.isEmpty()) {
            return HomeSummaryVO.ReminderSummaryVO.builder()
                    .hasPending(false)
                    .pendingCount(0)
                    .nextTitle("暂无待处理提醒")
                    .nextPetName(null)
                    .nextRemindDateText("可前往创建新的健康提醒")
                    .build();
        }
        HealthReminderVO next = reminders.get(0);
        return HomeSummaryVO.ReminderSummaryVO.builder()
                .hasPending(true)
                .pendingCount(reminders.size())
                .nextTitle(next.getTitle())
                .nextPetName(next.getPetName())
                .nextRemindDateText(next.getRemindDate() == null ? "时间待定" : next.getRemindDate().format(MONTH_DAY_FORMATTER))
                .build();
    }

    private HomeSummaryVO.TodayCareSummaryVO buildTodayCareSummary(Long userId) {
        List<TaskVO> tasks = taskService.getTodayTasks(userId);
        UserPointsVO points = taskService.getUserPoints(userId);
        int completedCount = (int) tasks.stream().filter(TaskVO::getCompleted).count();
        int totalCount = tasks.size();
        int completionRate = totalCount == 0 ? 0 : Math.round((completedCount * 100f) / totalCount);

        return HomeSummaryVO.TodayCareSummaryVO.builder()
                .totalCount(totalCount)
                .completedCount(completedCount)
                .totalPoints(points.getTotal())
                .completionRate(completionRate)
                .tasks(tasks.stream().limit(3).map(task -> HomeSummaryVO.TodayCareTaskVO.builder()
                        .taskId(task.getId())
                        .code(task.getCode())
                        .name(task.getName())
                        .desc(task.getDesc())
                        .icon(task.getIcon())
                        .points(task.getPoints())
                        .completed(task.getCompleted())
                        .build()).toList())
                .build();
    }

    private List<FeaturedContentVO> buildFeaturedContents() {
        return featuredContentService.getPublishedContents(3).stream()
                .map(this::toFeaturedContent)
                .toList();
    }

    private FeaturedContentVO toFeaturedContent(FeaturedContentVO item) {
        return FeaturedContentVO.builder()
                .id(item.getId())
                .draftId(item.getDraftId())
                .title(item.getTitle())
                .summary(item.getSummary())
                .coverUrl(item.getCoverUrl())
                .tag(item.getTag())
                .reasonLabel(item.getReasonLabel())
                .targetPage(item.getTargetPage())
                .targetId(item.getTargetId())
                .positionNo(item.getPositionNo())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .build();
    }
}
