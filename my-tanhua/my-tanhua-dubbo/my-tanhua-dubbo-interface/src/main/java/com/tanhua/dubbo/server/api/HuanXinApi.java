package com.tanhua.dubbo.server.api;

import com.tanhua.common.pojo.HuanXinUser;
import com.tanhua.dubbo.server.enums.HuanXinMessageType;

/**
 * @author: tang
 * @date: Create in 22:45 2021/8/12
 * @description:
 */
public interface HuanXinApi {

    /**
     * 获取环信管理员token信息
     * @return
     */
    String getToken();

    /**
     * 注册单个信息到环信
     * @return
     */
    Boolean register(Long userId);

    /**
     * 获取单个用户的信息
     * @param userId
     * @return
     */
    HuanXinUser queryHuanXinUser(Long userId);

    /**
     * 获取用户信息通过用户名
     * @param username
     * @return
     */
    HuanXinUser queryHuanXinUserByUsername(String username);

    /**
     * 添加好友（双向好友关系）
     *
     * @param userId   自己的id
     * @param friendId 好友的id
     * @return
     */
    Boolean addUserFriend(Long userId, Long friendId);

    /**
     * 删除好友关系（双向删除）
     *
     * @param userId   自己的id
     * @param friendId 好友的id
     * @return
     */
    Boolean removeUserFriend(Long userId, Long friendId);


    /**
     * 回复陌生人问题
     * @param targetUserName
     * @param huanXinMessageType
     * @param msg
     * @return
     */
    Boolean sendMsgFromAdmin(String targetUserName, HuanXinMessageType huanXinMessageType, String msg);


}
