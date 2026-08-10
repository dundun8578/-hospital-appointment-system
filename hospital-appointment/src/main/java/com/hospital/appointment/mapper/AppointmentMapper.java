package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约Mapper接口
 *
 * @author hospital
 * @date 2024-01-15
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 查询用户预约列表
     *
     * @param userId 用户ID
     * @param status 预约状态
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 预约列表
     */
    List<Appointment> selectAppointmentList(@Param("userId") Long userId,
                                            @Param("status") Integer status,
                                            @Param("offset") Integer offset,
                                            @Param("limit") Integer limit);

    /**
     * 统计用户预约数量
     *
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约数量
     */
    long countAppointments(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户是否已预约该排班
     *
     * @param userId    用户ID
     * @param scheduleId 排班ID
     * @return 预约记录数
     */
    int checkUserAppointed(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);
}
