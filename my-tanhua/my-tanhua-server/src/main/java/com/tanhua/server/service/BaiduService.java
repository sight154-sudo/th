package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.UserLocationApi;
import org.springframework.stereotype.Service;

/**
 * @author: tang
 * @date: Create in 11:53 2021/8/15
 * @description:
 */
@Service
public class BaiduService {

    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;

    public Boolean updateLocation(Double longitude, Double latitude, String address) {
        User user = UserThreadLocal.getUser();
        return userLocationApi.updateUserLocation(user.getId(),longitude,latitude,address);
    }
}
