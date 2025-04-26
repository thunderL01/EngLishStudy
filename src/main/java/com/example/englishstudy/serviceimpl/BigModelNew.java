package com.example.englishstudy.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class BigModelNew extends WebSocketListener {
    private static final Logger logger = LoggerFactory.getLogger(BigModelNew.class);

    @Value("${xinghuo.hostUrl}")
    private String hostUrl;

    @Value("${xinghuo.domain}")
    private String domain;

    @Value("${xinghuo.appid}")
    private String appid;

    @Value("${xinghuo.apiSecret}")
    private String apiSecret;

    @Value("${xinghuo.apiKey}")
    private String apiKey;

    private List<RoleContent> historyList = new ArrayList<>(); // 对话历史存储集合
    private String totalAnswer = ""; // 大模型的答案汇总
    private final Gson gson = new Gson();
    private CountDownLatch latch;

    private String sendMessageArgs;

    public String sendMessage(String message) throws Exception {
        this.sendMessageArgs = message;
        totalAnswer = "";
        latch = new CountDownLatch(1);

        // 构建鉴权 url
        String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
        OkHttpClient client = new OkHttpClient.Builder().build();
        String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(url).build();
        WebSocket webSocket = client.newWebSocket(request, this);

        // 等待服务端返回完毕
        latch.await();

        return totalAnswer;
    }

    public static boolean canAddHistory(List<RoleContent> historyList) {  // 由于历史记录最大上线 1.2W 左右，需要判断是否能加入历史
        int historyLength = 0;
        for (RoleContent temp : historyList) {
            historyLength = historyLength + temp.content.length();
        }
        if (historyLength > 12000) {
            if (historyList.size() >= 5) {
                for (int i = 0; i < 5; i++) {
                    historyList.remove(0);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    // 线程来发送音频与参数
    class MyThread extends Thread {
        private WebSocket webSocket;
        private String message;

        public MyThread(WebSocket webSocket, String message) {
            this.webSocket = webSocket;
            this.message = message;
        }

        public void run() {
            try {
                JSONObject requestJson = new JSONObject();

                JSONObject header = new JSONObject();  // header 参数
                header.put("app_id", appid);
                header.put("uid", UUID.randomUUID().toString().substring(0, 10));

                JSONObject parameter = new JSONObject(); // parameter 参数
                JSONObject chat = new JSONObject();
                chat.put("domain", domain);
                chat.put("temperature", 0.5);
                chat.put("max_tokens", 4096);
                parameter.put("chat", chat);

                JSONObject payload = new JSONObject(); // payload 参数
                JSONObject messageObj = new JSONObject();
                JSONArray text = new JSONArray();

                // 历史问题获取
                if (historyList.size() > 0) {
                    for (RoleContent tempRoleContent : historyList) {
                        text.add(JSON.toJSON(tempRoleContent));
                    }
                }

                // 最新问题
                RoleContent roleContent = new RoleContent();
                roleContent.role = "user";
                roleContent.content = message;
                text.add(JSON.toJSON(roleContent));
                historyList.add(roleContent);

                messageObj.put("text", text);
                payload.put("message", messageObj);

                requestJson.put("header", header);
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);

                webSocket.send(requestJson.toString());
            } catch (Exception e) {
                logger.error("发送消息时出现异常", e);
            }
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        // 这里应该传递 message 而不是 totalAnswer
        MyThread myThread = new MyThread(webSocket, sendMessageArgs);
        myThread.start();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
        if (myJsonParse.header.code != 0) {
            logger.error("发生错误，错误码为：{}，本次请求的 sid 为：{}", myJsonParse.header.code, myJsonParse.header.sid);
            webSocket.close(1000, "");
            latch.countDown();
        }
        List<Text> textList = myJsonParse.payload.choices.text;
        for (Text temp : textList) {
            totalAnswer = totalAnswer + temp.content;
        }
        if (myJsonParse.header.status == 2) {
            // 可以关闭连接，释放资源
            if (canAddHistory(historyList)) {
                RoleContent roleContent = new RoleContent();
                roleContent.setRole("assistant");
                roleContent.setContent(totalAnswer);
                historyList.add(roleContent);
            } else {
                if (historyList.size() > 0) {
                    historyList.remove(0);
                }
                RoleContent roleContent = new RoleContent();
                roleContent.setRole("assistant");
                roleContent.setContent(totalAnswer);
                historyList.add(roleContent);
            }
            webSocket.close(1000, "");
            latch.countDown();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                logger.error("onFailure code: {}", code);
                if (null != response.body()) {
                    logger.error("onFailure body: {}", response.body().string());
                }
                if (101 != code) {
                    logger.error("connection failed");
                }
            }
        } catch (IOException e) {
            logger.error("处理失败响应时出现异常", e);
        }
        // 关闭 webSocket
        if (webSocket != null) {
            webSocket.close(1000, "");
        }
        latch.countDown();
    }

    // 鉴权方法
    public String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {

        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";


        // SHA256 加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64 加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);


        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);


        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder()
                .addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date)
                .addQueryParameter("host", url.getHost())
                .build();


        return httpUrl.toString();
    }

    // 返回的 json 结果拆解
    class JsonParse {
        Header header;
        Payload payload;
    }

    class Header {
        int code;
        int status;
        String sid;
    }

    class Payload {
        Choices choices;
    }

    class Choices {
        List<Text> text;
    }

    class Text {
        String role;
        String content;
    }

    class RoleContent {
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}