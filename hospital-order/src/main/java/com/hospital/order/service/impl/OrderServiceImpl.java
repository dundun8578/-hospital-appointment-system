package com.hospital.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.order.entity.Order;
import com.hospital.order.mapper.OrderMapper;
import com.hospital.order.service.OrderService;
import com.hospital.order.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单服务实现类
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    /**
     * 生成订单号
     * 格式：ORD + 时间戳 + 随机数
     *
     * @return 订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 订单ID
     */
    @Override
    public Long createOrder(Order order) {
        // 生成订单号
        order.setOrderNo(generateOrderNo());
        order.setPayStatus(0); // 待支付
        order.setOrderStatus(1); // 待支付

        baseMapper.insert(order);
        log.info("订单创建成功: orderId={}, orderNo={}", order.getOrderId(), order.getOrderNo());

        return order.getOrderId();
    }

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单VO
     */
    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        Order order = baseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getDeleted, 0)
        );
        return order != null ? convertToVO(order) : null;
    }

    /**
     * 根据预约ID查询订单
     *
     * @param appointmentId 预约ID
     * @return 订单VO
     */
    @Override
    public OrderVO getOrderByAppointmentId(Long appointmentId) {
        Order order = baseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getAppointmentId, appointmentId)
                        .eq(Order::getDeleted, 0)
        );
        return order != null ? convertToVO(order) : null;
    }

    /**
     * 更新订单支付状态
     *
     * @param orderId   订单ID
     * @param payStatus 支付状态
     * @param payType   支付方式
     * @return 是否成功
     */
    @Override
    public boolean updatePayStatus(Long orderId, Integer payStatus, Integer payType) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setPayStatus(payStatus);
        if (payType != null) {
            order.setPayType(payType);
        }
        if (payStatus == 1) {
            order.setPayTime(java.time.LocalDateTime.now());
            order.setOrderStatus(2); // 已完成
        }
        return baseMapper.updateById(order) > 0;
    }

    /**
     * 实体转VO
     *
     * @param order 订单实体
     * @return 订单VO
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }
}
