package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.UserCheckins;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCheckinsMapper extends BaseMapper<UserCheckins> {
    // 目前无自定义方法
}