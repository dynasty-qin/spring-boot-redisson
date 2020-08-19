package com.example.redisson.controller;

import com.example.redisson.annotation.EnableRedissonLock;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author : Harry
 * Description : 测试
 * Date : 2020-08-19 16:11
 */
@RestController
public class LockTestController {

    @RequestMapping(value = "/test")
    @EnableRedissonLock(prefix = "#p0", key = "#p0")
    public String lockTest(@RequestParam Integer number) throws InterruptedException {

        Thread.sleep(5000L);
        return String.valueOf(number);
    }
}
