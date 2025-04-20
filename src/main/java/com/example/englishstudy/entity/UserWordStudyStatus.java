package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;


@Data
@TableName("user_word_study_status")
public class UserWordStudyStatus {

    // 主键
    @TableId(value = "user_word_study_status_id", type = IdType.AUTO)
    private Integer userWordStudyStatusId;

    // 用户 ID，外键，关联 user 表的 user_id
    @TableField("user_id")
    private Integer userId;

    // 单词 ID，外键，关联 word 表的 word_id
    @TableField("word_id")
    private Integer wordId;

    // 学习日期
    @TableField("user_word_study_status_date")
    private LocalDate studyDate;

    // 学习状态，认识，模糊，忘记
    @TableField("study_status")
    private String studyStatus;

    // 学习完成度（百分比）
    @TableField("completion_degree")
    private double completionDegree;

    // 是否完成学习，0 表示未完成，1 表示完成
    @TableField("is_completed")
    private Boolean isCompleted;

    // 单词的学习状态，是新学还是复习单词
    @TableField("study_type")
    private String studyType;


}