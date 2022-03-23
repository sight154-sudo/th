package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: tang
 * @date: Create in 10:36 2021/8/5
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodayBest {

    private Long id;//推荐用户的id
    private String avatar;//头像
    private String nickname;//呢称
    private String gender;
    private Integer age;
    private String[] tags;//标签
    private Long fateValue;//缘分值

}
