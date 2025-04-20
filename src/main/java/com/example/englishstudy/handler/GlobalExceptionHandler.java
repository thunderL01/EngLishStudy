package com.example.englishstudy.handler;

import com.example.englishstudy.utils.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // 处理参数校验异常
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e, HttpServletRequest request) {
        String errorMsg;
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            errorMsg = extractErrorMessages(ex.getBindingResult().getFieldErrors());
        } else {
            BindException ex = (BindException) e;
            errorMsg = extractErrorMessages(ex.getBindingResult().getFieldErrors());
        }
        logger.error("ValidationException occurred for request: {} {}, error message: {}", request.getMethod(), request.getRequestURI(), errorMsg, e);
        return Result.error(Result.Code.BAD_REQUEST, errorMsg);
    }

    // 处理其他未捕获异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        logger.error("Unexpected exception occurred for request: {} {}", request.getMethod(), request.getRequestURI(), e);
        return Result.error(Result.Code.INTERNAL_SERVER_ERROR, "系统繁忙，请稍后再试");
    }

    // 提取所有字段错误信息
    private String extractErrorMessages(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}