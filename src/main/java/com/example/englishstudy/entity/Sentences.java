package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sentences")
public class Sentences {
    @TableId(type = IdType.AUTO)
    private Integer sentenceId;

    // 外键，关联原word_details表的word_id，表示该句子属于哪个单词
    @TableField("word_id")
    private Integer wordId;
    // 存储句子内容
    @TableField("sentence")
    private String sentence;
    // 存储句子的翻译
    @TableField("sentence_trans")
    private String sentenceTrans;
}