package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.UserWordMemory;
import com.example.englishstudy.entity.UserWordProgress;
import com.example.englishstudy.entity.UserWordStudyStatus;
import com.example.englishstudy.mapper.UserWordMemoryMapper;
import com.example.englishstudy.mapper.UserWordStudyStatusMapper;
import com.example.englishstudy.service.UserWordMemoryService;
import com.example.englishstudy.service.UserWordProgressService;
import com.example.englishstudy.service.UserWordStudyStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserWordMemoryServiceImpl extends ServiceImpl<UserWordMemoryMapper, UserWordMemory> implements UserWordMemoryService {

    private static final Logger logger = LoggerFactory.getLogger(UserWordMemoryServiceImpl.class);

    private final UserWordStudyStatusMapper statusMapper;
    private final UserWordProgressService progressService;
    private final UserWordStudyStatusService userWordStudyStatusService;

    public UserWordMemoryServiceImpl(UserWordStudyStatusMapper statusMapper, UserWordProgressService progressService, UserWordStudyStatusService userWordStudyStatusService) {
        this.statusMapper = statusMapper;
        this.progressService = progressService;
        this.userWordStudyStatusService = userWordStudyStatusService;
    }

    // 统一的异常处理方法
    private <T> T handleException(Exception e, String methodName, T defaultValue) {
        logger.error("{} 方法执行失败: {}", methodName, e.getMessage(), e);
        return defaultValue;
    }


    @Override
    @Transactional
    public boolean createMemoryRecord(Integer userId, Integer wordId, LocalDate studyDate) {
        try {
            // 检查参数有效性
            if (userId == null || wordId == null || studyDate == null) {
                logger.warn("创建记忆记录失败：参数不能为空，userId={}, wordId={}, studyDate={}",
                        userId, wordId, studyDate);
                return false;
            }


            // 检查是否存在符合条件的UserWordStudyStatus记录
            LambdaQueryWrapper<UserWordStudyStatus> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserWordStudyStatus::getUserId, userId)
                    .eq(UserWordStudyStatus::getWordId, wordId)
                    .le(UserWordStudyStatus::getStudyDate, studyDate)
                    .eq(UserWordStudyStatus::getIsCompleted, true);

            long count = statusMapper.selectCount(queryWrapper);
            if (count > 0) {
                // 检查是否已存在记忆记录
                LambdaQueryWrapper<UserWordMemory> memoryQuery = new LambdaQueryWrapper<>();
                memoryQuery.eq(UserWordMemory::getUserId, userId)
                        .eq(UserWordMemory::getWordId, wordId);

                // 如果不存在记忆记录，则创建新的记录
                if (this.count(memoryQuery) == 0) {
                    // 创建新的记忆记录
                    UserWordMemory memory = new UserWordMemory();
                    memory.setUserId(userId);
                    memory.setWordId(wordId);
                    memory.setReviewCount(0);


                    // 首先检查该单词是否已完成学习
                    QueryWrapper<UserWordProgress> progressQuery = new QueryWrapper<>();
                    progressQuery.eq("user_id", userId)
                            .eq("word_id", wordId)
                            .le("learning_time", studyDate)
                            .orderByDesc("learning_time")
                            .last("limit 1");

                    UserWordProgress lastProgress = progressService.getOne(progressQuery);
                    if (lastProgress!= null) {
                        memory.setLastStudyStatus(lastProgress.getStatus());
                        memory.setLastStudyDate(lastProgress.getLearningTime());
                    }

                    // 计算记忆强度
                    double strength = calculateMemoryStrength(userId, wordId);
                    memory.setMemoryStrength(strength);

                    // 计算并设置下次复习时间
                    LocalDate nextReviewTime = calculateNextReviewTime(memory);
                    memory.setNextReviewTime(nextReviewTime);


                    boolean result = this.save(memory);
                    if (!result) {
                        logger.info("创建记忆记录保存失败，userId={}, wordId={}", userId, wordId);
                        return false;
                    }
                }else {

                    // 更新已有记录
                    UserWordMemory existingMemory = this.getOne(memoryQuery);
                    if (existingMemory != null) {
                        existingMemory.setReviewCount(existingMemory.getReviewCount() + 1);

                        UserWordStudyStatus statusRecord = statusMapper.selectOne(queryWrapper);
                        if (statusRecord != null) {
                            existingMemory.setLastStudyStatus(statusRecord.getStudyStatus());
                            existingMemory.setLastStudyDate(statusRecord.getStudyDate());
                        }

                        // 计算记忆强度
                        double strength = calculateMemoryStrength(userId, wordId);
                        existingMemory.setMemoryStrength(strength);

                        // 计算并设置下次复习时间
                        LocalDate nextReviewTime = calculateNextReviewTime(existingMemory);
                        existingMemory.setNextReviewTime(nextReviewTime);

                        boolean result = this.updateById(existingMemory);
                        if (!result) {
                            logger.info("更新记忆记录保存失败，userId={}, wordId={}", userId, wordId);
                            return false;
                        }
                    }
                }
                logger.info("创建/更新记忆记录成功，userId={}, wordId={}", userId, wordId);
                return true; // 已存在记录视为成功
            }
            logger.info("创建记忆记录失败：未找到符合条件的UserWordStudyStatus记录");
            return false; // 不符合创建条件
        } catch (Exception e) {
            return handleException(e, "createMemoryRecord", false);
        }
    }


    @Override
    @Transactional
    public boolean createReviewRecord(Integer userId, Integer wordId) {
        if (userId == null || wordId == null ) {
            return false;
        }


        try {


            // 检查是否已存在记忆记录
            LambdaQueryWrapper<UserWordMemory> memoryQuery = new LambdaQueryWrapper<>();
            memoryQuery.eq(UserWordMemory::getUserId, userId)
                    .eq(UserWordMemory::getWordId, wordId);


            if (this.count(memoryQuery) > 0) {
                UserWordStudyStatus reviewRecord = new UserWordStudyStatus();
                reviewRecord.setUserId(userId);
                reviewRecord.setWordId(wordId);
                reviewRecord.setStudyType("复习"); // 学习类型为"复习"
                reviewRecord.setIsCompleted(false);



                UserWordMemory existingMemory = this.getOne(memoryQuery);

                // 计算并设置下次复习时间
                LocalDate nextReviewTime = calculateNextReviewTime(existingMemory);
                reviewRecord.setStudyDate(nextReviewTime);


                logger.info("userWordStudyStatus表创建复习记录成功，userId={}, wordId={}",userId, wordId);
                return userWordStudyStatusService.save(reviewRecord);
            }else{
                logger.info("userWordStudyStatus表创建复习记录失败，userId={}, wordId={}",userId, wordId);
                return false;
            }
        } catch (Exception e) {
            return handleException(e, "createReviewRecord", false);
        }
    }



    @Override
    public double calculateMemoryStrength(Integer userId, Integer wordId) {

        List<UserWordProgress> progressList;
        try {
            progressList = progressService.getProgressHistory(userId, wordId);
        } catch (Exception e) {
            logger.error("获取学习进度历史记录失败，userId={}, wordId={}", userId, wordId, e);
            return 0.0;
        }

        if (progressList.isEmpty()) {
            return 0.0;
        }

        // 1. 计算加权状态值
        double weightedValue = calculateWeightedStatusValue(progressList);

        // 2. 计算学习次数加成
        double countBonus = calculateStudyCountBonus(progressList.size());

        // 3. 计算时间衰减
        double retentionRate = calculateRetentionRate(
                progressList.get(0).getLearningTime(),
                progressList.size()
        );

        // 综合计算并限制在0-1之间
        return Math.min(Math.max(weightedValue * retentionRate + countBonus, 0), 1.0);
    }




    // 辅助方法：计算加权状态值
    private double calculateWeightedStatusValue(List<UserWordProgress> progressList) {
        double[] weights = {0.5, 0.3, 0.2}; // 权重分配：最近一次50%，前两次30%和20%
        double weightedSum = 0;

        for (int i = 0; i < Math.min(3, progressList.size()); i++) {
            String status = progressList.get(i).getStatus();
            weightedSum += getStatusValue(status) * weights[i];
        }
        return weightedSum;
    }

    // 辅助方法：获取状态基础值
    private double getStatusValue(String status) {
        return switch (status) {
            case "认识" -> 1.0;  // 完全掌握
            case "模糊" -> 0.6;  // 部分掌握
            case "忘记" -> 0.3;  // 未掌握
            default -> 0.5;     // 默认值
        };
    }

    // 辅助方法：计算学习次数加成
    private double calculateStudyCountBonus(int studyCount) {
        if (studyCount <= 3) return studyCount * 0.08;       // 初期快速提升
        if (studyCount <= 7) return 0.24 + (studyCount - 3) * 0.05; // 中期平稳提升
        return 0.44 + (studyCount - 7) * 0.02;              // 后期缓慢提升
    }

    // 辅助方法：计算记忆保留率(基于遗忘曲线)
    private double calculateRetentionRate(LocalDate lastStudyDate, int studyCount) {
        long days = ChronoUnit.DAYS.between(lastStudyDate, LocalDate.now());
        double stability = 3 + studyCount * 1.5; // 记忆稳定性系数
        return Math.exp(-days / stability);
    }


    @Override
    public LocalDate calculateNextReviewTime(UserWordMemory memory) {
        // 1. 参数校验
        if (memory == null || memory.getMemoryStrength() == null
                || memory.getReviewCount() == null
                || memory.getLastStudyStatus() == null ) {
            return LocalDate.now().plusDays(1); // 默认返回明天
        }

        // 2. 获取基础间隔天数（基于记忆强度）
        int baseInterval = getBaseInterval(memory.getMemoryStrength());

        // 3. 根据最近学习状态调整间隔
        //    - 考虑学习状态和复习次数的影响
        int adjustedInterval = adjustByLastStatus(
            baseInterval,
            memory.getLastStudyStatus(),
            memory.getReviewCount()
        );

        // 4. 动态边界控制
        //    - 最大间隔随复习次数增加而增大（30-90天）
        //    - 最小间隔随复习次数增加而减小（3-1天）
        int maxInterval = Math.min(90, 30 + memory.getReviewCount() * 5);
        int minInterval = Math.max(1, 3 - memory.getReviewCount() / 5);

        // 5. 应用边界限制并计算最终间隔
        int finalInterval = Math.max(minInterval, Math.min(adjustedInterval, maxInterval));

        // 6. 返回下次复习日期（当前日期+最终间隔）
        return LocalDate.now().plusDays(finalInterval);
    }

    /**
     * 根据记忆强度获取基础复习间隔
     * @param strength 记忆强度(0.0-1.0)
     * @return 基础间隔天数
     */
    private int getBaseInterval(double strength) {
        // 记忆强度与间隔的映射关系
        if (strength >= 0.95) return 45;  // 非常牢固：1.5个月
        if (strength >= 0.9) return 30;   // 牢固：1个月
        if (strength >= 0.8) return 21;   // 良好：3周
        if (strength >= 0.7) return 14;   // 较好：2周
        if (strength >= 0.6) return 10;   // 一般：10天
        if (strength >= 0.5) return 7;    // 普通：1周
        if (strength >= 0.4) return 5;    // 较弱：5天
        if (strength >= 0.3) return 3;    // 薄弱：3天
        if (strength >= 0.2) return 2;    // 很差：2天
        return 1;                         // 极差：每天
    }

    /**
     * 根据最近学习状态调整复习间隔
     * @param interval 基础间隔
     * @param status 最近学习状态（认识/模糊/忘记）
     * @param reviewCount 复习次数
     * @return 调整后的间隔
     */
    private int adjustByLastStatus(int interval, String status, int reviewCount) {
        // 1. 计算调整系数
        double adjustment = switch (status) {
            case "认识" -> 1.0 + (0.15 - 0.02 * Math.min(reviewCount, 10)); // 认识：+15%~-5%
            case "模糊" -> 1.0;               // 模糊：保持原样
            case "忘记" -> 0.6 - 0.03 * Math.min(reviewCount, 10); // 忘记：-40%~-70%
            default -> 1.0;                 // 默认：保持原样
        };

        // 2. 限制调整系数在0.3-1.7之间
        adjustment = Math.max(0.3, Math.min(adjustment, 1.7));

        // 3. 应用调整并四舍五入
        return (int) Math.round(interval * adjustment);
    }

    @Override
    public Map<LocalDate, Double> getDailyAverageMemoryStrength(Integer userId, LocalDate startDate, LocalDate endDate) {
        // 参数校验
        if (userId == null || startDate == null || endDate == null) {
            logger.warn("参数不能为空: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
            return Collections.emptyMap();
        }

        // 日期范围校验
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        try {
            // 查询指定日期范围内的记忆记录
            QueryWrapper<UserWordMemory> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("DATE(last_study_date) as study_date", "AVG(memory_strength) as avg_strength")
                    .eq("user_id", userId)
                    .between("last_study_date", startDate, endDate)
                    .groupBy("DATE(last_study_date)")
                    .orderByAsc("DATE(last_study_date)");

            List<Map<String, Object>> result = this.listMaps(queryWrapper);

            // 转换为Map<LocalDate, Double>
            return result.stream()
                    .collect(Collectors.toMap(
                            map -> LocalDate.parse(map.get("study_date").toString()),
                            map -> Double.parseDouble(map.get("avg_strength").toString())
                    ));
        } catch (Exception e) {
            logger.error("统计每日平均记忆强度失败，userId: {}, startDate: {}, endDate: {}",
                    userId, startDate, endDate, e);
            return Collections.emptyMap();
        }
    }



}