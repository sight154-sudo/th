package com.tanhua.dubbo.server.api;


import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.common.mapper.HuanXinUserMapper;
import com.tanhua.common.pojo.HuanXinUser;
import com.tanhua.dubbo.server.config.HuanXinConfig;
import com.tanhua.dubbo.server.enums.HuanXinMessageType;
import com.tanhua.dubbo.server.service.RequestService;
import com.tanhua.dubbo.server.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;

/**
 * @author: tang
 * @date: Create in 22:45 2021/8/12
 * @description:
 */
@Service(version = "1.0.0")
@Slf4j
public class HuanXinApiImpl implements HuanXinApi{

    @Autowired
    private TokenService tokenService;
    @Autowired
    private HuanXinConfig config;
    @Autowired
    private RequestService requestService;

    @Autowired
    private HuanXinUserMapper huanXinUserMapper;

    @Override
    public String getToken() {
        return tokenService.getToken();
    }

    @Override
    public Boolean register(Long userId) {
        //获取token
        String url = config.getUrl()+config.getOrgName()+"/"+config.getAppName()+"/users";
        HuanXinUser huanXinUser = new HuanXinUser();
        huanXinUser.setUsername("HX_" + userId);  // 用户名
        huanXinUser.setPassword(IdUtil.simpleUUID());//生成随机密码
        String body = JSONUtil.toJsonStr(Arrays.asList(huanXinUser));
        HttpResponse response = requestService.execute(url, body, Method.POST);
        if(response.isOk()){
            //注册成功后 保存到数据库中
            huanXinUser.setUserId(userId);
            huanXinUser.setCreated(new Date());
            huanXinUser.setUpdated(huanXinUser.getCreated());
            huanXinUserMapper.insert(huanXinUser);
            return true;
        }
        return false;
    }

    @Override
    public HuanXinUser queryHuanXinUser(Long userId) {
        LambdaQueryWrapper<HuanXinUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HuanXinUser::getUserId,userId);
        return huanXinUserMapper.selectOne(wrapper);
    }

    @Override
    public HuanXinUser queryHuanXinUserByUsername(String username) {
        LambdaQueryWrapper<HuanXinUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HuanXinUser::getUsername,username);
        return huanXinUserMapper.selectOne(wrapper);
    }


    @Override
    public Boolean addUserFriend(Long userId, Long friendId) {
        String url = config.getUrl()+config.getOrgName()+"/"+config.getAppName()
                +"/users/"+"HX_"+userId+"/contacts/users/"+"HX_"+friendId;
        HttpResponse response = this.requestService.execute(url, null, Method.POST);
        if(!response.isOk()){
            return false;
        }
        return true;
    }

    /**
     * 解除好友关系
     * @param userId   自己的id
     * @param friendId 好友的id
     * @return
     */
    @Override
    public Boolean removeUserFriend(Long userId, Long friendId) {
        String url = config.getUrl()+config.getOrgName()+"/"+config.getAppName()
                +"/users/"+"HX_"+userId+"/contacts/users/"+"HX_"+friendId;
        HttpResponse response = this.requestService.execute(url, null, Method.DELETE);
        if(!response.isOk()){
            return false;
        }
        return true;
    }

    /**
     * 回复陌生人问题
     * @param targetUserName  发送的目标名称
     * @param huanXinMessageType 消息类型
     * @param msg  消息
     * @return
     */
    @Override
    public Boolean sendMsgFromAdmin(String targetUserName, HuanXinMessageType huanXinMessageType, String msg) {
        //http://a1.easemob.com/easemob-demo/testapp/messages'
        String url = config.getUrl()+config.getOrgName()+"/"+config.getAppName()
                +"/messages";
        //request-body
        // '{"target_type": "users","target": ["user2","user3"],"msg": {"type": "txt","msg": "testmessage"},"from": "user1"}'
        String body = JSONUtil.createObj()
                .set("target_type", "users")
                .set("target", JSONUtil.createArray().set(targetUserName))
                .set("msg", JSONUtil.createObj()
                        .set("type", huanXinMessageType.getType())
                        .set("msg", msg)).toString();
        //表示消息发送者;无此字段Server会默认设置为“from”:“admin”，有from字段但值为空串(“”)时请求失败
//                .set("from","");
        try {
            HttpResponse response = this.requestService.execute(url, body, Method.POST);
            if(response.isOk()){
                return true;
            }
        } catch (Exception e) {
            log.error("回复陌生人消息失败~targetName = "+targetUserName,e);
        }
        return false;
    }
}
