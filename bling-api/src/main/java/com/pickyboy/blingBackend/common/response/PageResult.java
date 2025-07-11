package com.pickyboy.blingBackend.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private List<T> records; //当前页数据集合
    private long total; //总记录数

    private Integer page;
    private Integer limit;

    public PageResult( long total,List<T> records) {
        this.records = records;
        this.total = total;
    }
}
