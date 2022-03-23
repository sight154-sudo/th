package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.common.mapper.BlackListMapper;
import com.tanhua.common.pojo.BlackList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 19:22 2021/8/17
 * @description:
 */
@Service
public class BlackListService {

    @Autowired
    private BlackListMapper blackListMapper;

    public IPage<BlackList> queryPageBlackList(Long userId,Integer curPage,Integer pagesize){
        LambdaQueryWrapper<BlackList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlackList::getUserId,userId);
        return blackListMapper.selectPage(new Page(curPage,pagesize),wrapper);
    }

    public List<BlackList> queryBlackList(Long userId){
        LambdaQueryWrapper<BlackList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlackList::getUserId,userId);
        return this.blackListMapper.selectList(wrapper);
    }
    public void removeBlackList(Long id, Long userId) {
        LambdaQueryWrapper<BlackList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlackList::getUserId,id);
        wrapper.eq(BlackList::getBlackUserId,userId);
        this.blackListMapper.delete(wrapper);
    }
}
