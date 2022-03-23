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
 * @date: Create in 16:01 2021/8/7
 * @description: 相册表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "quanzi_album_{userId}")
public class Album implements Serializable {
    @Id
    private ObjectId id;//主键id
    private ObjectId publishId;//发布的id
    private Long created;//发布时间
}
