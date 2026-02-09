package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcloud.user.domain.entity.Admin;
import com.petcloud.user.domain.service.AdminService;
import com.petcloud.user.infrastructure.persistence.mapper.AdminMapper;
import org.springframework.stereotype.Service;

/**
 * 管理员应用服务实现类
 *
 * @author luohao
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
}
