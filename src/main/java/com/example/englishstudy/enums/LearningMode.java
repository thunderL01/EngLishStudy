package com.example.englishstudy.enums;

import lombok.Getter;

/**
 * 学习模式枚举类
 */
@Getter
public enum LearningMode {
    中英("中英"),
    英中("英中");

    private final String value;

    LearningMode(String value) {
        this.value = value;
    }

}