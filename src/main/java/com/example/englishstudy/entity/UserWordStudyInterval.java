package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_word_study_interval")
public class UserWordStudyInterval {

    @TableId(value = "interval_id", type = IdType.AUTO)
    private Integer intervalId;

    @TableField("user_id")
    private Integer userId;

    @TableField("word_id")
    private Integer wordId;

    @TableField("study_status")
    private String studyStatus;

    @TableField("remaining_interval")
    private Integer remainingInterval;

    @TableField("study_count")
    private Integer studyCount;

    @TableField("study_date")
    private LocalDateTime studyDate;

}
