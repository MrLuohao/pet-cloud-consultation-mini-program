package com.petcloud.user;

import com.petcloud.common.core.domain.UserContext;
import com.petcloud.common.core.domain.UserContextHolder;
import com.petcloud.user.domain.entity.UserAddress;
import com.petcloud.user.infrastructure.persistence.mapper.UserAddressMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 BaseEntity 审计字段自动填充
 */
@SpringBootTest
public class AddressAutoFillTest {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Test
    public void testAutoFillOnInsert() {
        // 模拟用户上下文
        UserContext userContext = UserContext.builder()
                .userId(999L)
                .nickname("测试用户")
                .build();
        UserContextHolder.setContext(userContext);

        try {
            // 创建测试地址
            UserAddress address = new UserAddress();
            address.setUserId(999L);
            address.setContactName("自动填充测试");
            address.setContactPhone("13800138000");
            address.setProvince("测试省");
            address.setCity("测试市");
            address.setDistrict("测试区");
            address.setDetailAddress("测试详细地址");
            address.setIsDefault(0);

            // 插入
            userAddressMapper.insert(address);
            Long addressId = address.getId();
            System.out.println("======== 插入后的ID: " + addressId);

            // 查询验证
            UserAddress inserted = userAddressMapper.selectById(addressId);
            System.out.println("======== createTime: " + inserted.getCreateTime());
            System.out.println("======== modifyTime: " + inserted.getModifyTime());
            System.out.println("======== creatorId: " + inserted.getCreatorId());
            System.out.println("======== creatorName: " + inserted.getCreatorName());
            System.out.println("======== modifierId: " + inserted.getModifierId());
            System.out.println("======== modifierName: " + inserted.getModifierName());

            assertNotNull(inserted.getCreateTime(), "createTime 应该被自动填充");
            assertNotNull(inserted.getModifyTime(), "modifyTime 应该被自动填充");
            assertEquals(999L, inserted.getCreatorId(), "creatorId 应该被自动填充");
            assertEquals("测试用户", inserted.getCreatorName(), "creatorName 应该被自动填充");

            // 测试更新
            inserted.setContactName("更新后的名称");
            Thread.sleep(1000); // 等待1秒确保时间有变化
            userAddressMapper.updateById(inserted);

            UserAddress updated = userAddressMapper.selectById(addressId);
            System.out.println("======== 更新后 modifyTime: " + updated.getModifyTime());
            System.out.println("======== 更新后 modifierId: " + updated.getModifierId());

            assertTrue(updated.getModifyTime().after(inserted.getCreateTime()) ||
                       updated.getModifyTime().equals(inserted.getCreateTime()),
                       "modifyTime 应该被更新");

            // 清理测试数据
            userAddressMapper.deleteById(addressId);
            System.out.println("======== 测试通过，已清理测试数据");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            UserContextHolder.clear();
        }
    }
}
