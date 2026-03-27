package com.petcloud.shop.interfaces.controller.product;

import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.FileUploadUtil;
import com.petcloud.shop.domain.enums.ShopRespType;
import com.petcloud.shop.domain.service.ProductService;
import com.petcloud.shop.domain.vo.ProductCategoryVO;
import com.petcloud.shop.domain.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 商品控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final FileUploadUtil fileUploadUtil;

    /**
     * 获取商品分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Response<List<ProductCategoryVO>> getCategoryList() {
        log.info("获取商品分类列表");
        List<ProductCategoryVO> categories = productService.getCategoryList();
        return Response.succeed(categories);
    }

    /**
     * 获取商品列表（分页）- BE-0.1
     *
     * @param categoryId 分类ID（可选）
     * @param page       页码，默认1
     * @param pageSize   每页数量，默认10，最大50
     * @return 分页商品列表
     */
    @GetMapping("/list")
    public Response<PageVO<ProductVO>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("获取商品列表，categoryId: {}, page: {}, pageSize: {}", categoryId, page, pageSize);
        PageVO<ProductVO> result = productService.getProductPage(categoryId, page, pageSize);
        return Response.succeed(result);
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Response<ProductVO> getProductDetail(@PathVariable Long id) {
        log.info("获取商品详情，productId: {}", id);
        ProductVO product = productService.getProductDetail(id);
        return Response.succeed(product);
    }

    /**
     * 上传商品图片
     *
     * @param files 图片文件
     * @return 图片URL列表
     */
    @PostMapping("/upload/images")
    public Response<List<String>> uploadProductImages(@RequestParam("files") MultipartFile[] files) {
        try {
            List<String> imageUrls = fileUploadUtil.uploadFiles(files);
            return Response.succeed(imageUrls);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Response.error(ShopRespType.PRODUCT_IMAGE_UPLOAD_FAILED, e.getMessage());
        }
    }
}
