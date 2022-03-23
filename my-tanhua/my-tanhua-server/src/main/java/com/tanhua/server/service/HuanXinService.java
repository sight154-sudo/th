package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.common.mapper.HuanXinUserMapper;
import com.tanhua.common.pojo.HuanXinUser;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.HuanXinApi;
import com.tanhua.server.vo.HuanXinUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: tang
 * @date: Create in 11:44 2021/8/13
 * @description:
 */
@Service
public class HuanXinService {

    @Reference(version = "1.0.0")
    private HuanXinApi huanXinApi;

    @Autowired
    private HuanXinUserMapper huanXinUserMapper;

    public HuanXinUserVo queryHuanXinUser(){
        User user = UserThreadLocal.getUser();
        HuanXinUser huanXinUser = huanXinApi.queryHuanXinUser(user.getId());
        HuanXinUserVo hxVo = new HuanXinUserVo();
        hxVo.setUsername(huanXinUser.getUsername());
        hxVo.setPassword(huanXinUser.getPassword());
        return hxVo;
    }

    public HuanXinUser queryHuanXinUserByUid(Long userId) {
        LambdaQueryWrapper<HuanXinUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HuanXinUser::getUserId,userId);
        return this.huanXinUserMapper.selectOne(wrapper);
    }
}
