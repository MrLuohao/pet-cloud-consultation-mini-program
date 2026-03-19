package com.petcloud.user.application.impl;

import com.petcloud.user.domain.entity.UserAddress;
import com.petcloud.user.domain.service.AddressService;
import com.petcloud.user.domain.vo.UserAddressVO;
import com.petcloud.user.infrastructure.persistence.mapper.UserAddressMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AddressServiceImplTest {

    @Test
    void shouldExposeMapAndParseFieldsOnAddressView() {
        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(9L);
        address.setContactName("张宁");
        address.setContactPhone("13812345678");
        address.setProvince("上海市");
        address.setCity("上海市");
        address.setDistrict("浦东新区");
        address.setDetailAddress("世纪大道1188号2栋1702");
        address.setIsDefault(1);
        address.setLongitude(new BigDecimal("121.506300"));
        address.setLatitude(new BigDecimal("31.245520"));
        address.setBusinessArea("陆家嘴");
        address.setDoorNo("2栋1702");
        address.setRawText("张宁 13812345678 上海市浦东新区世纪大道1188号2栋1702");
        address.setParsedName("张宁");
        address.setParsedPhone("13812345678");
        address.setMapAddress("上海市浦东新区世纪大道1188号");

        AddressService service = new AddressServiceImpl(
                proxy(UserAddressMapper.class, (method, args) -> {
                    if ("selectList".equals(method.getName())) {
                        return List.of(address);
                    }
                    if ("selectOne".equals(method.getName()) || "selectById".equals(method.getName())) {
                        return address;
                    }
                    return null;
                })
        );

        UserAddressVO result = service.getDefaultAddress(9L);

        assertNotNull(result);
        assertEquals(new BigDecimal("121.506300"), result.getLongitude());
        assertEquals(new BigDecimal("31.245520"), result.getLatitude());
        assertEquals("陆家嘴", result.getBusinessArea());
        assertEquals("2栋1702", result.getDoorNo());
        assertEquals("张宁", result.getParsedName());
        assertEquals("13812345678", result.getParsedPhone());
        assertEquals("上海市浦东新区世纪大道1188号", result.getMapAddress());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> invocation.invoke(method, args)
        );
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
