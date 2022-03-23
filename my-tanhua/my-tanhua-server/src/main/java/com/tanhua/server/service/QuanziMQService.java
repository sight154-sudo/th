package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.QuanZiApi;
import com.tanhua.dubbo.server.pojo.Publish;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 19:02 2021/8/18
 * @description:
 */
@Service
@Slf4j
public class QuanziMQService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    /**
     * 发布动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean publishMsg(String publishId) {
        return this.sendMsg(publishId, 1);
    }

    /**
     * 浏览动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean queryPublishMsg(String publishId) {
        return this.sendMsg(publishId, 2);
    }

    /**
     * 点赞动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean likePublishMsg(String publishId) {
        return this.sendMsg(publishId, 3);
    }

    /**
     * 取消点赞动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean disLikePublishMsg(String publishId) {
        return this.sendMsg(publishId, 6);
    }

    /**
     * 喜欢动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean lovePublishMsg(String publishId) {
        return this.sendMsg(publishId, 4);
    }

    /**
     * 取消喜欢动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean disLovePublishMsg(String publishId) {
        return this.sendMsg(publishId, 7);
    }

    /**
     * 评论动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean commentPublishMsg(String publishId) {
        return this.sendMsg(publishId, 5);
    }


    /**
     * 发送消息
     * 使用type区分发送类型 1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点赞，7-取消喜欢
     * @param publishId
     * @param type
     * @return
     */
    private Boolean sendMsg(String publishId,Integer type){
        try {
            User user = UserThreadLocal.getUser();
            //查询动态信息
            Publish publish = this.quanZiApi.getPublishById(publishId);
            //设置发送的消息
            Map<String,Object> msg = new HashMap<>();
            msg.put("userId", user.getId());
            msg.put("date", System.currentTimeMillis());
            msg.put("publishId", publishId);
            msg.put("pid", publish.getPid());
            msg.put("type", type);
            this.rocketMQTemplate.convertAndSend("tanhua-quanzi",msg);
            return true;
        } catch (MessagingException e) {
            log.error("发送消息失败! publishId = " + publishId + ", type = " + type, e);
        }
        return false;
    }

}
