package com.petcloud.common.core.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 分页查询参数
 *
 * @author luohao
 */
@Setter
@Getter
public class PageQuery implements IQuery {
    private long pageNum = 1L;
    private long pageSize = 10L;
    private boolean needCount = true;
    private boolean needContent = true;

    public static PageQuery of() {
        return (new PageQuery()).format();
    }

    public static PageQuery of(long pageSize, long pageIndex) {
        return of(pageSize, pageIndex, true, true).format();
    }

    public static PageQuery of(long pageSize, long pageNum, boolean needCount, boolean needContent) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageNum);
        pageQuery.setPageSize(pageSize);
        pageQuery.setNeedCount(needCount);
        pageQuery.setNeedContent(needContent);
        return pageQuery;
    }

    public PageQuery format() {
        this.pageNum = Math.max(this.pageNum, 1L);
        this.pageSize = Math.max(this.pageSize, 1L);
        return this;
    }

    @Override
    public String toString() {
        return "PageQuery(pageNum=" + this.getPageNum() + ", pageSize=" + this.getPageSize() +
                ", needCount=" + this.isNeedCount() + ", needContent=" + this.isNeedContent() + ")";
    }
}
