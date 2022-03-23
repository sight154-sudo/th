package com.tanhua.common.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: tang
 * @date: Create in 9:49 2021/8/5
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends BasePojo{

    private Long id;
    private String mobile;
    @JsonIgnore
    private String password;//密码 json序列化时忽略
}