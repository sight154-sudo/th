package com.tanhua.recommend.msg;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.pojo.RecommendQuanZi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * @author: tang
 * @date: Create in 19:51 2021/8/18
 * @description:  接收消息
 */
@Component
@RocketMQMessageListener(topic = "tanhua-quanzi",consumerGroup = "tanhua-quanzi-consumer")
@Slf4j
public class QuanZiMsgConsumer implements RocketMQListener<String> {

    @Autowired
    private MongoTemplate mongoTemplate;



    @Override
    public void onMessage(String msg) {
        try {
            //创建推荐的动态信息
            RecommendQuanZi recommendQuanZi = new RecommendQuanZi();
            //解析消息
//        msg.put("userId", user.getId());
//        msg.put("date", System.currentTimeMillis());
//        msg.put("publishId", publishId);
//        msg.put("pid", publish.getPid());
//        msg.put("type", type);
            JSONObject jsonObject = JSONUtil.parseObj(msg);
            Long userId = jsonObject.getLong("userId");
            Long date = jsonObject.getLong("date");
            String publishId = jsonObject.getStr("publishId");
            Long pid = jsonObject.getLong("pid");
            Integer type = jsonObject.getInt("type");
            recommendQuanZi.setUserId(userId);
            recommendQuanZi.setDate(date);
            recommendQuanZi.setId(ObjectId.get());
            recommendQuanZi.setPublishId(pid);
            /**
             * - 浏览 +1 点赞 +5 喜欢 +8 评论 + 10 发布动态
             *   - 文字长度：50以内1分，50~100之间2分，100以上3分
             *   - 图片个数：每个图片一分
             */
//        1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点赞，7-取消喜欢
            switch (type){
                case 1: //1-发动态，50以内1分，50~100之间2分，100以上3分  每个图片一分
                    //获取动态信息
                    Publish publish = this.mongoTemplate.findById(publishId, Publish.class);
                    Double score = 0d;
                    int count = CollUtil.size(publish.getMedias());
                    score+=count;
                    int length = publish.getText().length();
                    if(length>=0 && length < 50){
                        score+=1;
                    }else if(length < 100){
                        score+=2;
                    }else{
                        score+=3;
                    }
                    recommendQuanZi.setScore(score);
                    break;
                case 2: //2-浏览动态， 浏览 +1
                    recommendQuanZi.setScore(1d);
                    break;
                case 3: //3-点赞，点赞 +5
                    recommendQuanZi.setScore(5d);
                    break;
                case 4: //4-喜欢，喜欢 +8
                    recommendQuanZi.setScore(8d);
                    break;
                case 5: //5-评论，评论 + 10
                    recommendQuanZi.setScore(10d);
                    break;
                case 6: //6-取消点赞， -5
                    recommendQuanZi.setScore(-5d);
                    break;
                case 7://7-取消喜欢 -8
                    recommendQuanZi.setScore(-8d);
                    break;
                default:
                    recommendQuanZi.setScore(0d);
                    break;
            }

            //保存信息到mongodb中
            this.mongoTemplate.insert(recommendQuanZi);
        } catch (Exception e) {
            log.error("处理消息出错！msg = " + msg, e);
        }
    }
}
