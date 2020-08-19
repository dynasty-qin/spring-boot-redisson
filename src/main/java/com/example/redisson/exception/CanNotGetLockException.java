package com.example.redisson.exception;

/**
 * Author : Harry
 * Description : 无法得到锁
 * Date : 2020-08-19 15:59
 */
public class CanNotGetLockException extends RuntimeException {

    public CanNotGetLockException(String message) {
        super(message);
    }
}
