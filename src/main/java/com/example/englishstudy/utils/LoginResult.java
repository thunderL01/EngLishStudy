package com.example.englishstudy.utils;

import com.example.englishstudy.entity.User;
import lombok.Data;

@Data
public class LoginResult {
    private User user;
    private String token;

    public LoginResult(User user, String token) {
        this.user = user;
        this.token = token;
    }
}