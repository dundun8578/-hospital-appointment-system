package com.hospital.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.doctor.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班Mapper接口
 *
 * @author hospital
 * @date 2024-01-15
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    @Update("""
            UPDATE schedule
            SET available_count = available_count - 1
            WHERE schedule_id = #{scheduleId}
              AND available_count > 0
              AND status = 1
              AND deleted = 0
            """)
    int deductAvailableStock(@Param("scheduleId") Long scheduleId);

    /**
     * 根据医生ID和日期范围查询排班
     *
     * @param doctorId   医生ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 排班列表
     */
    List<Schedule> selectByDoctorAndDateRange(@Param("doctorId") Long doctorId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 根据医院ID和日期查询排班
     *
     * @param hospitalId 医院ID
     * @param workDate   排班日期
     * @return 排班列表
     */
    List<Schedule> selectByHospitalAndDate(@Param("hospitalId") Long hospitalId,
                                            @Param("workDate") LocalDate workDate);

    /**
     * 根据科室ID和日期查询排班
     *
     * @param departmentId 科室ID
     * @param workDate     排班日期
     * @return 排班列表
     */
    List<Schedule> selectByDepartmentAndDate(@Param("departmentId") Long departmentId,
                                              @Param("workDate") LocalDate workDate);
}
