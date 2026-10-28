package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("""
            <script>
            SELECT * FROM appointment
            WHERE user_id = #{userId} AND deleted = 0
            <if test='status != null'>AND status = #{status}</if>
            ORDER BY created_time DESC
            LIMIT #{offset}, #{limit}
            </script>
            """)
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
    @Select("""
            <script>
            SELECT COUNT(*) FROM appointment
            WHERE user_id = #{userId} AND deleted = 0
            <if test='status != null'>AND status = #{status}</if>
            </script>
            """)
    long countAppointments(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户是否已预约该排班
     *
     * @param userId    用户ID
     * @param scheduleId 排班ID
     * @return 预约记录数
     */
    @Select("""
            SELECT COUNT(*) FROM appointment
            WHERE user_id = #{userId}
              AND schedule_id = #{scheduleId}
              AND status IN (1, 2)
              AND deleted = 0
            """)
    int checkUserAppointed(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);
}
