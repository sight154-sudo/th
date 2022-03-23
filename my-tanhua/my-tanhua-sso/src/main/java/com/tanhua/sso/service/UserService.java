package com.tanhua.sso.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.common.mapper.UserMapper;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.HuanXinApi;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: tang
 * @date: Create in 17:56 2021/8/2
 * @description:
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Reference(version = "1.0.0")
    private HuanXinApi huanXinApi;

    @Value("${jwt.secret}")
    private String secret;

    public String login(String phone, String verificationCode) {
        //判断验证码是否正确
        //是否为新用户的标识
        Boolean isNew = false;
        String redisKey = "CHECK_CODE_"+phone;
        String code = redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.equals(code,verificationCode)){
            //验证码不正确
            return null;
        }
        //验证码匹配 则删除redis中的数据
        redisTemplate.delete(redisKey);

        //判断用户是否为新用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getMobile,phone);
        User user = userMapper.selectOne(wrapper);
        if(null == user){
            //为新用户 则添加到数据库中
            user = new User();
            user.setMobile(phone);

            user.setPassword(DigestUtils.md5Hex("123456"));
            userMapper.insert(user);
            isNew = true;
            //并将用户信息注册到环信中
            Boolean flag = huanXinApi.register(user.getId());
            if(!flag){
                throw new RuntimeException("新用户注册失败");
            }
        }
        //生成token

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("id", user.getId());

        // 生成token
        String token = Jwts.builder()
                .setClaims(claims) //payload，存放数据的位置，不能放置敏感数据，如：密码等
                .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密盐
                .setExpiration(new DateTime().plusHours(12).toDate()) //设置过期时间，3秒后过期
                .compact();
        System.out.println(token);
        String result = token + "|" + isNew;
        return result;
    }


    /**
     * 通过token获取登陆用户的信息
     * @param token
     * @return
     */
    public User getTokenInfo(String token) {
        try {
            // 通过token解析数据
            Map<String, Object> body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
//            System.out.println(body);
            //{id=2, exp=1605513392}
            User user = new User();
            user.setId(Long.valueOf(body.get("id").toString()));
            //需要保存用户的手机号信息  为了减少查询数据库的频率 将用户手机号信息保存到redis中
            String redisKey = "TANHUA_USER_MOBILE_"+body.get("id");
            if(redisTemplate.hasKey(redisKey)){
                //存在
                String s = redisTemplate.opsForValue().get(redisKey);
                user.setMobile(s);
                return user;
            }
            //不存在，则查询数据库
            user = userMapper.selectById(user.getId());
            //保存到redis数据库中
            long time = Long.valueOf(body.get("exp").toString()).longValue()*1000 - System.currentTimeMillis();
            redisTemplate.opsForValue().set(redisKey,user.getMobile(),time,TimeUnit.MILLISECONDS);
            return user;
        } catch (ExpiredJwtException e) {
            log.info("token已经过期~token="+token);
        } catch (Exception e) {
            log.error("token不合法~token="+token,e);
        }
        return null;
    }

    /**
     * 判断手机号是否存在
     * @param phone
     * @return
     */
    public Boolean mobileIsExists(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getMobile,phone);
        return this.userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    public User queryUserByUid(Long userId){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId,userId);
        return this.userMapper.selectById(userId);
    }

    /**
     * 修改手机号
     * @param phone
     */
    public void updateMobile(Long userId,String phone) {
        User user = this.userMapper.selectById(userId);
        user.setMobile(phone);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId,userId);
        this.userMapper.update(user,wrapper);
    }
}
