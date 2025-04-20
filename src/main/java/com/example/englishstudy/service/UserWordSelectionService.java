package com.example.englishstudy.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.example.englishstudy.entity.UserWordSelection;
import com.example.englishstudy.entity.Word;

import java.util.List;

public interface UserWordSelectionService extends IService<UserWordSelection> {

    /**
     * 用户个人选择多个单词进行记忆，然后插入UserWordSelection表生成记录，以及UserWordStudyStatus表生成记录
     *
     * @param userId           用户 ID
     * @param words            要选择的单词列表
     * @param dailyStudyAmount 每日学习量
     * @return 插入是否成功
     */
    boolean selectWordsForMemory(Integer userId, List<String> words, Integer dailyStudyAmount);
    /**
     * 获取用户在指定前缀词典中已选择的单词列表
     * @param userId 用户 ID
     * @param bookIdPrefix 词典前缀
     * @return 用户已选择的单词列表
     */
    List<String> getSelectedWordsByBookIdPrefix(Integer userId, String bookIdPrefix);


    /**
     * 获取用户已选择的单词总数
     * @param userId 用户ID
     * @return 用户已选择的单词总数
     */
    int countSelectedWordsByUserId(Integer userId);




    /**
     * 从指定单词列表中随机选择单词进行记忆
     * @param userId 用户ID
     * @param words 候选单词列表
     * @param dailyStudyAmount 每日学习量
     * @return 随机选择的单词列表，如果失败返回空列表
     */
    List<String> getRandomWordsByUserIdAndAmount(Integer userId, List<String> words, Integer dailyStudyAmount);





}