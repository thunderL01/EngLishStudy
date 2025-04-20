package com.example.englishstudy.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@TableName("word")
public class Word {

    @TableId(type = IdType.AUTO)
    private Integer wordId;
    private String word;
    private String bookId;
    private String UkPhonetic;
    private String UkPronunciationUrl;
    private String UsPhonetic;
    private String UsPronunciationUrl;






}
