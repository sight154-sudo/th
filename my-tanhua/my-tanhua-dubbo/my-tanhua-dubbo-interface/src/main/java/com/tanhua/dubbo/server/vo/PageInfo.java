package com.tanhua.dubbo.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author: tang
 * @date: Create in 22:51 2021/8/4
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo<T> implements Serializable {

    //总记录数
    private Integer total = 0;
    //当前页
    private Integer currentPage = 0;
    //每页总条数
    private Integer pageSize = 0;

    //数据列表
    private List<T> records = Collections.EMPTY_LIST;

}
