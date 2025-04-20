package com.example.englishstudy;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

public class VerifySmsCodeTest {
    public static void main(String[] args) {
        // API 接口地址
        String url = "https://api.2su.cc/api/114/";
        // 请求参数，这里使用示例手机号和验证码，你需要替换为真实的手机号和收到的验证码
        String phone = "18773043519";
        String code = "你收到的验证码";
        String params = "?act=isCode&phone=" + phone + "&code=" + code;

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url + params);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity());
                System.out.println("校验验证码响应结果：" + result);
            } else {
                System.out.println("请求失败，状态码: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            System.out.println("请求发生异常: " + e.getMessage());
        }
    }
}