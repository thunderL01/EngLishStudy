package com.example.englishstudy.serviceimpl;

import com.example.englishstudy.entity.Word;
import com.example.englishstudy.mapper.WordMapper;
import com.example.englishstudy.service.WordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordServiceImpl extends ServiceImpl<WordMapper, Word> implements WordService {

    private static final Logger logger = LoggerFactory.getLogger(WordServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public Word getWordById(Integer wordId) {
        String key = "word:id:" + wordId;
        Word word = (Word) redisTemplate.opsForValue().get(key);
        if (word != null) {
            logger.info("从 Redis 缓存中获取单词信息: wordId={}", wordId);
            return word;
        }
        try {
            logger.info("尝试获取单词信息: wordId={}", wordId);
            word = this.baseMapper.getWordByWordId(wordId);
            if (word != null) {
                redisTemplate.opsForValue().set(key, word, 30, TimeUnit.MINUTES);
                logger.info("将单词信息存入 Redis 缓存: wordId={}", wordId);
            }
            logger.info("获取单词信息成功: wordId={}, 返回数据: {}", wordId, word);
            return word;
        } catch (Exception e) {
            logger.error("根据ID获取单词失败, wordId: {}", wordId, e);
            return null;
        }
    }

    //具体解析redis的使用
    @Override
    public Word getWordByWord(String wordStr) {
        // 1. 生成 Redis 缓存的键
        String key = "word:str:" + wordStr;

        // 2. 尝试从 Redis 缓存中获取单词信息
        Word word = (Word) redisTemplate.opsForValue().get(key);

        // 3. 检查缓存中是否存在该单词信息
        if (word != null) {
            // 如果存在，记录日志并返回该单词信息
            logger.info("从 Redis 缓存中获取单词信息: word={}", wordStr);
            return word;
        }

        // 4. 如果缓存中不存在该单词信息，尝试从数据库中查询
        try {
            // 记录尝试获取单词信息的日志
            logger.info("尝试获取单词信息: word={}", wordStr);
            // 调用 BaseMapper 的方法从数据库中查询单词信息
            word = this.baseMapper.getWordByWord(wordStr);

            // 5. 检查数据库中是否查询到该单词信息
            if (word != null) {
                // 如果查询到，将该单词信息存入 Redis 缓存，并设置过期时间为 30 分钟
                redisTemplate.opsForValue().set(key, word, 30, TimeUnit.MINUTES);
                // 记录将单词信息存入 Redis 缓存的日志
                logger.info("将单词信息存入 Redis 缓存: word={}", wordStr);
            }
            // 记录获取单词信息成功的日志
            logger.info("获取单词信息成功: word={}, 返回数据: {}", wordStr, word);
            // 返回查询到的单词信息
            return word;
        } catch (Exception e) {
            // 6. 如果在查询过程中出现异常，记录错误日志并返回 null
            logger.error("根据单词获取信息失败, word: {}", wordStr, e);
            return null;
        }
    }

    @Override
    public List<String> getAllDistinctBookIds() {
        String key = "bookIds:allDistinct";
        List<String> bookIds = (List<String>) redisTemplate.opsForValue().get(key);
        if (bookIds != null) {
            logger.info("从 Redis 缓存中获取所有不同的书籍ID");
            return bookIds;
        }
        try {
            logger.info("尝试获取所有不同的书籍ID");
            bookIds = this.baseMapper.getAllDistinctBookIds();
            if (bookIds != null) {
                redisTemplate.opsForValue().set(key, bookIds, 30, TimeUnit.MINUTES);
                logger.info("将所有不同的书籍ID存入 Redis 缓存");
            }
            logger.info("获取所有不同书籍ID成功, 返回数据: {}", bookIds);
            return bookIds;
        } catch (Exception e) {
            logger.error("获取所有不同书籍ID失败", e);
            return null;
        }
    }

    @Override
    public int getWordCountByBookId(String bookId) {
        String key = "wordCount:bookId:" + bookId;
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        if (count != null) {
            logger.info("从 Redis 缓存中获取书籍ID {} 的单词数量", bookId);
            return count;
        }
        try {
            logger.info("尝试获取书籍ID {} 的单词数量", bookId);
            count = this.baseMapper.getWordCountByBookId(bookId);
            if (count != null) {
                redisTemplate.opsForValue().set(key, count, 30, TimeUnit.MINUTES);
                logger.info("将书籍ID {} 的单词数量存入 Redis 缓存", bookId);
            }
            logger.info("获取书籍单词数量成功: bookId={}, 返回数量: {}", bookId, count);
            return count;
        } catch (Exception e) {
            logger.error("根据书籍ID获取单词数量失败, bookId: {}", bookId, e);
            return 0;
        }
    }

    @Override
    public int getWordCountByBookIdPrefix(String bookIdPrefix) {
        String key = "wordCount:bookIdPrefix:" + bookIdPrefix;
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        if (count != null) {
            logger.info("从 Redis 缓存中获取书籍ID前缀为 {} 的单词数量", bookIdPrefix);
            return count;
        }
        try {
            logger.info("尝试获取书籍ID前缀为 {} 的单词数量", bookIdPrefix);
            count = this.baseMapper.getWordCountByBookIdPrefix(bookIdPrefix);
            if (count != null) {
                redisTemplate.opsForValue().set(key, count, 30, TimeUnit.MINUTES);
                logger.info("将书籍ID前缀为 {} 的单词数量存入 Redis 缓存", bookIdPrefix);
            }
            logger.info("获取书籍单词数量成功: 前缀为bookId={}, 返回数量: {}", bookIdPrefix, count);
            return count;
        } catch (Exception e) {
            logger.error("根据书籍ID获取单词数量失败, bookId: {}", bookIdPrefix, e);
            return 0;
        }
    }

    @Override
    public List<String> getWordsByBookId(String bookId) {
        String key = "words:bookId:" + bookId;
        List<String> words = (List<String>) redisTemplate.opsForValue().get(key);
        if (words != null) {
            logger.info("从 Redis 缓存中获取书籍ID {} 的单词列表", bookId);
            return words;
        }
        try {
            logger.info("尝试获取书籍ID {} 的单词列表", bookId);
            words = this.baseMapper.getWordsByBookId(bookId);
            if (words != null) {
                redisTemplate.opsForValue().set(key, words, 30, TimeUnit.MINUTES);
                logger.info("将书籍ID {} 的单词列表存入 Redis 缓存", bookId);
            }
            logger.info("获取书籍单词列表成功: bookId={}, 返回单词数量: {}", bookId, words != null ? words.size() : 0);
            return words;
        } catch (Exception e) {
            logger.error("根据书籍ID获取单词列表失败, bookId: {}", bookId, e);
            return null;
        }
    }

    @Override
    public List<String> getWordsByBookIdPrefix(String bookIdPrefix) {
        String key = "words:bookIdPrefix:" + bookIdPrefix;
        List<String> words = (List<String>) redisTemplate.opsForValue().get(key);
        if (words != null) {
            logger.info("从 Redis 缓存中获取书籍ID前缀为 {} 的单词列表", bookIdPrefix);
            return words;
        }
        try {
            logger.info("尝试获取书籍ID前缀为 {} 的单词列表", bookIdPrefix);
            words = this.baseMapper.getWordsByBookIdPrefix(bookIdPrefix);
            if (words != null) {
                redisTemplate.opsForValue().set(key, words, 30, TimeUnit.MINUTES);
                logger.info("将书籍ID前缀为 {} 的单词列表存入 Redis 缓存", bookIdPrefix);
            }
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
        String key = "words:searchByPrefix:" + prefix + ":" + limit;
        List<String> words = (List<String>) redisTemplate.opsForValue().get(key);
        if (words != null) {
            logger.info("从 Redis 缓存中获取前缀为 {} 且限制为 {} 的单词列表", prefix, limit);
            return words;
        }
        try {
            logger.info("尝试补全");
            String searchPrefix = prefix.toLowerCase() + "%";
            words = this.baseMapper.searchByPrefix(searchPrefix, limit);
            if (words != null) {
                redisTemplate.opsForValue().set(key, words, 30, TimeUnit.MINUTES);
                logger.info("将前缀为 {} 且限制为 {} 的单词列表存入 Redis 缓存", prefix, limit);
            }
            return words;
        } catch (Exception e) {
            logger.error("根据前缀获取单词列表失败, prefix: {}, limit: {}", prefix, limit, e);
            return null;
        }
    }

    @Override
    public List<String> getAllWords() {
        String key = "words:all";
        List<String> words = (List<String>) redisTemplate.opsForValue().get(key);
        if (words != null) {
            logger.info("从 Redis 缓存中获取所有单词");
            return words;
        }
        try {
            logger.info("尝试获取所有单词");
            words = this.baseMapper.getAllWords();
            if (words != null) {
                redisTemplate.opsForValue().set(key, words, 30, TimeUnit.MINUTES);
                logger.info("将所有单词存入 Redis 缓存");
            }
            logger.info("获取所有单词成功, 返回单词数量: {}", words != null ? words.size() : 0);
            return words;
        } catch (Exception e) {
            logger.error("获取所有单词失败", e);
            return null;
        }
    }
}