package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.UserWordSelection;
import com.example.englishstudy.entity.UserWordStudyStatus;
import com.example.englishstudy.entity.Word;
import com.example.englishstudy.mapper.UserWordSelectionMapper;
import com.example.englishstudy.mapper.UserWordStudyStatusMapper;
import com.example.englishstudy.service.UserWordSelectionService;
import com.example.englishstudy.service.UserWordStudyStatusService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserWordSelectionServiceImpl extends ServiceImpl<UserWordSelectionMapper, UserWordSelection> implements UserWordSelectionService {

    private static final Logger logger = LoggerFactory.getLogger(UserWordSelectionServiceImpl.class);

    private final UserWordStudyStatusService userWordStudyStatusService;
    private final UserWordStudyStatusMapper userWordStudyStatusMapper;
    private final WordServiceImpl wordService;


    // 构造函数注入
    public UserWordSelectionServiceImpl(UserWordStudyStatusService userWordStudyStatusService , UserWordStudyStatusMapper userWordStudyStatusMapper, WordServiceImpl wordService) {
        this.userWordStudyStatusService = userWordStudyStatusService;
        this.userWordStudyStatusMapper = userWordStudyStatusMapper;

        this.wordService = wordService;
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean selectWordsForMemory(Integer userId, List<String> words, Integer dailyStudyAmount) {
        if (userId == null || words == null || words.isEmpty()) {
            logger.warn("selectWordsForMemory 方法参数无效，用户 ID: {}, 单词列表: {}", userId, words);
            return false;
        }


        // 检查单词选择数量是否超过每日学习量
        QueryWrapper<UserWordSelection> statusQuery = new QueryWrapper<>();
        statusQuery.eq("user_id", userId)
                .eq("date", LocalDate.now())
                .eq("status", 1);

        int count = (int) this.count(statusQuery);

        if (count >= dailyStudyAmount) {
            logger.warn("用户今天已经添加了足够的单词，不能再添加，用户 ID: {}, 已添加数量: {}, 每日学习量: {}",
                    userId, count, dailyStudyAmount);
            return false;
        }


        // 获取当天需要复习的单词数量
        int reviewAmount = getReviewAmountByUserId(userId,dailyStudyAmount);

        // 计算新学单词的数量
        int newLearningAmount = Math.max(0, dailyStudyAmount - reviewAmount);

        // 检查用户选择的单词数量是否超过允许的新学单词数量
        if (words.size() > newLearningAmount) {
            logger.warn("用户选择的单词数量超过了当天允许的新学单词数量，用户 ID: {}, 选择的单词数量: {}, 允许的新学单词数量: {}",
                    userId, words.size(), newLearningAmount);
            return false;
        }

        // 使用批量查询获取单词ID列表
        List<Word> wordEntities = wordService.list(new QueryWrapper<Word>().in("word", words));
        List<Integer> wordIds = wordEntities.stream()
                .map(Word::getWordId)
                .collect(Collectors.toList());

        if (wordIds.isEmpty()) {
            logger.warn("未找到有效的单词ID，用户 ID: {}, 单词列表: {}", userId, words);
            return false;
        }

        List<UserWordSelection> userWordSelections = new ArrayList<>();
        for (Integer wordId : wordIds) {
            UserWordSelection userWordSelection = new UserWordSelection();
            userWordSelection.setUserId(userId);
            userWordSelection.setWordId(wordId);
            userWordSelection.setStatus(1);
            userWordSelection.setDate(LocalDate.now());
            userWordSelections.add(userWordSelection);
        }

        try {
            // 获取当前对象的代理
            UserWordSelectionServiceImpl proxy = (UserWordSelectionServiceImpl) AopContext.currentProxy();
            boolean isUserWordSelectionSaved = proxy.saveUserWordSelections(userWordSelections);

            if (isUserWordSelectionSaved) {
                // 在 UserWordStudyStatus 表中创建记录
                List<UserWordStudyStatus> studyStatusList = new ArrayList<>();
                for (Integer wordId : wordIds) {
                    UserWordStudyStatus studyStatus = new UserWordStudyStatus();
                    studyStatus.setUserId(userId);
                    studyStatus.setWordId(wordId);
                    studyStatus.setStudyDate(LocalDate.now());
                    studyStatus.setIsCompleted(false);
                    studyStatus.setStudyType("新学");
                    studyStatusList.add(studyStatus);
                }
                boolean statusSaved = userWordStudyStatusService.saveBatch(studyStatusList);
                if (statusSaved) {
                    logger.info("成功为用户 {} 选择 {} 个单词进行学习，单词ID列表: {}",
                            userId, wordIds.size(), wordIds);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return handleException(e, "selectWordsForMemory");
        }
    }


    @Override
    public List<String> getSelectedWordsByBookIdPrefix(Integer userId, String bookIdPrefix) {
        try {
            return baseMapper.getSelectedWordsByBookIdPrefix(userId, bookIdPrefix);
        } catch (Exception e) {
            handleException(e, "getSelectedWordsByBookIdPrefix");
            return new ArrayList<>();
        }
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> getRandomWordsByUserIdAndAmount(Integer userId, List<String> words, Integer dailyStudyAmount) {
        if (isInvalidParams(userId, dailyStudyAmount) || words == null || words.isEmpty()) {
            logger.warn("getRandomWordsByUserIdAndAmount 方法参数无效，用户 ID: {}, 单词列表: {}, 每日学习数量: {}",
                    userId, words, dailyStudyAmount);
            return Collections.emptyList();
        }

        // 检查单词选择数量是否超过每日学习量
        QueryWrapper<UserWordSelection> statusQuery = new QueryWrapper<>();
        statusQuery.eq("user_id", userId)
                .eq("date", LocalDate.now())
                .eq("status", 1);

        int count = (int) this.count(statusQuery);

        if (count >= dailyStudyAmount) {
            logger.info("用户今天已经添加了足够的单词，不能再添加，用户 ID: {}, 已添加数量: {}, 每日学习量: {}",
                    userId, count, dailyStudyAmount);
            return Collections.emptyList();
        }


        try {
            // 获取当天需要复习的单词数量
            int reviewAmount = getReviewAmountByUserId(userId, dailyStudyAmount);

            // 计算新学单词的数量
            int newLearningAmount = Math.max(0, dailyStudyAmount - reviewAmount);

            if(newLearningAmount == 0){
                logger.info("单词已选择完毕，请勿重复添加（若需要添加则请提高每日学习量）");
                return Collections.emptyList();
            }

            // 获取用户已选择的单词集合
            Set<String> existingWords = this.lambdaQuery()
                    .eq(UserWordSelection::getUserId, userId)
                    .list()
                    .stream()
                    .map(selection -> wordService.getById(selection.getWordId()).getWord())
                    .collect(Collectors.toSet());

            // 从未选择的单词中筛选
            List<String> availableWords = words.stream()
                    .filter(word -> !existingWords.contains(word))
                    .collect(Collectors.toList());

            if (availableWords.isEmpty()) {
                logger.warn("没有可用的新单词供选择，用户 ID: {}", userId);
                return Collections.emptyList();
            }

            // 随机打乱并选取指定数量的单词
            Collections.shuffle(availableWords);
            List<String> selectedWords = availableWords.stream()
                    .limit(newLearningAmount)
                    .collect(Collectors.toList());

            // 使用批量查询获取单词ID列表
            List<Word> wordEntities = wordService.list(new QueryWrapper<Word>().in("word", selectedWords));
            List<Integer> wordIds = wordEntities.stream()
                    .map(Word::getWordId)
                    .collect(Collectors.toList());

            if (wordIds.isEmpty()) {
                logger.warn("未找到有效的单词ID，用户 ID: {}, 单词列表: {}", userId, selectedWords);
                return Collections.emptyList();
            }

            // 创建用户单词选择记录
            List<UserWordSelection> userWordSelections = new ArrayList<>();
            for (Integer wordId : wordIds) {
                UserWordSelection userWordSelection = new UserWordSelection();
                userWordSelection.setUserId(userId);
                userWordSelection.setWordId(wordId);
                userWordSelection.setStatus(1);
                userWordSelection.setDate(LocalDate.now());
                userWordSelections.add(userWordSelection);
            }

            // 获取当前对象的代理
            UserWordSelectionServiceImpl proxy = (UserWordSelectionServiceImpl) AopContext.currentProxy();
            boolean isUserWordSelectionSaved = proxy.saveUserWordSelections(userWordSelections);

            if (isUserWordSelectionSaved) {
                // 创建学习状态记录
                List<UserWordStudyStatus> studyStatusList = new ArrayList<>();
                for (Integer wordId : wordIds) {
                    UserWordStudyStatus studyStatus = new UserWordStudyStatus();
                    studyStatus.setUserId(userId);
                    studyStatus.setWordId(wordId);
                    studyStatus.setStudyDate(LocalDate.now());
                    studyStatus.setIsCompleted(false);
                    studyStatus.setStudyType("新学");
                    studyStatusList.add(studyStatus);
                }
                boolean statusSaved = userWordStudyStatusService.saveBatch(studyStatusList);
                if (statusSaved) {
                    logger.info("成功为用户 {} 随机选择 {} 个单词进行学习，单词列表: {}",
                            userId, selectedWords.size(), selectedWords);
                    return selectedWords;
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            handleException(e, "getRandomWordsByUserIdAndAmount");
            return Collections.emptyList();
        }
    }


    @Override
    public int countSelectedWordsByUserId(Integer userId) {
        if (userId == null) {
            logger.warn("countSelectedWordsByUserId 方法参数无效，用户ID为null");
            return 0;
        }

        try {
            QueryWrapper<UserWordSelection> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            logger.info("尝试获取已选单词");
            int count = (int) this.count(queryWrapper);
            logger.info("获取已选单词成功, 返回单词数量: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("统计用户 {} 已选择单词数量失败", userId, e);
            return 0;
        }
    }



    private boolean isInvalidParams(Integer userId, Integer dailyLearningAmount) {
        return userId == null || dailyLearningAmount == null || dailyLearningAmount <= 0;
    }

    // 统一异常处理方法
    private boolean handleException(Exception e, String methodName) {
        logger.error("{} 方法发生错误: {}", methodName, e.getMessage(), e);
        return false;
    }

    @Transactional
    public boolean saveUserWordSelections(List<UserWordSelection> userWordSelections) {
        try {
            return saveBatch(userWordSelections);
        } catch (Exception e) {
            return handleException(e, "saveUserWordSelections");
        }
    }

    /**
     * 根据用户 ID 获取需要复习的单词数量
     * @param userId 用户 ID
     * @return 需要复习的单词数量
     */
    private int getReviewAmountByUserId(Integer userId,int dailyStudyAmount) {
        return userWordStudyStatusMapper.countPendingReviewWordsByUserId(userId,dailyStudyAmount);
    }





}