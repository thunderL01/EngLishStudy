package com.example.englishstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@TableName("user_checkins")
public class UserCheckins {
    @TableId(type = IdType.AUTO)
    private Integer userCheckinsId;
    private Integer userId;
    private LocalDate checkinDate;
    private LocalDate lastCheckinDate;
    private Integer experienceEarned;
    private Integer consecutiveCheckinDays;
    private Integer totalCheckinDays;
}
