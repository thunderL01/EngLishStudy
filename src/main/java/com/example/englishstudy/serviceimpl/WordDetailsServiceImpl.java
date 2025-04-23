package com.example.englishstudy.serviceimpl;

import com.example.englishstudy.entity.WordDetails;
import com.example.englishstudy.mapper.WordDetailsMapper;
import com.example.englishstudy.service.WordDetailsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordDetailsServiceImpl extends ServiceImpl<WordDetailsMapper, WordDetails> implements WordDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(WordDetailsServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    @Override
    public List<WordDetails> getWordDetailsByWordId(Integer wordId) {
        String key = "wordDetails:wordId:" + wordId;
        List<WordDetails> details = (List<WordDetails>) redisTemplate.opsForValue().get(key);
        if (details != null) {
            logger.info("从 Redis 缓存中获取单词ID {} 的详情", wordId);
            return details;
        }
        try {
            logger.info("尝试获取单词ID {} 的详情", wordId);
            details = this.baseMapper.getWordDetailsByWordId(wordId);
            if (details != null) {
                redisTemplate.opsForValue().set(key, details, 30, TimeUnit.MINUTES);
                logger.info("将单词ID {} 的详情存入 Redis 缓存", wordId);
            }
            logger.info("获取单词详情成功: wordId={}, 返回数据: {}", wordId, details);
            return details;
        } catch (Exception e) {
            logger.error("根据单词ID获取详情失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<WordDetails> getWordDetailsByWord(String word) {
        String key = "wordDetails:word:" + word;
        List<WordDetails> details = (List<WordDetails>) redisTemplate.opsForValue().get(key);
        if (details != null) {
            logger.info("从 Redis 缓存中获取单词 {} 的详情", word);
            return details;
        }
        try {
            logger.info("尝试获取单词 {} 的详情", word);
            details = this.baseMapper.getWordDetailsByWord(word);
            if (details != null) {
                redisTemplate.opsForValue().set(key, details, 30, TimeUnit.MINUTES);
                logger.info("将单词 {} 的详情存入 Redis 缓存", word);
            }
            logger.info("获取单词详情成功: word={}, 返回数据: {}", word, details);
            return details;
        } catch (Exception e) {
            logger.error("根据单词获取详情失败, word: {}", word, e);
            return null;
        }
    }
}