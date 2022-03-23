package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.common.mapper.AnnouncementMapper;
import com.tanhua.common.pojo.Announcement;
import com.tanhua.server.vo.AnnouncementVo;
import com.tanhua.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 14:59 2021/8/14
 * @description:
 */
@Service
public class AnnouncementService {


    @Autowired
    private AnnouncementMapper announcementMapper;

    public PageResult queryAnnouncement(Integer page,Integer pagesize){
        IPage<Announcement> announcementIPage = this.queryAnnouncementPage(page, pagesize);
        List<Announcement> records = announcementIPage.getRecords();
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        if(CollUtil.isEmpty(records)){
            return pageResult;
        }
        List<AnnouncementVo> collect = records.stream().map(record -> {
            AnnouncementVo announcementVo = new AnnouncementVo();
            announcementVo.setId(String.valueOf(record.getId()));
            announcementVo.setTitle(record.getTitle());
            announcementVo.setDescription(record.getDescription());
            announcementVo.setCreateDate(DateUtil.format(record.getCreated(), "yyyy-MM-dd HH:mm"));
            return announcementVo;
        }).collect(Collectors.toList());
        pageResult.setItems(collect);
        return pageResult;
    }


    public IPage<Announcement> queryAnnouncementPage(Integer page,Integer pagesize){
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        return announcementMapper.selectPage(new Page<>(page,pagesize),wrapper);
    }
}
