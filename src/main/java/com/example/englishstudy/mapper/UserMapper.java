package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 目前无自定义方法


    /**
     * 根据用户 ID 查询用户等级
     * @param userId 用户 ID
     * @return 用户等级
     */
    @Select("SELECT FLOOR(experience / 100) + 1 FROM user WHERE user_id = #{userId}")
    Integer getUserLevel(Integer userId);

}
