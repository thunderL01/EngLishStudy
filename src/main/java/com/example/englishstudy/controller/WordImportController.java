package com.example.englishstudy.controller;

import com.example.englishstudy.service.WordImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class WordImportController {

    private static final Logger logger = LoggerFactory.getLogger(WordImportController.class);
    // 用于提取单词的线程池大小
    private static final int EXTRACT_THREAD_POOL_SIZE = 1;
    // 用于导入单词的线程池大小，这里每个线程池的大小设为1，总共3个线程池
    private static final int IMPORT_THREAD_POOL_SIZE = 1;
    private static final int IMPORT_THREAD_POOL_COUNT = 3;

    @Autowired
    private WordImportService wordImportService;

    @PostMapping("/import-words-from-file")
    public String importWordsFromFile(@RequestParam("file") MultipartFile file) {
        // 文件格式验证
        if (!file.getOriginalFilename().endsWith(".txt")) {
            logger.error("用户尝试上传非 .txt 格式的文件: {}", file.getOriginalFilename());
            return "仅支持上传 .txt 格式的文件";
        }

        try {
            // 使用单独的线程池提取单词
            ExecutorService extractExecutor = Executors.newFixedThreadPool(EXTRACT_THREAD_POOL_SIZE);
            CompletableFuture<List<String>> extractFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return readWordsFromFile(file);
                } catch (IOException e) {
                    logger.error("读取文件时出现异常", e);
                    throw new RuntimeException(e);
                }
            }, extractExecutor);

            // 等待单词提取完成
            List<String> words = extractFuture.join();
            logger.info("从文件 {} 中读取到 {} 个单词", file.getOriginalFilename(), words.size());

            // 关闭提取线程池
            extractExecutor.shutdown();

            // 将单词列表分成多个部分，以便多个导入线程池处理
            int chunkSize = (int) Math.ceil((double) words.size() / IMPORT_THREAD_POOL_COUNT);
            List<List<String>> wordChunks = new ArrayList<>();
            for (int i = 0; i < IMPORT_THREAD_POOL_COUNT; i++) {
                int start = i * chunkSize;
                int end = Math.min((i + 1) * chunkSize, words.size());
                wordChunks.add(words.subList(start, end));
            }

            // 创建多个导入线程池并提交导入任务
            List<CompletableFuture<WordImportService.ImportResult>> importFutures = new ArrayList<>();
            ExecutorService[] importExecutors = new ExecutorService[IMPORT_THREAD_POOL_COUNT];
            for (int i = 0; i < IMPORT_THREAD_POOL_COUNT; i++) {
                importExecutors[i] = Executors.newFixedThreadPool(IMPORT_THREAD_POOL_SIZE);
                final int index = i;
                importFutures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        logger.info("开始异步导入第 {} 部分，共 {} 个单词", index + 1, wordChunks.get(index).size());
                        WordImportService.ImportResult result = wordImportService.importWords(wordChunks.get(index)).get();
                        logger.info("异步导入第 {} 部分完成，成功导入 {} 个单词，失败的单词有: {}", index + 1, result.getSuccessCount(), result.getFailedWords());
                        return result;
                    } catch (InterruptedException e) {
                        logger.error("导入单词时线程被中断", e);
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        logger.error("导入单词时出现执行异常", e);
                        throw new RuntimeException(e);
                    }
                }, importExecutors[i]));
            }

            // 等待所有导入任务完成
            CompletableFuture.allOf(importFutures.toArray(new CompletableFuture[0])).join();

            // 统计总的导入结果
            int totalSuccessCount = 0;
            List<String> allFailedWords = new ArrayList<>();
            for (CompletableFuture<WordImportService.ImportResult> importFuture : importFutures) {
                WordImportService.ImportResult result = importFuture.join();
                totalSuccessCount += result.getSuccessCount();
                allFailedWords.addAll(result.getFailedWords());
            }

            // 关闭所有导入线程池
            for (ExecutorService importExecutor : importExecutors) {
                importExecutor.shutdown();
            }

            StringBuilder response = new StringBuilder();
            response.append("已完成单词导入，共尝试导入 ").append(words.size()).append(" 个单词，成功导入 ").append(totalSuccessCount).append(" 个单词。");
            if (!allFailedWords.isEmpty()) {
                response.append(" 导入失败的单词有：").append(String.join(", ", allFailedWords));
            }
            return response.toString();
        } catch (Exception e) {
            logger.error("从文件导入单词时出现异常", e);
            return "从文件导入单词失败：" + e.getMessage();
        }
    }

    private List<String> readWordsFromFile(MultipartFile file) throws IOException {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineWords = line.split(",");
                for (String word : lineWords) {
                    String trimmedWord = word.trim();
                    if (!trimmedWord.isEmpty()) {
                        words.add(trimmedWord);
                    }
                }
            }
        }
        return words;
    }
}