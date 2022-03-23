package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Visitors;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 21:32 2021/8/14
 * @description:
 */
public interface VisitorsApi {
    /**
     * 保存访客数据
     * @param userId 我的id
     * @param visitorUserId 访客id
     * @param from 来源
     * @return
     */
    String saveVisitor(Long userId, Long visitorUserId, String from);

    /**
     * 查询我的访客数据，存在2种情况：
     * 1. 我没有看过我的访客数据，返回前5个访客信息
     * 2. 之前看过我的访客，从上一次查看的时间点往后查询5个访客数据
     * @param userId
     * @return
     */
    List<Visitors> queryMyVisitor(Long userId);

    /**
     * 查询来访者列表
     * @param userId
     * @return
     */
    PageInfo<Visitors> queryVisitorsList(Long userId,Integer page,Integer pagesize);
}
