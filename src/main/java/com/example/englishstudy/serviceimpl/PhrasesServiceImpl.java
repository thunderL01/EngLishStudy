package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.Phrases;
import com.example.englishstudy.mapper.PhrasesMapper;
import com.example.englishstudy.service.PhrasesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PhrasesServiceImpl extends ServiceImpl<PhrasesMapper, Phrases> implements PhrasesService {

    private static final Logger logger = LoggerFactory.getLogger(PhrasesServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Phrases> getPhrasesByWordId(Integer wordId) {
        String key = "phrases:wordId:" + wordId;
        List<Phrases> phrases = (List<Phrases>) redisTemplate.opsForValue().get(key);
        if (phrases != null) {
            logger.info("从 Redis 缓存中获取单词ID {} 的短语", wordId);
            return phrases;
        }
        try {
            logger.info("尝试获取单词ID {} 的短语", wordId);
            phrases = this.baseMapper.getPhrasesByWordId(wordId);
            if (phrases != null) {
                redisTemplate.opsForValue().set(key, phrases, 30, TimeUnit.MINUTES);
                logger.info("将单词ID {} 的短语存入 Redis 缓存，过期时间为30分钟", wordId);
            }
            logger.info("获取短语成功: wordId={}, 返回数据: {}", wordId, phrases);
            return phrases;
        } catch (Exception e) {
            logger.error("根据单词ID获取短语失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<Phrases> getPhrasesByWord(String word) {
        String key = "phrases:word:" + word;
        List<Phrases> phrases = (List<Phrases>) redisTemplate.opsForValue().get(key);
        if (phrases != null) {
            logger.info("从 Redis 缓存中获取单词 {} 的短语", word);
            return phrases;
        }
        try {
            logger.info("尝试获取单词 {} 的短语", word);
            phrases = this.baseMapper.getPhrasesByWord(word);
            if (phrases != null) {
                redisTemplate.opsForValue().set(key, phrases, 30, TimeUnit.MINUTES);
                logger.info("将单词 {} 的短语存入 Redis 缓存，过期时间为30分钟", word);
            }
            logger.info("获取短语成功: word={}, 返回数据: {}", word, phrases);
            return phrases;
        } catch (Exception e) {
            logger.error("根据单词获取短语失败, word: {}", word, e);
            return null;
        }
    }
}