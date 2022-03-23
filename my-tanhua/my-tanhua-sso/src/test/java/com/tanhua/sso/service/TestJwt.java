package com.tanhua.sso.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 16:27 2021/8/2
 * @description:
 */
public class TestJwt {

    String secret = "itcast";

    @Test
    public void testCreateToken(){

        Map<String, Object> header = new HashMap<String, Object>();
        header.put(JwsHeader.TYPE, JwsHeader.JWT_TYPE);
        header.put(JwsHeader.ALGORITHM, "HS256");

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("mobile", "1333333333");
        claims.put("id", "2");

        // 生成token
        String jwt = Jwts.builder()
                .setHeader(header)  //header，可省略
                .setClaims(claims) //payload，存放数据的位置，不能放置敏感数据，如：密码等
                .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密盐
                .setExpiration(new Date(System.currentTimeMillis() + 300000)) //设置过期时间，3秒后过期
                .compact();

        System.out.println(jwt);

    }

    @Test
    public void testDecodeToken(){
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMzMzMzMzMzMzIiwiaWQiOiIyIiwiZXhwIjoxNjI3ODkzMjc4fQ.6uO3Im3dz5N3lbKHGabG-Z0RKI5fjXhQaTx4E5GYOXU";
        try {
            // 通过token解析数据
            Map<String, Object> body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println(body); //{mobile=1333333333, id=2, exp=1605513392}
        } catch (ExpiredJwtException e) {
            System.out.println("token已经过期！");
        } catch (Exception e) {
            System.out.println("token不合法！");
        }
    }


    @Test
    public void picTest(){
        String filename = "aa.jpgaa.jpg";
        String s = StringUtils.substringAfterLast(filename, ".");
        System.out.println(s);
    }

    @Test
    public void userDate(){
        DateTime dateTime = new DateTime();
        /*String year = dateTime.toString("yyyy");
        System.out.println(year);
        System.out.println(dateTime.toString("mm"));*/
        System.out.println(dateTime.toString("yyyy")+"-"+dateTime.toString("MM")+"-"+
                dateTime.toString("dd")+" "+
                dateTime.toString("HH")+":"+
                dateTime.toString("mm")+":"+
                dateTime.toString("ss"));
//        String hour = dateTime.plusHours(12).toString("HH");
//        System.out.println(hour);

    }
    @Test
    public void getFilepath(){
        String filename = "aaa.jpg";
        DateTime dateTime = new DateTime();
        String filepath =  "images/"+dateTime.toString("yyyy")+"/"+
                dateTime.toString("MM")+"/"+
                dateTime.toString("dd")+"/"+
                System.currentTimeMillis()+ RandomUtils.nextInt(10000,99999)+"/"+
                StringUtils.substringBeforeLast(filename,".");
        System.out.println(filepath);
    }


    @Test
    public void testDateTime(){
        String birthday = "1995-10-28";
        LocalDate over = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate now = LocalDate.now();
        int years = Period.between(over, now).getYears();
        System.out.println(years);
        DateTime parse = DateTime.parse(birthday);
        DateTime now1 = DateTime.now();
        System.out.println(now1.getYear()-parse.getYear());
    }
}
