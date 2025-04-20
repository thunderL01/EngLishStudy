package com.example.englishstudy.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("word_synonyms")
public class WordSynonyms {
    @TableId(type = IdType.AUTO)
    private Integer wordSynonymsId;
    private Integer wordId;

    @TableField("pos")
    private String pos;
    @TableField("synonym_word")
    private String synonymWord;
    @TableField("synonym_word_trans")
    private String synonymWordTrans;
}
