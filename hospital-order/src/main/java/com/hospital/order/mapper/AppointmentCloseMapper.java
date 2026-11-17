package com.hospital.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AppointmentCloseMapper {
    @Update("UPDATE appointment SET status=4 WHERE appointment_id=#{id} AND schedule_id=#{scheduleId} AND status=1 AND deleted=0")
    int closePending(@Param("id") Long id, @Param("scheduleId") Long scheduleId);
}
