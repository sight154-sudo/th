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
 * @date: Create in 11:50 2021/8/9
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "recommend_quanzi")
public class RecommendQuanZi implements Serializable {

    @Id
    private ObjectId id;//主键
    private Long userId;//用户id
    private Long publishId;//推荐的动态id
    private Double score;//缘分值
    private Long date;//动态发布的时间

}
