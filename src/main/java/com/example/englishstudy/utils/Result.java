package com.example.englishstudy.utils;


import lombok.Data;

@Data
public class Result<T> {
    private int code;    // 状态码
    private String msg;  // 返回消息
    private T data;      // 返回数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(Code.SUCCESS.code);
        result.setMsg(Code.SUCCESS.msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Code code) {
        Result<T> result = new Result<>();
        result.setCode(code.code);
        result.setMsg(code.msg);
        return result;
    }

    public static <T> Result<T> error(Code code, String customMsg) {
        Result<T> result = new Result<>();
        result.setCode(code.code);
        result.setMsg(customMsg != null ? customMsg : code.msg);
        return result;
    }



    // 状态码枚举
    public enum Code {
        SUCCESS(200, "操作成功"),
        BAD_REQUEST(400, "请求参数错误"),
        UNAUTHORIZED(401, "未授权"),
        FORBIDDEN(403, "禁止访问"),
        NOT_FOUND(404, "资源不存在"),
        INTERNAL_SERVER_ERROR(500, "服务器内部错误");

        private final int code;
        private final String msg;

        Code(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
