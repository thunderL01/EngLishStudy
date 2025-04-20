package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("phrases")
public class Phrases {
    @TableId(type = IdType.AUTO)
    private Integer phraseId;

    // 外键，关联原word_details表的word_id，表示该短语属于哪个单词
    @TableField("word_id")
    private Integer wordId;
    // 存储短语内容
    @TableField("phrase_content")
    private String phraseContent;
    // 存储短语的翻译
    @TableField("phrase_trans")
    private String phraseTrans;
}