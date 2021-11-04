package cn.vuffy.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * 只要是同一个线程，往ThreadLocal里面写入数据和读取数据是完全相同的
 */
@Component
public class ThreadLocalToken {

    private ThreadLocal local = new ThreadLocal();

    public void setToken(String token) {
        local.set(token);
    }

    public String getToken() {
        return (String) local.get();
    }

    public void clear() {
        local.remove();
    }
}