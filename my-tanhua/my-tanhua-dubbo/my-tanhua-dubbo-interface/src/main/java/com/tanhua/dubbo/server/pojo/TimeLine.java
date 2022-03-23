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
 * @date: Create in 16:04 2021/8/7
 * @description:时间线表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "quanzi_time_line_{userId}")
public class TimeLine implements Serializable {
    @Id
    private ObjectId id;//主键
    private Long userId;//好友发布的id
    private ObjectId publishId;//发布动态的id
    private Long date;//发布的时间
}
