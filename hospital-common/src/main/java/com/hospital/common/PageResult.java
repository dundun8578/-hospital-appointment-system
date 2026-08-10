package com.hospital.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果封装
 *
 * @param <T> 数据类型
 * @author hospital
 * @date 2024-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 构建分页结果
     *
     * @param current 当前页
     * @param size    每页大小
     * @param total   总记录数
     * @param records 数据列表
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        long pages = (total + size - 1) / size;
        return new PageResult<>(current, size, total, pages, records);
    }
}
