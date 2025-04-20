package com.example.englishstudy.enums;

import lombok.Getter;

/**
 * 外观模式枚举类
 */
@Getter
public enum AppearanceMode {
    白天("白天"),
    黑夜("黑夜");

    private final String value;

    AppearanceMode(String value) {
        this.value = value;
    }

}