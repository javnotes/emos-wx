package cn.vuffy.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @description: 过滤器，可拦截所有Http请求
 * 1. 判断哪些请求应该被 Shirol处理
 * 2. 判断 Token是真过期还是假过期
 * 3. 存储新令牌
 * @author: luf
 * @create: 2021-11-07 16:52
 **/
@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {
    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 拦截请求后，把令牌字符串封装成令牌对象
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;

        // 获取请求token
        String token = getRequestToken(req);

        if (StrUtil.isBlank(token)) {
            return null;
        }
        return new OAuth2Token(token);
    }

    /**
     * 拦截请求，判断请求是否需要被Shiro处理
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {

        HttpServletRequest req = (HttpServletRequest) request;

        // Ajax 提交application/json 数据的时候，会先发出Options请求
        // 这里要放行Options请求，不需要Shiro处理
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        // 除了Options请求外，所有请求都要被Shiro处理
        return false;
    }

    /**
     * 处理所有应该被Shiro处理的请求
     * isAccessAllowed() 返回true时，该方法执行
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        // 允许跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        threadLocalToken.clear();

        // 获取token，如果token不存在，直接返回401
        String token = getRequestToken(req);
        if (StrUtil.isBlank(token)) { // 不存在token
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }
        try { // 存在token
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) { // (客户端)token过期，则去查看Redis里有无token
            if (redisTemplate.hasKey(token)) { // Redis内有token，则【刷新】令牌
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                token = jwtUtil.createToken(userId);
                //set(K key, V value, long timeout, TimeUnit unit) 设置变量值的过期时间
                redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(token);
            } else { // (客户端)token过期，Redis内无token，则令牌过期（认证/授权），需要重新登录
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期");
                return false;
            }
        } catch (Exception e) { // 不排除令牌可能是假冒的
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("令牌已过期");
            return false;
        }

        // 上面的检测没有问题，则执行Realm类
        // 只能间接地让Shiro调用Realm类，得到认证/授权结果
        boolean bool = executeLogin(request, response);
        return bool;
    }

    /**
     * 当用户没有登录/登录失败时，会触发该方法
     *
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);

        try {
            resp.getWriter().print(e.getMessage()); // 认证(非授权)失败的信息
        } catch (Exception exception) {

        }
        return false;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        super.doFilterInternal(request, response, chain);
    }

    /**
     * 获取请求头里的token
     *
     * @param httpRequest
     * @return
     */
    private String getRequestToken(HttpServletRequest httpRequest) {
        // 从header中获取token
        String token = httpRequest.getHeader("token");

        // 如果header中不存在token，则从参数中获取token
        if (StrUtil.isBlank(token)) {
            token = httpRequest.getParameter("token");
        }
        return token;
    }
}
