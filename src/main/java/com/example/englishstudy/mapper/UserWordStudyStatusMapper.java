package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.UserWordStudyStatus;
import com.example.englishstudy.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserWordStudyStatusMapper extends BaseMapper<UserWordStudyStatus> {
    // 可以根据具体需求自定义一些SQL查询方法，扩展MyBatis-Plus的功能


    /**
     * 根据用户 ID 统计需要复习且未完成复习的单词数量
     * @param userId 用户 ID
     * @param dailyStudyAmount 每日学习量限制
     * @return 需要复习且未完成复习的单词数量
     */
    @Select("SELECT COUNT(*) FROM (" +
            "SELECT 1 " +
            "FROM user_word_study_status " +
            "WHERE user_id = #{userId} " +
            "AND user_word_study_status_date <= CURDATE() " +
            "AND is_completed = false " +
            "LIMIT #{dailyStudyAmount}" +
            ") t")
    int countPendingReviewWordsByUserId(@Param("userId") Integer userId, @Param("dailyStudyAmount") int dailyStudyAmount);


    /**
     * 根据用户 ID 统计需要学习的单词数量，最多返回 dailyStudyAmount 个
     * @param userId 用户 ID
     * @param dailyStudyAmount 每日学习数量上限
     * @return 需要学习的单词数量
     */
    @Select("SELECT COUNT(*) FROM ( " +
            "SELECT 1 FROM user_word_study_status " +
            "WHERE user_id = #{userId} " +
            "AND is_completed = false " +
            "AND user_word_study_status_date = CURRENT_DATE " +
            "LIMIT #{dailyStudyAmount} " +
            ") tmp")
    int countWordsToLearn(@Param("userId") Integer userId, @Param("dailyStudyAmount") int dailyStudyAmount);

    /**
     * 根据学习规则获取需要学习的单词
     * 1. 优先级规则：忘记 > 模糊 > 记忆
     * 2. 考虑学习间隔：只返回remaining_interval <= 0的单词
     * 3. 排序规则：
     *    - 首先按学习状态优先级排序
     *    - 相同优先级的按学习日期排序
     * 确保只获取每个单词的最新学习记录（通过子查询）
     * 当所有单词都是新单词时，按单词ID排序保证结果确定性
     * 完整的优先级排序逻辑（忘记 > 模糊 > 记忆）
     * 正确处理学习间隔为NULL或0的情况
     *
     * @param userId 用户ID，用于查询特定用户的学习记录
     * @return 符合条件且按优先级排序的单词
     */
    @Select("SELECT w.word " +
            "FROM user_word_study_status uwss " +
            "JOIN word w ON uwss.word_id = w.word_id " +
            "LEFT JOIN (" +
            "   SELECT uwsi1.* FROM user_word_study_interval uwsi1 " +
            "   INNER JOIN (" +
            "       SELECT user_id, word_id, MAX(study_date) as max_date " +
            "       FROM user_word_study_interval " +
            "       WHERE user_id = #{userId}" +
            "       GROUP BY user_id, word_id" +
            "   ) latest ON uwsi1.user_id = latest.user_id " +
            "           AND uwsi1.word_id = latest.word_id " +
            "           AND uwsi1.study_date = latest.max_date" +
            ") uwsi ON uwss.user_id = uwsi.user_id AND uwss.word_id = uwsi.word_id " +
            "WHERE uwss.user_id = #{userId} " +
            "AND uwss.is_completed = false " +
            "AND uwss.user_word_study_status_date <= CURDATE() " +
            "AND (" +

            "   (uwsi.remaining_interval = 0 AND DATE(uwsi.study_date) <= CURRENT_DATE) " +
            "   OR " +

            "   uwsi.user_id IS NULL " +
            "   OR " +

            "   (NOT EXISTS (" +
            "       SELECT 1 FROM user_word_study_status uwss2 " +
            "       LEFT JOIN (" +
            "           SELECT uwsi3.* FROM user_word_study_interval uwsi3 " +
            "           INNER JOIN (" +
            "               SELECT user_id, word_id, MAX(study_date) as max_date " +
            "               FROM user_word_study_interval " +
            "               WHERE user_id = #{userId}" +
            "               GROUP BY user_id, word_id" +
            "           ) latest3 ON uwsi3.user_id = latest3.user_id " +
            "                   AND uwsi3.word_id = latest3.word_id " +
            "                   AND uwsi3.study_date = latest3.max_date" +
            "       ) uwsi2 ON uwss2.user_id = uwsi2.user_id AND uwss2.word_id = uwsi2.word_id " +
            "       WHERE uwss2.user_id = #{userId} " +
            "       AND uwss2.is_completed = false " +
            "       AND uwss2.user_word_study_status_date <= CURDATE() " +
            "       AND (uwsi2.remaining_interval = 0 OR uwsi2.user_id IS NULL)" +
            "   ))" +
            ") " +
            "ORDER BY " +

            "  CASE " +
            "    WHEN uwsi.remaining_interval = 0 AND DATE(uwsi.study_date) <= CURRENT_DATE THEN 0 " +
            "    WHEN uwsi.user_id IS NULL THEN 1 " +
            "    ELSE 2 " +
            "  END, " +

            "  CASE " +
            "    WHEN uwsi.study_status = '忘记' THEN 1 " +
            "    WHEN uwsi.study_status = '模糊' THEN 2 " +
            "    WHEN uwsi.study_status = '认识' THEN 3 " +
            "    ELSE 4 " +
            "  END, " +

            "  COALESCE(uwsi.study_date, TIMESTAMP(CURRENT_DATE)) ASC, " +

            "  w.word_id ASC " +
            "LIMIT 1")
    String getWordsToLearnByPriority(@Param("userId") Integer userId);


}