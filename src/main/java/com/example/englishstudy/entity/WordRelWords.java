package com.example.englishstudy.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@TableName("word_rel_words")
public class WordRelWords {
    @TableId(type = IdType.AUTO)
    private Integer wordRelWordsId;
    private Integer wordId;

    @TableField("pos")
    private String pos;
    @TableField("rel_word")
    private String relWord;
    @TableField("rel_word_trans")
    private String relWordTrans;

}
