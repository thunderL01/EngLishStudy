package com.example.englishstudy.controller;

import com.example.englishstudy.serviceimpl.BigModelNew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/AI")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private BigModelNew bigModelNew;

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        try {
            return bigModelNew.sendMessage(message);
        } catch (Exception e) {
            logger.info("调用大模型接口时出现异常", e);
            return "请求出错，请稍后重试";
        }
    }
}