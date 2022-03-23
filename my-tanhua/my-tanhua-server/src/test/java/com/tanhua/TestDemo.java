package com.tanhua;

import cn.hutool.core.lang.Snowflake;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: tang
 * @date: Create in 10:15 2021/8/19
 * @description:
 */
public class TestDemo {

    /*@Test
    public void demo01(){
        Snowflake
    }*/


    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }

        long start = System.currentTimeMillis();
        list.stream().forEach(i->list.get(i));//32
        for (int i = 0; i < list.size(); i++) {
            list.get(i);
        }
        long end = System.currentTimeMillis();

        System.out.println("用户时: "+(end-start));
    }
}
