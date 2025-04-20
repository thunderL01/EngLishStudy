package com.example.englishstudy.enums;

import lombok.Getter;

/**
 * 单词学习状态枚举类
 */
@Getter
public enum WordStatus {
    FORGOTTEN("忘记"),
    VAGUE("模糊"),
    KNOWN("认识");

    private final String value;

    WordStatus(String value) {
        this.value = value;
    }

}