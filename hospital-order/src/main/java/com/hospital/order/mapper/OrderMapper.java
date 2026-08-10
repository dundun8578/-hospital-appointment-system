package com.hospital.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 *
 * @author hospital
 * @date 2024-01-15
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
