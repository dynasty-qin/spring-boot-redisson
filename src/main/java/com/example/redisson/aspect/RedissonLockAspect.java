package com.example.redisson.aspect;

import com.example.redisson.annotation.EnableRedissonLock;
import com.example.redisson.exception.CanNotGetLockException;
import com.example.redisson.exception.IllegalPrefixOrKeyException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Author : Harry
 * Description : 处理锁逻辑
 * Date : 2020-08-19 15:28
 */
@SuppressWarnings("SpellCheckingInspection")
@Aspect
@Component
public class RedissonLockAspect {

    private final RedissonClient redissonClient;

    @Autowired
    public RedissonLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Pointcut(value = "@annotation(com.example.redisson.annotation.EnableRedissonLock)")
    public void pointcut() {
    }

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        EnableRedissonLock enableRedissonLock = method.getAnnotation(EnableRedissonLock.class);
        if (enableRedissonLock == null) {
            return joinPoint.proceed();
        }

        String prefix = enableRedissonLock.prefix();
        String key = enableRedissonLock.key();
        String prefixValue = this.parse(target, prefix, method, args);
        String keyValue = this.parse(target, key, method, args);

        if (this.isAnyEmpty(prefixValue, keyValue)) {
            throw new IllegalPrefixOrKeyException("锁前缀或KEY不合法");
        }
        String lockKey = prefixValue + ":" + keyValue;

        // 使用公平锁模式
        RLock fairLock = redissonClient.getLock(lockKey);
        if (fairLock.tryLock(0, 5, TimeUnit.SECONDS)) {
            try {
                // 取到锁, 执行方法
                return joinPoint.proceed();
            } catch (Throwable throwable) {

                throwable.printStackTrace();
                return "方法执行出现异常 !";
            } finally {
                // 释放锁
                if (fairLock.isHeldByCurrentThread()) {
                    if (fairLock.isLocked()) {
                        fairLock.unlock();
                    }
                }
            }
        }else {
            throw new CanNotGetLockException("无法获得锁 !");
        }
    }

    private boolean isAnyEmpty(String... params) {

        if (params.length == 0) {
            return true;
        }
        for (String param : params) {

            if (StringUtils.isEmpty(param)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据注解中定义的SPEL表达式获取值
     */
    private String parse(Object rootObject, String spel, Method method, Object[] args) {

        // 获取方法参数名列表
        LocalVariableTableParameterNameDiscoverer u =
                new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);
        if (paraNameArr == null || paraNameArr.length == 0) {
            return null;
        }
        // 解析SPEL表达式
        ExpressionParser parser = new SpelExpressionParser();
        // SPEL CONTEXT
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, args, u);
        // 将参数加入CONTEXT
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(spel).getValue(context, String.class);
    }
}
