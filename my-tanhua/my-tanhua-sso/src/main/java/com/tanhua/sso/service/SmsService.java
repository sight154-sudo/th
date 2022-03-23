package com.tanhua.sso.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.tanhua.sso.config.AliyunSMSConfig;
import com.tanhua.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author: tang
 * @date: Create in 22:10 2021/8/1
 * @description:
 */
@Service
@Slf4j
public class SmsService {

    @Autowired
    private AliyunSMSConfig smsConfig;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    /**
     * 发送验证码
     * @param mobile
     * @return
     */
    public String sendSms(String mobile){
        DefaultProfile profile = DefaultProfile.getProfile(smsConfig.getRegionId(),
                smsConfig.getAccessKeyId(), smsConfig.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        String code = String.valueOf(RandomUtils.nextInt(100000,999999));

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(smsConfig.getDomain());
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", smsConfig.getRegionId());
        request.putQueryParameter("PhoneNumbers", mobile); //目标手机号
        request.putQueryParameter("SignName", smsConfig.getSignName()); //签名名称
        request.putQueryParameter("TemplateCode", smsConfig.getTemplateCode()); //短信模板code
        request.putQueryParameter("TemplateParam", "{\"code\":\""+code+"\"}");//模板中变量替换
        try {
            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            if(StringUtils.contains(data,"\"Message\":\"OK\"")){
                log.info("短信验证成功...data="+data);
                return code;
            }
            //{"Message":"OK","RequestId":"EC2D4C9A-0EAC-4213-BE45-CE6176E1DF23","BizId":"110903802851113360^0","Code":"OK"}
            //System.out.println(response.getData());
        } catch (Exception e) {
            log.error("短信验证失败...mobile = ",mobile,e);
        }
        return null;
    }

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    public ErrorResult sendCheckCode(String phone) {
        //将验证码保存到redis中 key为CHECK_CODE_+phone  值为随机生成的6位数字
        String redisKey = "CHECK_CODE_"+phone;
        ErrorResult result = null;
        //判断redis中是否存在
        Boolean flag = redisTemplate.hasKey(redisKey);
        if(flag){
            //如果存在,则提示上一次的验证码还未失效
            return ErrorResult.builder().errCode("000001").errMessage("上一次验证码还未失效").build();
        }
        //不存在，则发送验证码
//        String checkCode = this.sendSms(phone);
        String checkCode = "123456";
        if(StringUtils.isBlank(checkCode)){
            return ErrorResult.builder().errCode("000002").errMessage("验证码发送失败~").build();
        }
        //将验证码保存到redis中
        redisTemplate.opsForValue().set(redisKey,checkCode, Duration.ofMinutes(5));
        return null;
    }
}
