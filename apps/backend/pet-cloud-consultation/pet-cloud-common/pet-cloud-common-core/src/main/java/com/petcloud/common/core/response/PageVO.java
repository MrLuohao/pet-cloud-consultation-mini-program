package com.petcloud.common.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 分页响应 VO（对齐前后端 API 契约规范）
 * 字段命名遵循 api-contracts.md，与前端 hasMore 分页机制一致
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    /** 总记录数 */
    private long total;

    /** 当前页数据列表 */
    private List<T> list;

    /** 是否有更多数据（前端上拉加载判断依据） */
    private boolean hasMore;

    /** 当前页码（从 1 开始） */
    private int page;

    /** 每页数量 */
    private int pageSize;

    /**
     * 工厂方法 - 构建分页结果
     *
     * @param list     当前页数据
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页数量
     */
    public static <T> PageVO<T> of(List<T> list, long total, int page, int pageSize) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotal(total);
        vo.setList(list != null ? list : Collections.emptyList());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        // hasMore：当前页已加载数量 < 总数时还有更多
        vo.setHasMore((long) page * pageSize < total);
        return vo;
    }

    /**
     * 空结果
     */
    public static <T> PageVO<T> empty(int page, int pageSize) {
        return of(Collections.emptyList(), 0L, page, pageSize);
    }
}
