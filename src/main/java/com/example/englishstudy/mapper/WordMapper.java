package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WordMapper extends BaseMapper<Word> {
    // 目前无自定义方法

    /**
     * 根据单词 ID 获取单词除单词 ID 和 bookId 外的详细信息
     * @param wordId 单词 ID
     * @return 单词详细信息
     */
    @Select("SELECT word, uk_phonetic, uk_pronunciation_url, us_phonetic, us_pronunciation_url " +
            "FROM word " +
            "WHERE word_id= #{wordId}")
    Word getWordByWordId(@Param("wordId") Integer wordId);


    /**
     * 根据单词获取单词除单词 ID 和 bookId 外的详细信息
     * @param word 单词
     * @return 单词详细信息
     */
    @Select("SELECT word_id, word, book_id, uk_phonetic, uk_pronunciation_url, us_phonetic, us_pronunciation_url " +
            "FROM word " +
            "WHERE word = #{word}")
    Word getWordByWord(@Param("word") String word);



    /**
     * 获取所有不同的 book_id
     * @return 不同的 book_id 列表
     */
    @Select("SELECT DISTINCT CAST(book_id AS CHAR) FROM word")
    List<String> getAllDistinctBookIds();


    /**
     * 根据 book_id获取该书籍下的单词数量
     * @param bookId 书籍 ID
     * @return 该书籍下的单词数量
     */
    @Select("SELECT COUNT(*) FROM word WHERE book_id = #{bookId}")
    int getWordCountByBookId(@Param("bookId") String bookId);

    /**
     * 根据 book_id 前缀获取匹配书籍的单词总数量
     * @param bookIdPrefix 书籍ID前缀(如"CET4")
     * @return 匹配前缀的所有书籍的单词总数量
     */
    @Select("SELECT COUNT(*) FROM word WHERE book_id LIKE CONCAT(#{bookIdPrefix}, '%')")
    int getWordCountByBookIdPrefix(@Param("bookIdPrefix") String bookIdPrefix);


    /**
     * 根据 book_id 获取该书籍下的所有单词
     * @param bookId 书籍 ID
     * @return 该书籍下的所有单词列表
     */
    @Select("SELECT word FROM word WHERE book_id = #{bookId}")
    List<String> getWordsByBookId(@Param("bookId") String bookId);

    /**
     * 根据 book_id 前缀获取匹配书籍的所有单词
     * @param bookIdPrefix 书籍ID前缀
     * @return 匹配前缀的所有书籍的单词列表
     */
    @Select("SELECT word FROM word WHERE book_id LIKE CONCAT(#{bookIdPrefix}, '%')")
    List<String> getWordsByBookIdPrefix(@Param("bookIdPrefix") String bookIdPrefix);




    /**
     * 前缀搜索（使用MyBatis-Plus的注解方式）
     * @param prefix 前缀
     * @param limit 返回条数
     */
    @Select("SELECT word FROM word WHERE word LIKE #{prefix} LIMIT #{limit}")
    List<String> searchByPrefix(@Param("prefix") String prefix,
                                @Param("limit") Integer limit);

    /**
     * 获取所有单词
     * @return 所有单词列表
     */
    @Select("SELECT word FROM word")
    List<String> getAllWords();

}