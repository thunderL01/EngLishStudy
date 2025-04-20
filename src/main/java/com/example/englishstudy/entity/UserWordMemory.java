package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

//根据UserWordStudyStatus的单词 ：userId，wordId，studyDate，isCompleted（为true），则在UserWordMemory中创建一条记录
@Data
@TableName("user_word_memory")
public class UserWordMemory {

    @TableId(type = IdType.AUTO)
    private Integer userWordMemoryId;

    private Integer userId;

    private Integer wordId;

    //记忆强度，通过UserWordStudyStatus的userId,wordId，studyDate，studyStatus来统计memoryStrength
    @TableField("memory_strength")
    private Double memoryStrength;

    //复习次数，通过UserWordStudyStatus的userId,wordId，studyDate来统计count
    @TableField("review_count")
    private Integer reviewCount;

    /*下次复习时间，通过UserWordStudyStatus的userId,wordId，studyDate，
    通过UserWordStudyInterval的userId,wordId，studyDate，studyCount来统计nextReviewTime
     */
    @TableField("next_review_time")
    private LocalDate nextReviewTime;


    @TableField("last_study_status")
    private String lastStudyStatus; // 最近一次学习状态

    @TableField("last_study_date")
    private LocalDate lastStudyDate; // 最近一次学习日期

}