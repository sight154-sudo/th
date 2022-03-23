package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.dubbo.server.vo.UserLocationVo;

/**
 * @author: tang
 * @date: Create in 10:20 2021/8/15
 * @description:
 */
public interface UserLocationApi {

    /**
     * 更新用户的地理位置
     *
     * @param userId
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    Boolean updateUserLocation(Long userId, Double longitude, Double latitude, String address);

    /**
     * 查询用户的地理位置
     *
     * @param userId
     * @return
     */
    UserLocationVo queryUserLocation(Long userId);

    /**
     * 搜索附近的人
     * @param longitude
     * @param latitude
     * @param distance
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Double distance, Integer page, Integer pageSize);
}