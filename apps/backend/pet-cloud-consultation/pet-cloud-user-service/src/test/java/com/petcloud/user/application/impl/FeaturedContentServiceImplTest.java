package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.user.domain.entity.FeaturedContentPublish;
import com.petcloud.user.domain.vo.FeaturedContentVO;
import com.petcloud.user.infrastructure.persistence.mapper.FeaturedContentPublishMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeaturedContentServiceImplTest {

    @Test
    void shouldReturnPublishedContentsInPositionOrder() {
        FeaturedContentPublish second = buildPublish(2L, 2, "第二条");
        FeaturedContentPublish first = buildPublish(1L, 1, "第一条");

        FeaturedContentPublishMapper mapper = proxy(FeaturedContentPublishMapper.class, (method, args) -> {
            if ("selectPage".equals(method.getName())) {
                Page<FeaturedContentPublish> page = (Page<FeaturedContentPublish>) args[0];
                page.setRecords(List.of(first, second));
                return page;
            }
            throw new UnsupportedOperationException(method.getName());
        });

        FeaturedContentServiceImpl service = new FeaturedContentServiceImpl(mapper);
        List<FeaturedContentVO> result = service.getPublishedContents(3);

        assertEquals(2, result.size());
        assertEquals("第一条", result.get(0).getTitle());
        assertEquals(1, result.get(0).getPositionNo());
        assertEquals("article", result.get(0).getTargetPage());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> invocation.invoke(method, args)
        );
    }

    private FeaturedContentPublish buildPublish(Long id, int positionNo, String title) {
        FeaturedContentPublish publish = new FeaturedContentPublish();
        publish.setId(id);
        publish.setDraftId(100L + id);
        publish.setTitle(title);
        publish.setSummary(title + "摘要");
        publish.setCoverUrl("https://img/" + id + ".jpg");
        publish.setTag("精选");
        publish.setReasonLabel("今日推荐");
        publish.setTargetPage("article");
        publish.setTargetId(1000L + id);
        publish.setPositionNo(positionNo);
        publish.setStatus(FeaturedContentPublish.Status.PUBLISHED.getCode());
        publish.setStartTime(new Date());
        publish.setIsDeleted(0);
        return publish;
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
