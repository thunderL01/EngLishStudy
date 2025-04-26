package com.example.englishstudy.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 86400000; // 定义 JWT 的过期时间，设置为 24 小时。

    public String generateToken(String subject) {
        return Jwts.builder()//创建一个 JWT 构建器实例。
                .setHeaderParam("typ", "JWT")//设置 JWT 的头部参数，指定类型为 "JWT"。
                .setSubject(subject)//设置 JWT 的主题（通常是用户标识）。
                .setIssuedAt(new Date(System.currentTimeMillis()))//设置 JWT 的签发时间为当前时间。
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))//设置 JWT 的过期时间为当前时间加上定义的过期时间。
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)//使用 HS256 算法和指定的密钥对 JWT 进行签名。
                .compact();//构建并返回 JWT 的紧凑字符串表示。

    }

    public Claims validateToken(String token) {
        return Jwts.parser()//创建一个 JWT 解析器实例。
                .setSigningKey(SECRET_KEY)//设置用于验证 JWT 签名的密钥。设置解析器的签名密钥。
                .parseClaimsJws(token)//解析传入的 JWT 字符串并验证签名。
                .getBody();//获取解析后的 JWT 的主体部分（通常是包含用户信息的声明）。
    }
}