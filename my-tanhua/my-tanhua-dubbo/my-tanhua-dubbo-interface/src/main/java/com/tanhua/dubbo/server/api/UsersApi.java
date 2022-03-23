package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Users;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 19:02 2021/8/13
 * @description:
 */
public interface UsersApi {

    /**
     * 添加联系人
     * @param userId
     * @param friendId
     */
    void saveUsers(Long userId,Long friendId);

    /**
     * 删除好友数据
     * @param userId
     * @param friendId
     */
    void removeUsers(Long userId,Long friendId);

    /**
     * 根据用户id查询用户
     * @param userId
     * @return
     */
    List<Users> queryUserList(Long userId);

    /**
     * 根据用户id分页查询
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Users> queryUsersList(Long userId, Integer page, Integer pageSize);
}
