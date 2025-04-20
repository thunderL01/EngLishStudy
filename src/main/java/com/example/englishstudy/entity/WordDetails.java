package com.example.englishstudy.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("word_details")
public class WordDetails {
    @TableId(type = IdType.AUTO)
    private Integer wordDetailsId;
    private Integer wordId;

    @TableField("pos")
    private String pos;
    @TableField("definition")
    private String definition;

}
