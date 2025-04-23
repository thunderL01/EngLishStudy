package com.example.englishstudy.serviceimpl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.UserWordStudyInterval;
import com.example.englishstudy.entity.UserWordStudyStatus;
import com.example.englishstudy.entity.Word;

import com.example.englishstudy.mapper.UserWordStudyStatusMapper;
import com.example.englishstudy.service.UserWordProgressService;
import com.example.englishstudy.service.UserWordStudyIntervalService;
import com.example.englishstudy.service.UserWordStudyStatusService;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserWordStudyStatusServiceImpl extends ServiceImpl<UserWordStudyStatusMapper, UserWordStudyStatus> implements UserWordStudyStatusService {

    // 定义常量
    private static final String STUDY_STATUS_KNOWN = "认识";
    private static final String STUDY_STATUS_VAGUE = "模糊";
    private static final String STUDY_STATUS_FORGOTTEN = "忘记";

    private static final Logger logger = LoggerFactory.getLogger(UserWordStudyStatusServiceImpl.class);


    private final UserWordStudyIntervalService userWordStudyIntervalService;
    private final UserWordProgressService userWordProgressService;

    public UserWordStudyStatusServiceImpl(UserWordStudyIntervalService userWordStudyIntervalService, UserWordProgressService userWordProgressService) {

        this.userWordStudyIntervalService = userWordStudyIntervalService;
        this.userWordProgressService = userWordProgressService;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean recordLearning(Integer userId, Integer wordId, String studyStatus) {
        logger.info("开始记录学习状态，用户ID: {}, 单词ID: {}, 学习状态: {}", userId, wordId, studyStatus);

        // 1. 查询所有学习记录（1-4次）
        UserWordStudyInterval record1 = getStudyRecord(userId, wordId, 1);
        UserWordStudyInterval record2 = getStudyRecord(userId, wordId, 2);
        UserWordStudyInterval record3 = getStudyRecord(userId, wordId, 3);
        UserWordStudyInterval record4 = getStudyRecord(userId, wordId, 4);

        // 2. 获取当前学习状态记录
        UserWordStudyStatus userWordStudyStatus = getCurrentStudyStatus(userId, wordId);

        // 3. 确定当前学习次数
        StudyContext context = determineStudyContext(record1, record2, record3, record4);

        logger.info("学习次数context: {}", context.currentStudyCount);

        // 4. 根据学习次数处理不同逻辑
        try {
            // 处理第一次学习
            if (context.currentStudyCount == 0) {
                if (userWordStudyStatus == null) {
                    logger.info("第一次学习但找不到学习状态记录，userId: {}, wordId: {}", userId, wordId);
                    return false;
                }

                logger.info("第一次学习,正在记录学习状态");
                return this.handleFirstLearning(userWordStudyStatus, studyStatus);
            }

            return switch (context.currentStudyCount) {
                // 处理第二次学习
                case 1 -> handleSecondLearning(userWordStudyStatus,
                        record1.getStudyStatus(),
                        studyStatus);
                // 处理第三次学习
                case 2 -> handleThirdLearning(userWordStudyStatus,
                        record1.getStudyStatus(),
                        record2.getStudyStatus(),
                        studyStatus);
                // 处理第四次学习
                case 3 -> handleFourthLearning(userWordStudyStatus,
                        record1.getStudyStatus(),
                        record2.getStudyStatus(),
                        record3.getStudyStatus(),
                        studyStatus);
                // 处理第五次学习
                case 4 -> handleFifthLearning(userWordStudyStatus,
                        record1.getStudyStatus(),
                        record2.getStudyStatus(),
                        record3.getStudyStatus(),
                        record4.getStudyStatus(),
                        studyStatus);
                default -> {
                    logger.error("无效的学习次数: {}", context.currentStudyCount);
                    yield false;
                }
            };
        } catch (Exception e) {
            logger.error("记录学习状态时发生异常", e);
            throw e;
        } finally {
            // 5. 保存最终状态
            if (userWordStudyStatus != null) {
                boolean isUpdate = this.updateById(userWordStudyStatus);
                if (isUpdate) {
                    logger.info("学习状态记录已更新: {}", userWordStudyStatus);
                }
                else {
                    logger.info("更新学习状态失败: {}", userWordStudyStatus);
                }
            }
        }
    }

    // 辅助方法：获取学习记录
    private UserWordStudyInterval getStudyRecord(Integer userId, Integer wordId, int studyCount) {
        QueryWrapper<UserWordStudyInterval> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .apply("DATE(study_date) = {0}", LocalDate.now())
                .eq("study_count", studyCount);
        return userWordStudyIntervalService.getOne(queryWrapper);
    }

    // 辅助方法：获取当前学习状态
    private UserWordStudyStatus getCurrentStudyStatus(Integer userId, Integer wordId) {
        QueryWrapper<UserWordStudyStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("word_id", wordId)
                .eq("is_completed", false)
                .le("user_word_study_status_date", LocalDate.now()); // 添加小于等于今天的日期条件

        return this.getOne(queryWrapper);
    }

    // 辅助类：学习上下文
    private static class StudyContext {
        int currentStudyCount;
        UserWordStudyInterval existingRecord;
    }

    // 辅助方法：确定学习上下文
    private StudyContext determineStudyContext(UserWordStudyInterval record1,
                                               UserWordStudyInterval record2,
                                               UserWordStudyInterval record3,
                                               UserWordStudyInterval record4) {
        StudyContext context = new StudyContext();

        // 从高到低检查学习记录
        if (record4 != null) {
            context.currentStudyCount = 4;
            context.existingRecord = record4;
        } else if (record3 != null) {
            context.currentStudyCount = 3;
            context.existingRecord = record3;
        } else if (record2 != null) {
            context.currentStudyCount = 2;
            context.existingRecord = record2;
        } else if (record1 != null) {
            context.currentStudyCount = 1;
            context.existingRecord = record1;
        }else if(record1 == null && record2 == null && record3 == null && record4 == null) {
            context.currentStudyCount = 0;
            context.existingRecord = null;
        }

        return context;
    }




    // ================ 第一次学习处理 ================
    private boolean handleFirstLearning(UserWordStudyStatus status,
                                         String currentStatus) {

        switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  //  认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 1.0, true);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.5, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.3, false);
                    break;
                default:
                    return false;
            }

        return true;
    }



    // ================ 第二次学习处理 ================
    private boolean handleSecondLearning(UserWordStudyStatus status,
                                         String firstStudyStatus,
                                         String currentStatus) {
        // 第一次为模糊状态
        if (STUDY_STATUS_VAGUE.equals(firstStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.8, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.6, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.4, false);
                    break;
                default:
                    return false;
            }
        }
        // 第一次为忘记状态
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.6, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.4, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.2, false);
                    break;
                default:
                    return false;
            }
        }
        return true;
    }


    // ================ 第三次学习处理 ================
    private boolean handleThirdLearning(UserWordStudyStatus status,
                                        String firstStudyStatus,
                                        String secondStudyStatus,
                                        String currentStatus) {
        // 模糊 -> 认识
        if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_KNOWN.equals(secondStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 认识 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 1.0, true);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 认识 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.9, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 认识 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.7, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 模糊
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_VAGUE.equals(secondStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 模糊 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 1.0, true);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 模糊 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.8, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 模糊 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.5, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 忘记
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 忘记 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.8, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 忘记 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.6, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 忘记 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.3, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 认识
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_KNOWN.equals(secondStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 认识 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.8, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 认识 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.7, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 认识 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.6, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 模糊
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_VAGUE.equals(secondStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 模糊 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.7, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 模糊 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.6, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 模糊 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.4, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 忘记
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 忘记 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.5, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 忘记 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.4, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 忘记 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.2, false);
                    break;
                default:
                    return false;
            }
        }
        return true;
    }


    // ================ 第四次学习处理 ================
    private boolean handleFourthLearning(UserWordStudyStatus status,
                                         String firstStudyStatus,
                                         String secondStudyStatus,
                                         String thirdStudyStatus,
                                         String currentStatus) {
        // 模糊 -> 认识 -> 模糊
        if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_KNOWN.equals(secondStudyStatus)
                && STUDY_STATUS_VAGUE.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 认识 -> 模糊 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 1.0, true);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 认识 -> 模糊 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 1.0, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 认识 -> 模糊 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.7, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 认识 -> 忘记
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_KNOWN.equals(secondStudyStatus)
                && STUDY_STATUS_FORGOTTEN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 认识 -> 忘记 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 1.0, true);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 认识 -> 忘记 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.9, true);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 认识 -> 忘记 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.6, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 模糊 -> 模糊
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_VAGUE.equals(secondStudyStatus)
                && STUDY_STATUS_VAGUE.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 模糊 -> 模糊 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 1.0, true);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 模糊 -> 模糊 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 1.0, true);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 模糊 -> 模糊 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.8, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 模糊 -> 忘记
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_VAGUE.equals(secondStudyStatus)
                && STUDY_STATUS_FORGOTTEN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 模糊 -> 忘记 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.8, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 模糊 -> 忘记 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.7, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 模糊 -> 忘记 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.5, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 忘记 -> 认识
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)
                && STUDY_STATUS_KNOWN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 忘记 -> 认识 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.9, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 忘记 -> 认识 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.8, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 忘记 -> 认识 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.7, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 忘记 -> 模糊
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)
                && STUDY_STATUS_VAGUE.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 忘记 -> 模糊 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.8, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 忘记 -> 模糊 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.7, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 忘记 -> 模糊 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.6, false);
                    break;
                default:
                    return false;
            }
        }
        // 模糊 -> 忘记 -> 忘记
        else if (STUDY_STATUS_VAGUE.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)
                && STUDY_STATUS_FORGOTTEN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 模糊 -> 忘记 -> 忘记 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.6, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 模糊 -> 忘记 -> 忘记 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.5, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 模糊 -> 忘记 -> 忘记 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.3, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 认识 -> 认识
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_KNOWN.equals(secondStudyStatus)
                && STUDY_STATUS_KNOWN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 认识 -> 认识 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 1.0, true);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 认识 -> 认识 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.9, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 认识 -> 认识 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.8, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 认识 -> 模糊
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_KNOWN.equals(secondStudyStatus)
                && STUDY_STATUS_VAGUE.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 认识 -> 模糊 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.9, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 认识 -> 模糊 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.8, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 认识 -> 模糊 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.7, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 认识 -> 忘记
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_KNOWN.equals(secondStudyStatus)
                && STUDY_STATUS_FORGOTTEN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 认识 -> 忘记 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.8, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 认识 -> 忘记 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.7, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 认识 -> 忘记 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.6, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 模糊 -> 认识
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_VAGUE.equals(secondStudyStatus)
                && STUDY_STATUS_KNOWN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                case STUDY_STATUS_KNOWN:  // 忘记 -> 模糊 -> 认识 -> 认识
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.9, false);
                    break;
                case STUDY_STATUS_VAGUE:  // 忘记 -> 模糊 -> 认识 -> 模糊
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.8, false);
                    break;
                case STUDY_STATUS_FORGOTTEN:  // 忘记 -> 模糊 -> 认识 -> 忘记
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.7, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 模糊 -> 模糊
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_VAGUE.equals(secondStudyStatus)
                && STUDY_STATUS_VAGUE.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                // 忘记 -> 模糊 -> 模糊 -> 认识
                case STUDY_STATUS_KNOWN:
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.8, false);
                    break;
                // 忘记 -> 模糊 -> 模糊 -> 模糊
                case STUDY_STATUS_VAGUE:
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.7, false);
                    break;
                // 忘记 -> 模糊 -> 模糊 -> 忘记
                case STUDY_STATUS_FORGOTTEN:
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.5, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 模糊 -> 忘记
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_VAGUE.equals(secondStudyStatus)
                && STUDY_STATUS_FORGOTTEN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                // 忘记 -> 模糊 -> 忘记 -> 认识
                case STUDY_STATUS_KNOWN:
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.7, false);
                    break;
                // 忘记 -> 模糊 -> 忘记 -> 模糊
                case STUDY_STATUS_VAGUE:
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.6, false);
                    break;
                // 忘记 -> 模糊 -> 忘记 -> 忘记
                case STUDY_STATUS_FORGOTTEN:
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.4, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 忘记 -> 认识
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)
                && STUDY_STATUS_KNOWN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                // 忘记 -> 忘记 -> 认识 -> 认识
                case STUDY_STATUS_KNOWN:
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.7, false);
                    break;
                // 忘记 -> 忘记 -> 认识 -> 模糊
                case STUDY_STATUS_VAGUE:
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.6, false);
                    break;
                // 忘记 -> 忘记 -> 认识 -> 忘记
                case STUDY_STATUS_FORGOTTEN:
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.5, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 忘记 -> 模糊
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)
                && STUDY_STATUS_VAGUE.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                // 忘记 -> 忘记 -> 模糊 -> 认识
                case STUDY_STATUS_KNOWN:
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.6, false);
                    break;
                // 忘记 -> 忘记 -> 模糊 -> 模糊
                case STUDY_STATUS_VAGUE:
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.5, false);
                    break;
                // 忘记 -> 忘记 -> 模糊 -> 忘记
                case STUDY_STATUS_FORGOTTEN:
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.3, false);
                    break;
                default:
                    return false;
            }
        }
        // 忘记 -> 忘记 -> 忘记
        else if (STUDY_STATUS_FORGOTTEN.equals(firstStudyStatus) && STUDY_STATUS_FORGOTTEN.equals(secondStudyStatus)
                && STUDY_STATUS_FORGOTTEN.equals(thirdStudyStatus)) {
            switch (currentStatus) {
                // 忘记 -> 忘记 -> 忘记 -> 认识
                case STUDY_STATUS_KNOWN:
                    updateStatus(status, STUDY_STATUS_KNOWN, 0.5, false);
                    break;
                // 忘记 -> 忘记 -> 忘记 -> 模糊
                case STUDY_STATUS_VAGUE:
                    updateStatus(status, STUDY_STATUS_VAGUE, 0.4, false);
                    break;
                // 忘记 -> 忘记 -> 忘记 -> 忘记
                case STUDY_STATUS_FORGOTTEN:
                    updateStatus(status, STUDY_STATUS_FORGOTTEN, 0.2, false);
                    break;
                default:
                    return false;
            }
        }
        return true;
    }


    // ================第五次学习处理 ================
    private boolean handleFifthLearning(UserWordStudyStatus status,
                                        String firstStudyStatus,
                                        String secondStudyStatus,
                                        String thirdStudyStatus,
                                        String fourthStudyStatus,
                                        String currentStatus) {
        // 第五次学习统一处理：完成度1.0，标记为已完成
        updateStatus(status, currentStatus, 1.0, true);
        return true;
    }



        // ================ 统一状态更新方法 ================
    private void updateStatus(UserWordStudyStatus status,
                              String newStatus,
                              double completionDegree,
                              boolean isCompleted) {
        status.setStudyStatus(newStatus);
        status.setCompletionDegree(completionDegree);
        status.setIsCompleted(isCompleted);
    }



    @Override
    public int countWordsToLearn(Integer userId, int dailyStudyAmount) {
        return baseMapper.countWordsToLearn(userId, dailyStudyAmount);
    }

    @Override
    public int countCompletedNewStudyRecords(Integer userId, LocalDate studyDate) {
        try {
            LambdaQueryWrapper<UserWordStudyStatus> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserWordStudyStatus::getUserId, userId)
                    .eq(UserWordStudyStatus::getStudyDate, studyDate)
                    .eq(UserWordStudyStatus::getStudyType, "新学")
                    .eq(UserWordStudyStatus::getIsCompleted, true);
            int count = (int) this.count(queryWrapper);
            logger.info("统计新学单词数量成功，userId: {}, studyDate: {}, count: {}", userId, studyDate, count);
            return count;
        } catch (Exception e) {
            logger.error("统计新学单词数量失败，userId: {}, studyDate: {}", userId, studyDate, e);
            return 0;
        }
    }

    @Override
    public int countCompletedReviewRecords(Integer userId, LocalDate studyDate) {
        try {
            LambdaQueryWrapper<UserWordStudyStatus> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserWordStudyStatus::getUserId, userId)
                    .eq(UserWordStudyStatus::getStudyDate, studyDate)
                    .eq(UserWordStudyStatus::getStudyType, "复习")
                    .eq(UserWordStudyStatus::getIsCompleted, true);
            int count = (int) this.count(queryWrapper);
            logger.info("统计复习单词数量成功，userId: {}, studyDate: {}, count: {}", userId, studyDate, count);
            return count;
        } catch (Exception e) {
            logger.error("统计复习单词数量失败，userId: {}, studyDate: {}", userId, studyDate, e);
            return 0;
        }
    }

    @Override
    public Map<LocalDate, Integer> getNewStudyStats(Integer userId, LocalDate startDate, LocalDate endDate) {

        // 添加日期范围校验
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        // 限制最大查询范围(30天)
        if (ChronoUnit.DAYS.between(startDate, endDate) > 30) {
            throw new IllegalArgumentException("查询日期范围不能超过30天");
        }

        QueryWrapper<UserWordStudyStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_word_study_status_date", "count(*) as count")
                .eq("user_id", userId)
                .eq("is_completed", true)
                .eq("study_type", "新学")
                .between("user_word_study_status_date", startDate, endDate)
                .groupBy("user_word_study_status_date")
                .orderByAsc("user_word_study_status_date"); // 添加排序

        List<Map<String, Object>> result = this.listMaps(queryWrapper);

        return result.stream()
                .collect(Collectors.toMap(
                        map -> LocalDate.parse(map.get("user_word_study_status_date").toString()),
                        map -> Integer.parseInt(map.get("count").toString())
                ));
    }

    @Override
    public Map<LocalDate, Integer> getReviewStats(Integer userId, LocalDate startDate, LocalDate endDate) {

        // 添加日期范围校验
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        // 限制最大查询范围(30天)
        if (ChronoUnit.DAYS.between(startDate, endDate) > 30) {
            throw new IllegalArgumentException("查询日期范围不能超过30天");
        }

        QueryWrapper<UserWordStudyStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_word_study_status_date", "count(*) as count")
                .eq("user_id", userId)
                .eq("is_completed", true)
                .eq("study_type", "复习")
                .between("user_word_study_status_date", startDate, endDate)
                .groupBy("user_word_study_status_date")
                .orderByAsc("user_word_study_status_date");

        List<Map<String, Object>> result = this.listMaps(queryWrapper);

        return result.stream()
                .collect(Collectors.toMap(
                        map -> LocalDate.parse(map.get("user_word_study_status_date").toString()),
                        map -> Integer.parseInt(map.get("count").toString())
                ));
    }


    @Override
    public String getWordsToLearnByPriority(Integer userId) {

        String result = baseMapper.getWordsToLearnByPriority(userId);
        logger.info("获取未完成学习的单词成功, 返回单词:{} ", result);
        return baseMapper.getWordsToLearnByPriority(userId);
    }

    @Override
    public int countPendingReviewWordsByUserId(Integer userId, int dailyStudyAmount) {
        if (userId == null || dailyStudyAmount <= 0) {
            logger.warn("countPendingReviewWordsByUserId 参数无效，userId: {}, dailyStudyAmount: {}",
                    userId, dailyStudyAmount);
            return 0;
        }

        try {
            logger.info("尝试获取未完成学习的单词数量");
            int result = baseMapper.countPendingReviewWordsByUserId(userId, dailyStudyAmount);
            logger.info("获取未完成学习的单词数量成功, 返回单词数量: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("统计用户 {} 待复习单词数量失败", userId, e);
            return 0;
        }
    }


}
