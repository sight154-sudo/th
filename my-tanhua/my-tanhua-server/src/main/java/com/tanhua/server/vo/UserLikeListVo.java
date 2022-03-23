package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author king
 * 喜欢，粉丝，谁看过我，互相喜欢列表结果集
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLikeListVo {

    private Long id;
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String city;
    private String education;
    private Integer marriage; //婚姻状态（0未婚，1已婚）
    private Integer matchRate; //匹配度
    private Boolean alreadyLove; //是否喜欢ta

}