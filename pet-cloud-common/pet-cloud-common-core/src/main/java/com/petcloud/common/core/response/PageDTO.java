package com.petcloud.common.core.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petcloud.common.core.utils.NullSafeUtil;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页响应结果
 *
 * @author luohao
 */
@Data
public class PageDTO<V> {
    public static final PageDTO<Object> EMPTY_PAGE = of(Collections.emptyList(), 0L, 0L, 0L);

    private long totalNum = 0L;
    private long totalPage = 0L;
    private long totalPages = 0L;
    private long limit = 10L;
    private long currentPage = 1L;
    private List<V> list;

    public List<V> getList() {
        return NullSafeUtil.list(this.list);
    }

    public void setLimit(long limit) {
        this.limit = Math.max(limit, 1L);
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = Math.max(currentPage, 1L);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.list == null || this.list.isEmpty();
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !this.isEmpty();
    }

    public static <V> PageDTO<V> of(List<V> list, long totalCount, long pageSize, long pageNum) {
        PageDTO<V> pageDTO = new PageDTO<V>();
        pageDTO.setTotalNum(totalCount);
        pageDTO.setLimit(pageSize);
        pageDTO.setCurrentPage(pageNum);
        pageDTO.setList(list);
        if (pageSize != 0L) {
            long pages = totalCount / pageSize;
            if (totalCount % pageSize != 0L) {
                ++pages;
            }

            pageDTO.setTotalPage(pages);
            pageDTO.setTotalPages(pages);
        }

        return pageDTO;
    }

    @Override
    public String toString() {
        return "PageDTO(totalNum=" + this.getTotalNum() + ", totalPage=" + this.getTotalPage() +
                ", totalPages=" + this.getTotalPages() + ", limit=" + this.getLimit() +
                ", currentPage=" + this.getCurrentPage() + ", list=" + this.getList() + ")";
    }
}
