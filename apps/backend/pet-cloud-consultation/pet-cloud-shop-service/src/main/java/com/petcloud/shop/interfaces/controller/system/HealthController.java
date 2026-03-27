package com.petcloud.shop.interfaces.controller.system;

import com.petcloud.common.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商城服务健康检查控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/v1/health")
public class HealthController {

    /**
     * 健康检查接口
     */
    @GetMapping
    public Response<?> health() {
        return Response.succeed("商城服务运行正常");
    }
}
