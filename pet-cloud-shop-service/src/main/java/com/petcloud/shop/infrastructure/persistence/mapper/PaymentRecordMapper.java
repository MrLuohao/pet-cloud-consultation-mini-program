package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}
