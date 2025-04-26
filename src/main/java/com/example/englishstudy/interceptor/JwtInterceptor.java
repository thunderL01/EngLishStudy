package com.example.englishstudy.interceptor;

import com.example.englishstudy.utils.JwtUtils;
import com.example.englishstudy.utils.Result;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// 实现 HandlerInterceptor 接口，用于拦截请求并处理 JWT 验证
public class JwtInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取 Authorization 字段的值
        String token = request.getHeader("Authorization");


        if (token != null && token.startsWith("Bearer ")) {
            // 去除 "Bearer " 前缀，获取真正的 JWT
            token = token.substring(7);
            try {
                // 使用注入的 JwtUtils 实例验证 JWT
                Claims claims = jwtUtils.validateToken(token);
                // 将 JWT 中的主题（通常是用户标识）存入请求属性，方便后续处理使用
                request.setAttribute("openid", claims.getSubject());
                return true;
            } catch (Exception e) {
                // JWT 验证失败，返回未授权错误信息
                sendErrorResponse(response, Result.Code.UNAUTHORIZED);
                return false;
            }
        }
        // 没有提供有效的 JWT，返回未授权错误信息
        sendErrorResponse(response, Result.Code.UNAUTHORIZED);
        return false;
    }

    private void sendErrorResponse(HttpServletResponse response, Result.Code code) throws IOException {
        // 设置响应的内容类型为 JSON 格式，并指定字符编码为 UTF-8
        response.setContentType("application/json;charset=UTF-8");
        // 设置响应状态码
        response.setStatus(code.getCode());
        // 获取响应输出流
        PrintWriter writer = response.getWriter();
        // 将错误信息以 JSON 格式写入响应
        writer.write(Result.error(code).toString());
        writer.flush();
        writer.close();
    }
}