package com.petcloud.user.application.impl;

import com.petcloud.user.domain.service.FeaturedContentService;
import com.petcloud.user.domain.service.HealthReminderService;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.service.TaskService;
import com.petcloud.user.domain.vo.FeaturedContentVO;
import com.petcloud.user.domain.vo.HealthReminderVO;
import com.petcloud.user.domain.vo.HomeSummaryVO;
import com.petcloud.user.domain.vo.TaskVO;
import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.domain.vo.UserPointsVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeSummaryServiceImplTest {

    @Test
    void shouldBuildHomeSummaryWithPet() {
        HomeSummaryServiceImpl service = new HomeSummaryServiceImpl(
                proxy(PetService.class, (method, args) -> List.of(UserPetVO.builder()
                        .id(1L)
                        .name("团子")
                        .breed("英短")
                        .birthday(LocalDate.now().minusYears(2))
                        .healthStatus("稳定")
                        .motto("慢热小猫")
                        .build())),
                proxy(HealthReminderService.class, (method, args) -> List.of(
                        HealthReminderVO.builder().id(1L).title("疫苗复查").petName("团子")
                                .remindDate(LocalDate.now()).isDone(false).build()
                )),
                proxy(TaskService.class, (method, args) -> {
                    if ("getTodayTasks".equals(method.getName())) {
                        return List.of(
                                TaskVO.builder().id(1L).name("记录饮食").completed(true).points(5).build(),
                                TaskVO.builder().id(2L).name("记录饮水").completed(false).points(5).build()
                        );
                    }
                    if ("getUserPoints".equals(method.getName())) {
                        return UserPointsVO.builder().total(80).build();
                    }
                    throw new UnsupportedOperationException(method.getName());
                }),
                proxy(FeaturedContentService.class, (method, args) -> List.of(
                        FeaturedContentVO.builder().id(10L).title("春季护理").summary("护理摘要").build()
                ))
        );

        HomeSummaryVO result = service.getHomeSummary(1L);

        assertTrue(result.getLoggedIn());
        assertNotNull(result.getPetCard());
        assertEquals(1, result.getPetCard().getPetCount());
        assertEquals("慢热小猫", result.getPetCard().getPets().get(0).getSignature());
        assertTrue(result.getReminderSummary().getHasPending());
        assertEquals(2, result.getTodayCareSummary().getTotalCount());
        assertEquals(1, result.getFeaturedContents().size());
    }

    @Test
    void shouldBuildHomeSummaryWithoutPet() {
        HomeSummaryServiceImpl service = new HomeSummaryServiceImpl(
                proxy(PetService.class, (method, args) -> List.of()),
                proxy(HealthReminderService.class, (method, args) -> List.of()),
                proxy(TaskService.class, (method, args) -> {
                    if ("getTodayTasks".equals(method.getName())) {
                        return List.of();
                    }
                    if ("getUserPoints".equals(method.getName())) {
                        return UserPointsVO.builder().total(0).build();
                    }
                    throw new UnsupportedOperationException(method.getName());
                }),
                proxy(FeaturedContentService.class, (method, args) -> List.of())
        );

        HomeSummaryVO result = service.getHomeSummary(1L);

        assertTrue(result.getLoggedIn());
        assertNull(result.getPetCard());
        assertFalse(result.getReminderSummary().getHasPending());
        assertEquals(0, result.getTodayCareSummary().getTotalCount());
        assertTrue(result.getFeaturedContents().isEmpty());
    }

    @Test
    void shouldBuildPublicHomeSummaryWhenLoggedOut() {
        HomeSummaryServiceImpl service = new HomeSummaryServiceImpl(
                proxy(PetService.class, unsupported()),
                proxy(HealthReminderService.class, unsupported()),
                proxy(TaskService.class, unsupported()),
                proxy(FeaturedContentService.class, (method, args) -> List.of(
                        FeaturedContentVO.builder().id(10L).title("公开内容").summary("内容").build()
                ))
        );

        HomeSummaryVO result = service.getHomeSummary(null);

        assertFalse(result.getLoggedIn());
        assertNull(result.getPetCard());
        assertNull(result.getReminderSummary());
        assertNull(result.getTodayCareSummary());
        assertEquals(1, result.getFeaturedContents().size());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> invocation.invoke(method, args)
        );
    }

    private Invocation unsupported() {
        return (method, args) -> {
            throw new UnsupportedOperationException(method.getName());
        };
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
