package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author: tang
 * @date: Create in 21:30 2021/8/14
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "visitors")
public class Visitors implements Serializable {

    private ObjectId id;
    private Long userId; //我的id
    private Long visitorUserId; //来访用户id
    private String from; //来源，如首页、圈子等
    private Long date; //来访时间

    private Double score; //得分
}
