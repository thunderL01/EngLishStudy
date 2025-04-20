package com.example.englishstudy;

import com.example.englishstudy.entity.Word;
import com.example.englishstudy.service.WordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WordServiceTest {

    @Autowired
    private WordService wordService;

    @Test
    public void testGetWordById() {

        // 测试正常情况，存在单词 ID
        Word word = wordService.getWordById(1);
        Assertions.assertNotNull(word);

        // 测试不存在的单词 ID
        Word nonExistentWord = wordService.getWordById(-1);
        Assertions.assertNull(nonExistentWord);
    }
}