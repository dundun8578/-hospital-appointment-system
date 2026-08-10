package com.hospital.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.order.entity.Order;
import com.hospital.order.vo.OrderVO;

/**
 * 订单服务接口
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 订单ID
     */
    Long createOrder(Order order);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单VO
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 根据预约ID查询订单
     *
     * @param appointmentId 预约ID
     * @return 订单VO
     */
    OrderVO getOrderByAppointmentId(Long appointmentId);

    /**
     * 更新订单支付状态
     *
     * @param orderId   订单ID
     * @param payStatus 支付状态
     * @param payType   支付方式
     * @return 是否成功
     */
    boolean updatePayStatus(Long orderId, Integer payStatus, Integer payType);
}
