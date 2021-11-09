package cn.vuffy.emos.wx.aop;

import cn.vuffy.emos.wx.common.util.R;
import cn.vuffy.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description: AOP切面类，拦截所有Web方法的返回值，在返回的R对象中添加更新后的令牌
 * 注意：在OAuth2Filter中，把更新后的令牌写到ThreadLocalToken里面的ThreadLocal
 * @author: luf
 * @create: 2021-11-08 22:38
 **/

@Aspect
@Component
public class TokenAspect {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * cn.vuffy.emos.wx.controller.*.*(..))")
    public void aspect() {
    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        R r = (R) point.proceed(); // 方法执行结果

        //如果ThreadLocal中存在Token，说明是更新的Token
        String token = threadLocalToken.getToken();
        if (null != token) {
            r.put("token", token); // 向响应中添加Token
            threadLocalToken.clear();
        }
        return r;
    }
}