package com.example.englishstudy.service;

import com.example.englishstudy.entity.Word;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WordService extends IService<Word> {

    /**
     * 根据单词 ID 获取单词除单词 ID 和 bookId 外的详细信息
     * @param wordId 单词 ID
     * @return 单词详细信息
     */
    Word getWordById(Integer wordId);


    /**
     * 根据单词获取单词除单词 ID 和 bookId 外的详细信息
     * @param word 单词
     * @return 单词详细信息
     */
    Word getWordByWord(String word);

    /**
     * 获取所有不同的 book_id
     * @return 不同的 book_id 列表
     */
    List<String> getAllDistinctBookIds();


    /**
     * 根据 book_id 获取该书籍下的单词数量
     * @param bookId 书籍 ID
     * @return 该书籍下的单词数量
     */
    int getWordCountByBookId(String bookId);

    /**
     * 根据 book_id的前缀 获取该书籍下全部的单词数量
     * @param bookIdPrefix 书籍 ID
     * @return 该书籍下的单词数量
     */
    int getWordCountByBookIdPrefix(String bookIdPrefix);


    /**
     * 根据 book_id 获取该书籍下的所有单词
     * @param bookId 书籍 ID
     * @return 该书籍下的所有单词列表
     */
    List<String> getWordsByBookId(String bookId);

    /**
     * 根据 book_id 的前缀获取该书籍下全部的单词
     * @param bookIdPrefix 书籍ID前缀
     * @return 匹配前缀的所有书籍的单词列表
     */
    List<String> getWordsByBookIdPrefix(String bookIdPrefix);




    /**
     * 检查单词是否存在
     * @param word 要检查的单词
     * @return 如果单词存在返回 true，否则返回 false
     */
    boolean isWordExists(String word);



    /**
     * 根据 book_id 获取该书籍下的所有单词
     * @param prefix 单词前缀
     * @param limit 限制条数
     * @return 该书籍下的所有单词列表
     */
    List<String> searchByPrefix(String prefix, int limit);


    /**
     * 获取所有单词
     * @return 所有单词列表
     */
    List<String> getAllWords();


}