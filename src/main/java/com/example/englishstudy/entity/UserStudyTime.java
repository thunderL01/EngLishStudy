package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@TableName("user_study_time")
public class UserStudyTime {
    @TableId(type = IdType.AUTO)
    private Integer userStudyTimeId;
    private Integer userId;
    private LocalDate userStudyTimeDate;
    private Integer studyTime;
}
