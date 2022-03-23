package com.tanhua.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.common.pojo.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: tang
 * @date: Create in 21:24 2021/8/1
 * @description:
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
