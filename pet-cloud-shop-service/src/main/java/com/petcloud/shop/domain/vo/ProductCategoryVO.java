package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品分类VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ProductCategoryVO vo = new ProductCategoryVO();

        public Builder id(Long id) {
            vo.id = id;
            return this;
        }

        public Builder name(String name) {
            vo.name = name;
            return this;
        }

        public Builder icon(String icon) {
            vo.icon = icon;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            vo.sortOrder = sortOrder;
            return this;
        }

        public ProductCategoryVO build() {
            return vo;
        }
    }
}
