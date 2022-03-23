package com.tanhua.sso.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.tanhua.sso.config.AliyunSMSConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/*
pom.xml
<dependency>
  <groupId>com.aliyun</groupId>
  <artifactId>aliyun-java-sdk-core</artifactId>
  <version>4.5.3</version>
</dependency>
*/
@SpringBootTest
@Slf4j
public class SendSms {

    @Autowired
    private AliyunSMSConfig smsConfig;

    @Test
    public void sendTest(){
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
        request.putQueryParameter("PhoneNumbers", "17602026868"); //目标手机号
        request.putQueryParameter("SignName", smsConfig.getSignName()); //签名名称
        request.putQueryParameter("TemplateCode", smsConfig.getTemplateCode()); //短信模板code
        request.putQueryParameter("TemplateParam", "{\"code\":\""+code+"\"}");//模板中变量替换
        try {
            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            if(StringUtils.contains(data,"\"Message\":\"OK\"")){
                log.info("短信验证成功...data=",data);

            }
            //{"Message":"OK","RequestId":"EC2D4C9A-0EAC-4213-BE45-CE6176E1DF23","BizId":"110903802851113360^0","Code":"OK"}
            //System.out.println(response.getData());
        } catch (Exception e) {
            log.error("短信验证失败...mobile = ","17602026868",e);
        }
    }
    /*public static void main(String[] args) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                "", "");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", "15172375571"); //目标手机号
        request.putQueryParameter("SignName", "乐优商城"); //签名名称
        request.putQueryParameter("TemplateCode", "SMS_187751505"); //短信模板code
        request.putQueryParameter("TemplateParam", "{\"code\":\"123456\"}");//模板中变量替换
        try {
            CommonResponse response = client.getCommonResponse(request);

            //{"Message":"OK","RequestId":"EC2D4C9A-0EAC-4213-BE45-CE6176E1DF23","BizId":"110903802851113360^0","Code":"OK"}
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }*/
}
