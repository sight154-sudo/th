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
 * @date: Create in 16:09 2021/8/7
 * @description:评论表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "quanzi_comment")
public class Comment implements Serializable {
    @Id
    private ObjectId id;//主键
    private ObjectId publishId;//发布的动态的id
    private Integer commentType;//评论的类型  1-点赞  2-评论  3-喜欢
    private String content;//评论的内容
    private Long userId;//评论的用户id
    private Long publishUserId;//发布动态的用户id;
    private Boolean isParent = false;//是否是父节点
    private ObjectId parentId;//父节点id
    private Long created;//评论的时间

}
