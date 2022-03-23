package com.tanhua.dubbo.server.service;

import com.tanhua.dubbo.server.pojo.TimeLine;
import com.tanhua.dubbo.server.pojo.Users;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author: tang
 * @date: Create in 22:42 2021/8/7
 * @description:使用异步的方式保存到好友的动态
 */
@Service
@Slf4j
public class TimeLineService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Async
    public CompletableFuture<String> saveTimeLine(Long userId, ObjectId publishId){
        try {
            Query query = Query.query(Criteria.where("userId").is(userId));
            List<Users> usersList = this.mongoTemplate.find(query, Users.class);
            //将动态信息保存到每个好友的动态列表中
            for (Users user : usersList) {
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                //设置发布人的userId
                timeLine.setUserId(userId);
                //设置动态的id
                timeLine.setPublishId(publishId);
                timeLine.setDate(System.currentTimeMillis());

                this.mongoTemplate.save(timeLine,"quanzi_time_line_"+user.getFriendId());
            }
        } catch (Exception e) {
            log.error("异步保存到好友列表的动态信息失败~userId = "+userId+" publishId = "+publishId,e);
            return CompletableFuture.completedFuture("error");
        }
        return CompletableFuture.completedFuture("ok");
    }
}
