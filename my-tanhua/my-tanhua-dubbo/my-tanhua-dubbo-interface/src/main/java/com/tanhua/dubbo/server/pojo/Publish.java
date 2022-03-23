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
 * @date: Create in 15:53 2021/8/7
 * @description: 发布表 记录用户发布信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_publish")
public class Publish implements Serializable {
    @Id
    private ObjectId id;//主键
    private Long pid;//发布动态的记录id
    private Long userId;//发布动态的用户id
    private String text;//发布动态的内容
    private List<String> medias;//发布动态时的图片地址
    private Integer seeType;//可见类型 1:公开  2:私有  3:部分可见 4:不给谁看
    private List<Long> seeList;//谁可见的列表
    private List<Long> notSeeList;//不给谁看的列表
    private String longitude;//经度
    private String latitude;//纬度
    private String locationName;//发布的地址
    private Long created;//发布的时间
}
