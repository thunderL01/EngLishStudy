package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.englishstudy.enums.LearningMode;
import com.example.englishstudy.enums.AppearanceMode;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String userName;
    private String phone;
    private String avatar;
    private String wechatOpenid;
    private Integer experience;
    private Integer level;
    private Integer age;
    private LocalDate birthday;
    private String gender;
    private LocalDateTime createdAt;
    private LearningMode learningMode;
    private AppearanceMode appearanceMode;
    private Integer dailyStudyAmount;
    private String bookId;


}
