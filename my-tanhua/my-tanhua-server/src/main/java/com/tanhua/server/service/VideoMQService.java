package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.VideoApi;
import com.tanhua.dubbo.server.pojo.Video;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 21:36 2021/8/18
 * @description:
 */
@Service
@Slf4j
public class VideoMQService {

    @Reference(version = "1.0.0")
    private VideoApi videoApi;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发布小视频消息
     * @return
     */
    public Boolean videoMsg(String videoId) {
        return this.sendMsg(videoId, 1);
    }
    /**
     * 点赞小视频
     * @return
     */
    public Boolean likeVideoMsg(String videoId) {
        return this.sendMsg(videoId, 2);
    }
    /**
     * 取消点赞小视频
     * @return
     */
    public Boolean disLikeVideoMsg(String videoId) {
        return this.sendMsg(videoId, 3);
    }

    /**
     * 评论小视频
     * @return
     */
    public Boolean commentVideoMsg(String videoId) {
        return this.sendMsg(videoId, 4);
    }


    private Boolean sendMsg(String videoId,Integer type){

        try {
            User user = UserThreadLocal.getUser();
            Video video = this.videoApi.queryVideoById(videoId);
            Map<String,Object> msg = new HashMap<>();
            //构建消息
            msg.put("userId", user.getId());
            msg.put("date", System.currentTimeMillis());
            msg.put("videoId", videoId);
            msg.put("vid", video.getVid());
            msg.put("type", type);

            this.rocketMQTemplate.convertAndSend("tanhua-video", msg);
            return true;
        } catch (MessagingException e) {
            log.error("发送消息失败! videoId = " + videoId + ", type = " + type, e);
        }
        return false;
    }
}
