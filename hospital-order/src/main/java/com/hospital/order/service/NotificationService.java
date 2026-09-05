package com.hospital.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知服务
 * 当前为模拟实现：接入阿里云/腾讯云短信SDK后替换内部实现即可，调用方无感知
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * 发送预约成功短信
     */
    public void sendAppointmentSuccessSms(String phone, String patientName, String doctorName, String workDate, String orderNo) {
        if (phone == null || phone.isEmpty()) {
            log.warn("手机号为空，跳过短信发送: orderNo={}", orderNo);
            return;
        }
        String content = String.format(
                "【在线挂号】尊敬的%s，您已成功预约%s（就诊日期：%s），订单号%s，请携带身份证按时就诊。",
                patientName, doctorName, workDate, orderNo);
        // 模拟短信通道：真实实现为短信SDK调用
        log.info("[模拟短信] 发送至 {}: {}", phone, content);
    }
}
