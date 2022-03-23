package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author: tang
 * @date: Create in 16:51 2021/8/12
 * @description:aaa
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "follow_user")
public class FollowUser implements Serializable {
    @Id
    private ObjectId id;//主键
    private Long userId;//用户id
    private Long followUserId;//关注用户的id
    private Long created;//关注时间
}
