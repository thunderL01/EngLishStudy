package com.example.englishstudy.serviceimpl;


import com.example.englishstudy.entity.Word;

import com.example.englishstudy.mapper.WordMapper;
import com.example.englishstudy.service.WordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordServiceImpl extends ServiceImpl<WordMapper, Word> implements WordService {

    private static final Logger logger = LoggerFactory.getLogger(WordServiceImpl.class);

    @Override
    public Word getWordById(Integer wordId) {
        try {
            logger.info("尝试获取单词信息: wordId={}", wordId);
            Word word = this.baseMapper.getWordByWordId(wordId);
            logger.info("获取单词信息成功: wordId={}, 返回数据: {}", wordId, word);
            return word;
        } catch (Exception e) {
            logger.error("根据ID获取单词失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public Word getWordByWord(String word) {
        try {
            logger.info("尝试获取单词信息: word={}", word);
            Word result = this.baseMapper.getWordByWord(word);
            logger.info("获取单词信息成功: word={}, 返回数据: {}", word, result);
            return result;
        } catch (Exception e) {
            logger.error("根据单词获取信息失败, word: {}", word, e);
            return null;
        }
    }

    @Override
    public List<String> getAllDistinctBookIds() {
        try {
            logger.info("尝试获取所有不同的书籍ID");
            List<String> bookIds = this.baseMapper.getAllDistinctBookIds();
            logger.info("获取所有不同书籍ID成功, 返回数据: {}", bookIds);
            return bookIds;
        } catch (Exception e) {
            logger.error("获取所有不同书籍ID失败", e);
            return null;
        }
    }

    @Override
    public int getWordCountByBookId(String bookId) {
        try {
            logger.info("尝试获取书籍ID {} 的单词数量", bookId);
            int count = this.baseMapper.getWordCountByBookId(bookId);
            logger.info("获取书籍单词数量成功: bookId={}, 返回数量: {}", bookId, count);
            return count;
        } catch (Exception e) {
            logger.error("根据书籍ID获取单词数量失败, bookId: {}", bookId, e);
            return 0;
        }
    }


    @Override
    public int getWordCountByBookIdPrefix(String bookIdPrefix) {
        try {
            logger.info("尝试获取书籍ID前缀为 {} 的单词数量", bookIdPrefix);
            int count = this.baseMapper.getWordCountByBookIdPrefix(bookIdPrefix);
            logger.info("获取书籍单词数量成功: 前缀为bookId={}, 返回数量: {}", bookIdPrefix, count);
            return count;
        } catch (Exception e) {
            logger.error("根据书籍ID获取单词数量失败, bookId: {}", bookIdPrefix, e);
            return 0;
        }
    }


    @Override
    public List<String> getWordsByBookId(String bookId) {
        try {
            logger.info("尝试获取书籍ID {} 的单词列表", bookId);
            List<String> words = this.baseMapper.getWordsByBookId(bookId);
            logger.info("获取书籍单词列表成功: bookId={}, 返回单词数量: {}", bookId, words != null ? words.size() : 0);
            return words;
        } catch (Exception e) {
            logger.error("根据书籍ID获取单词列表失败, bookId: {}", bookId, e);
            return null;
        }
    }

    @Override
    public List<String> getWordsByBookIdPrefix(String bookIdPrefix) {
        try {
            logger.info("尝试获取书籍ID前缀为 {} 的单词列表", bookIdPrefix);
            List<String> words = this.baseMapper.getWordsByBookIdPrefix(bookIdPrefix);
            logger.info("获取书籍单词列表成功: 前缀为bookId={}, 返回单词数量: {}", bookIdPrefix, words != null ? words.size() : 0);
            return words;
        } catch (Exception e) {
            logger.error("根据书籍ID前缀获取单词列表失败, bookIdPrefix: {}", bookIdPrefix, e);
            return null;
        }
    }

    @Override
    public boolean isWordExists(String word) {
        // 使用 MyBatis-Plus 的 LambdaQueryWrapper 来查询单词是否存在
        return this.lambdaQuery().eq(Word::getWord, word).exists();
    }





    @Override
    public List<String> searchByPrefix(String prefix, int limit) {

        logger.info("尝试补全");
        // 确保前缀小写（根据需求调整）
        String searchPrefix = prefix.toLowerCase() + "%";

        // 方案1：使用自定义Mapper方法
        return baseMapper.searchByPrefix(searchPrefix, limit);


    }

    @Override
    public List<String> getAllWords() {
        try {
            logger.info("尝试获取所有单词");
            List<String> words = this.baseMapper.getAllWords();
            logger.info("获取所有单词成功, 返回单词数量: {}", words != null ? words.size() : 0);
            return words;
        } catch (Exception e) {
            logger.error("获取所有单词失败", e);
            return null;
        }
    }


}
