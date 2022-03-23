package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author: tang
 * @date: Create in 9:51 2021/8/11
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "recommend_video")
public class RecommendVideo implements Serializable {
    @Id
    private ObjectId id;
    private Long userId;
    private Long videoId;
    private Double score;
    private Long date;
}
