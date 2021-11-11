package cn.vuffy.emos.wx.config.shiro;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 工具类，用来加密Token和验证Token的有效性
 * JWT的Token要经过加密才能返回给客户端，包括客户端上传的Token，后端项目需要验证核实。
 */
@Component
@Slf4j
public class JwtUtil {

    // 密钥
    @Value("${emos.jwt.secret}")
    private String secret;

    // 过期时间/天
    @Value("${emos.jwt.expire}")
    private int expire;

    public String createToken(int userId) {
        //Date date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, expire).toJdkDate();
        Date date=DateUtil.offset(new Date(), DateField.DAY_OF_YEAR,5);
        // 创建加密算法对象
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTCreator.Builder builder = JWT.create();
        String token = builder.withClaim("userId", userId).withExpiresAt(date).sign(algorithm);
        return token;
    }

    // 解码
    public int getUserId(String token){
        DecodedJWT jwt=JWT.decode(token);
        int userId=jwt.getClaim("userId").asInt();
        return userId;
    }

    public void verifierToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(token);
    }
}