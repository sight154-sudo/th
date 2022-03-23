package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.common.mapper.UserInfoMapper;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.server.vo.QuanZiVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author: tang
 * @date: Create in 15:36 2021/8/5
 * @description:
 */
@Service
public class UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    public UserInfo queryUserInfoByUserId(Long userId){
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId,userId);
        return userInfoMapper.selectOne(wrapper);
    }


    public List<UserInfo> queryUserInfoList(QueryWrapper<UserInfo> wrapper) {
        return userInfoMapper.selectList(wrapper);
    }
    public List<UserInfo> queryUserInfoList(LambdaQueryWrapper<UserInfo> wrapper) {
        return userInfoMapper.selectList(wrapper);
    }
    public List<UserInfo> queryUserInfoByUserIds(Collection<?> userIds){
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserInfo::getUserId,userIds);
        return userInfoMapper.selectList(wrapper);
    }

    public void updateUserInfo(UserInfo userInfo){
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId,userInfo.getUserId());
        userInfoMapper.update(userInfo,wrapper);
    }
}
