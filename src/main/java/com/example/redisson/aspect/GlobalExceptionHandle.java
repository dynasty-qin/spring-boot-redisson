package com.example.redisson.aspect;

import com.example.redisson.exception.CanNotGetLockException;
import com.example.redisson.exception.IllegalPrefixOrKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Author : Harry
 * Description : 全局异常处理
 * Date : 2020-08-19 16:33
 */
@Component
@ControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(value = IllegalPrefixOrKeyException.class)
    @ResponseBody
    public String illegalPrefixOrKeyException() {
        return "锁前缀或KEY不合法 !";
    }

    @ExceptionHandler(value = CanNotGetLockException.class)
    @ResponseBody
    public String canNotGetLockException() {
        return "点太快啦 !";
    }
}
