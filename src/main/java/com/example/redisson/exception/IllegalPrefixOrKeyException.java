package com.example.redisson.exception;

/**
 * Author : Harry
 * Description : 锁前缀或KEY值异常
 * Date : 2020-08-19 15:49
 */
public class IllegalPrefixOrKeyException extends RuntimeException {

    public IllegalPrefixOrKeyException(String message) {
        super(message);
    }
}
