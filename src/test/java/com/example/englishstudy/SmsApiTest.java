package com.example.englishstudy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsApiTest {
    private static final String API_URL = "https://api.2su.cc/api/114/";

    public static void main(String[] args) {
        String phone = "19118117065"; // 请替换为真实的手机号

        // 测试发送验证码
        testSendCode(phone);

        // 由于验证码需要手动输入，这里不进行自动验证测试
        // 你可以手动调用 testVerifyCode 方法并输入验证码进行测试
        // String code = "123456"; // 请替换为实际收到的验证码
        // testVerifyCode(phone, code);
    }

    public static void testSendCode(String phone) {
        try {
            String url = API_URL + "?act=getCode&phone=" + phone;
            String response = sendGetRequest(url);
            System.out.println("发送验证码响应: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testVerifyCode(String phone, String code) {
        try {
            String url = API_URL + "?act=isCode&phone=" + phone + "&code=" + code;
            String response = sendGetRequest(url);
            System.out.println("验证验证码响应: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendGetRequest(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}