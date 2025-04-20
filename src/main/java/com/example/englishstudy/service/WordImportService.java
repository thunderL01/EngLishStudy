package com.example.englishstudy.service;

import com.example.englishstudy.entity.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordImportService {

    private static final Logger logger = LoggerFactory.getLogger(WordImportService.class);
    private static final int MAX_CONNECTIONS = 10; // 最大连接数
    private static final Gson gson = new Gson();

    @Autowired
    private WordService wordService;
    @Autowired
    private WordDetailsService wordDetailsService;
    @Autowired
    private WordRelWordsService wordRelWordsService;
    @Autowired
    private WordSynonymsService wordSynonymsService;
    @Autowired
    private PhrasesService phrasesService;
    @Autowired
    private SentencesService sentencesService;

    private final CloseableHttpClient httpClient;

    public WordImportService() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_CONNECTIONS);
        this.httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    @Transactional
    public boolean importWord(String word) {
        // 检查单词是否已经存在
        if (wordService.isWordExists(word)) {
            logger.info("单词 {} 已经存在，跳过导入", word);
            return false;
        }

        JsonObject data = getWordDataFromApi(word);
        if (data == null) {
            return false;
        }

        Word wordEntity = new Word();
        wordEntity.setWord(data.get("word").getAsString());
        wordEntity.setBookId(data.get("bookId").getAsString());
        wordEntity.setUkPhonetic(data.get("ukphone").getAsString());
        wordEntity.setUkPronunciationUrl(data.get("ukspeech").getAsString());
        wordEntity.setUsPhonetic(data.get("usphone").getAsString());
        wordEntity.setUsPronunciationUrl(data.get("usspeech").getAsString());

        // 先保存单词，获取自增的 wordId
        if (wordService.save(wordEntity)) {
            Integer wordId = wordEntity.getWordId();

            // 处理单词详情
            processWordDetails(data, wordId);

            // 处理同根词
            processWordRelWords(data, wordId);

            // 处理近义词
            processWordSynonyms(data, wordId);

            // 处理短语
            processPhrases(data, wordId);

            // 处理例句
            processSentences(data, wordId);

            return true;
        }
        return false;
    }

    private JsonObject getWordDataFromApi(String word) {
        try {
            URL url = new URL("https://v2.xxapi.cn/api/englishwords?word=" + word);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                JsonElement dataElement = jsonResponse.get("data");
                if (dataElement != null && dataElement.isJsonObject()) {
                    return dataElement.getAsJsonObject();
                } else {
                    logger.error("API 返回的数据中 'data' 字段不是一个有效的 JSON 对象，单词: {}", word);
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            logger.error("获取单词 {} 的 API 数据时出现异常：", word, e);
        }
        return null;
    }

    private void processWordDetails(JsonObject data, Integer wordId) {
        List<WordDetails> wordDetailsList = new ArrayList<>();
        JsonArray translations = data.getAsJsonArray("translations");
        for (int i = 0; i < translations.size(); i++) {
            JsonObject translation = translations.get(i).getAsJsonObject();
            WordDetails wordDetails = new WordDetails();
            wordDetails.setWordId(wordId);
            wordDetails.setPos(translation.get("pos").getAsString());
            wordDetails.setDefinition(translation.get("tran_cn").getAsString());
            wordDetailsList.add(wordDetails);
        }
        if (!wordDetailsList.isEmpty()) {
            wordDetailsService.saveBatch(wordDetailsList);
        }
    }

    private void processWordRelWords(JsonObject data, Integer wordId) {
        List<WordRelWords> wordRelWordsList = new ArrayList<>();
        JsonArray relWords = data.getAsJsonArray("relWords");
        for (int i = 0; i < relWords.size(); i++) {
            JsonObject relWordGroup = relWords.get(i).getAsJsonObject();
            JsonArray hwds = relWordGroup.getAsJsonArray("Hwds");
            String pos = relWordGroup.get("Pos").getAsString();
            for (int j = 0; j < hwds.size(); j++) {
                JsonObject hwd = hwds.get(j).getAsJsonObject();
                WordRelWords wordRelWords = new WordRelWords();
                wordRelWords.setWordId(wordId);
                wordRelWords.setPos(pos);
                wordRelWords.setRelWord(hwd.get("hwd").getAsString());
                wordRelWords.setRelWordTrans(hwd.get("tran").getAsString());
                wordRelWordsList.add(wordRelWords);
            }
        }
        if (!wordRelWordsList.isEmpty()) {
            wordRelWordsService.saveBatch(wordRelWordsList);
        }
    }

    private void processWordSynonyms(JsonObject data, Integer wordId) {
        List<WordSynonyms> wordSynonymsList = new ArrayList<>();
        JsonArray synonyms = data.getAsJsonArray("synonyms");
        for (int i = 0; i < synonyms.size(); i++) {
            JsonObject synonymGroup = synonyms.get(i).getAsJsonObject();
            JsonArray hwds = synonymGroup.getAsJsonArray("Hwds");
            String pos = synonymGroup.get("pos").getAsString();
            String tran = synonymGroup.get("tran").getAsString();
            for (int j = 0; j < hwds.size(); j++) {
                JsonObject hwd = hwds.get(j).getAsJsonObject();
                WordSynonyms wordSynonyms = new WordSynonyms();
                wordSynonyms.setWordId(wordId);
                wordSynonyms.setPos(pos);
                wordSynonyms.setSynonymWord(hwd.get("word").getAsString());
                wordSynonyms.setSynonymWordTrans(tran);
                wordSynonymsList.add(wordSynonyms);
            }
        }
        if (!wordSynonymsList.isEmpty()) {
            wordSynonymsService.saveBatch(wordSynonymsList);
        }
    }

    private void processPhrases(JsonObject data, Integer wordId) {
        List<Phrases> phrasesList = new ArrayList<>();
        JsonArray phrases = data.getAsJsonArray("phrases");
        for (int i = 0; i < phrases.size(); i++) {
            JsonObject phrase = phrases.get(i).getAsJsonObject();
            Phrases p = new Phrases();
            p.setWordId(wordId);
            p.setPhraseContent(phrase.get("p_content").getAsString());
            p.setPhraseTrans(phrase.get("p_cn").getAsString());
            phrasesList.add(p);
        }
        if (!phrasesList.isEmpty()) {
            phrasesService.saveBatch(phrasesList);
        }
    }

    private void processSentences(JsonObject data, Integer wordId) {
        List<Sentences> sentencesList = new ArrayList<>();
        JsonArray sentences = data.getAsJsonArray("sentences");
        for (int i = 0; i < sentences.size(); i++) {
            JsonObject sentence = sentences.get(i).getAsJsonObject();
            Sentences s = new Sentences();
            s.setWordId(wordId);
            s.setSentence(sentence.get("s_content").getAsString());
            s.setSentenceTrans(sentence.get("s_cn").getAsString());
            sentencesList.add(s);
        }
        if (!sentencesList.isEmpty()) {
            sentencesService.saveBatch(sentencesList);
        }
    }

    @Async
    @Transactional
    public Future<ImportResult> importWords(List<String> words) {
        int successCount = 0;
        List<String> failedWords = new ArrayList<>();
        for (String word : words) {
            if (importWord(word)) {
                successCount++;
            } else {
                failedWords.add(word);
            }
        }
        ImportResult result = new ImportResult(successCount, failedWords);
        return new AsyncResult<>(result);
    }

    @Getter
    public static class ImportResult {
        private final int successCount;
        private final List<String> failedWords;

        public ImportResult(int successCount, List<String> failedWords) {
            this.successCount = successCount;
            this.failedWords = failedWords;
        }
    }
}