package com.example.englishstudy.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_word_selection")
public class UserWordSelection {
    @TableId(type = IdType.AUTO)
    private Integer userWordSelectionId;

    private Integer userId;
    private Integer wordId;
    private Integer status;

    private LocalDate date;

}