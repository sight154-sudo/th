package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * @author: tang
 * @date: Create in 21:37 2021/8/10
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "video")
public class Video implements Serializable {
    @Id
    private ObjectId id;//主键
    private Long vid;//小视频自增长id
    private Long userId;//发布视频的用户id
    private String text;//内容
    private String picUrl;//缩略图路径
    private String videoUrl;//小视频路径
    private Long created;//创建时间
    private Integer seeType; // 谁可以看，1-公开，2-私密，3-部分可见，4-不给谁看
    private List<Long> seeList; //部分可见的列表
    private List<Long> notSeeList; //不给谁看的列表
    private String longitude; //经度
    private String latitude; //纬度
    private String locationName; //位置名称
}
