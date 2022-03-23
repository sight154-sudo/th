package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: tang
 * @date: Create in 22:57 2021/8/5
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendUserQueryParam {
    private Integer page = 1; //当前页数
    private Integer pagesize = 10; //页尺寸
    private String gender; //性别 man woman
    private String lastLogin; //近期登陆时间
    private Integer age; //年龄
    private String city; //居住地
    private String education; //学历
}
