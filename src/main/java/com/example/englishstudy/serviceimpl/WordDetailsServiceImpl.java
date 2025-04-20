package com.example.englishstudy.serviceimpl;


import com.example.englishstudy.entity.WordDetails;
import com.example.englishstudy.mapper.WordDetailsMapper;
import com.example.englishstudy.service.WordDetailsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordDetailsServiceImpl extends ServiceImpl<WordDetailsMapper, WordDetails> implements WordDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(WordDetailsServiceImpl.class);


    @Override
    public List<WordDetails> getWordDetailsByWordId(Integer wordId) {
        logger.info("尝试获取单词ID {} 的详情", wordId);
        try {
            List<WordDetails> details = this.baseMapper.getWordDetailsByWordId(wordId);
            logger.info("获取单词详情成功: wordId={}, 返回数据: {}", wordId, details);
            return details;
        } catch (Exception e) {
            logger.error("根据单词ID获取详情失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<WordDetails> getWordDetailsByWord(String word) {
        logger.info("尝试获取单词 {} 的详情", word);
        try {
            List<WordDetails> details = this.baseMapper.getWordDetailsByWord(word);
            logger.info("获取单词详情成功: word={}, 返回数据: {}", word, details);
            return details;
        } catch (Exception e) {
            logger.error("根据单词获取详情失败, word: {}", word, e);
            return null;
        }
    }

}