package com.packclaw.common.domain;

import java.io.Serializable;
import com.packclaw.common.constant.HttpStatus;
import lombok.Data;

/**
 * API response wrapper
 */
@Data
public class Response<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = HttpStatus.SUCCESS;
    public static final int FAIL = HttpStatus.ERROR;

    private int code;
    private String msg;
    private T data;

    public static <T> Response<T> ok() {
        return restResult(null, SUCCESS, "success");
    }

    public static <T> Response<T> ok(T data) {
        return restResult(data, SUCCESS, "success");
    }

    public static <T> Response<T> ok(T data, String msg) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> Response<T> fail() {
        return restResult(null, FAIL, null);
    }

    public static <T> Response<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> Response<T> fail(T data) {
        return restResult(data, FAIL, null);
    }

    public static <T> Response<T> fail(T data, String msg) {
        return restResult(data, FAIL, msg);
    }

    public static <T> Response<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    public static <T> Response<T> restResult(T data, int code, String msg) {
        Response<T> apiResult = new Response<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static <T> Response<T> error() {
        return Response.error("Operation failed");
    }

    public static <T> Response<T> error(String msg) {
        return Response.error(msg, null);
    }

    public static <T> Response<T> error(String msg, T data) {
        return restResult(data, HttpStatus.ERROR, msg);
    }

    public static <T> Response<T> error(int code, String msg) {
        return restResult(null, code, msg);
    }

    public boolean isSuccess() {
        return this.code == SUCCESS;
    }
}
