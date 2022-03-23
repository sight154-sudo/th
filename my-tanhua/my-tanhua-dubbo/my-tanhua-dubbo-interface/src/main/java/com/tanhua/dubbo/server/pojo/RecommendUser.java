package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: tang
 * @date: Create in 22:42 2021/8/4
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "recommend_user")
public class RecommendUser implements Serializable {

    @Id
    private ObjectId id;
    @Indexed
    private Long userId;//推荐的用户id
    private Long toUserId;//用户id
    @Indexed
    private Double score;//得分
    private Date date;//日期

}
