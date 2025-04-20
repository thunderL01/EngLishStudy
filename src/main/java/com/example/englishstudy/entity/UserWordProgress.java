package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;


@Data
@TableName("user_word_progress")
@NoArgsConstructor
@AllArgsConstructor
public class UserWordProgress {
    @TableId(type = IdType.AUTO)
    private Integer userWordProgressId;

    private Integer userId;

    private Integer wordId;

    private String status;

    @TableField("learning_time")
    private LocalDate learningTime;

    @TableField("study_count")
    private Integer studyCount;




}