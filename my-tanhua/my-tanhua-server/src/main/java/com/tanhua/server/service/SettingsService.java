package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tanhua.common.mapper.SettingsMapper;
import com.tanhua.common.pojo.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: tang
 * @date: Create in 16:45 2021/8/17
 * @description:
 */
@Service
public class SettingsService {

    @Autowired
    private SettingsMapper settingsMapper;

    public Settings queryUserSettings(Long userId){
        LambdaQueryWrapper<Settings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Settings::getUserId,userId);
        return this.settingsMapper.selectOne(wrapper);
    }

    /**
     * 修改用户通用设置
     * @param settings
     */
    public void updateUserSettings(Settings settings) {
        LambdaQueryWrapper<Settings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Settings::getId,settings.getId());
        this.settingsMapper.updateById(settings);
    }
}
