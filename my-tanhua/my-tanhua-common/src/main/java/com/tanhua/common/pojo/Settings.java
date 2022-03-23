package com.tanhua.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings extends BasePojo {
    private Long id;
    private Long userId;
    private Boolean likeNotification = true; //推送喜欢通知
    private Boolean pinglunNotification = true;//推送评论通知
    private Boolean gonggaoNotification = true;//推送公告通知
}