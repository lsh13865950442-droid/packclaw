package com.packclaw.controller;

import com.packclaw.common.domain.Response;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.message.Msg;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络连通性测试接口
 *
 * @author PackClaw
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Tag(name = "网络测试", description = "用于测试网络连通性")
public class AgentTestController {

    @Resource
    private ChatModelBase glmChatModel;

    /**
     * 测试 DashScope API 网络连通性（Telnet方式）
     *
     * @return 连通性测试结果
     */
    @GetMapping("/network/dashscope")
    @Operation(summary = "测试DashScope连通性", description = "使用telnet方式测试 dashscope.aliyuncs.com:443 是否可达")
    public Response<Map<String, Object>> testDashScopeConnectivity() {
        String host = "dashscope.aliyuncs.com";
        int port = 443;
        log.info("开始测试网络连通性(telnet方式): {}:{}", host, port);

        Map<String, Object> result = testTelnetConnectivity(host, port);
        return Response.ok(result);
    }

    /**
     * 使用Socket测试端口连通性（类似telnet）
     *
     * @param host 主机地址
     * @param port 端口号
     * @return 测试结果
     */
    private Map<String, Object> testTelnetConnectivity(String host, int port) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 10000);
            long endTime = System.currentTimeMillis();

            result.put("host", host);
            result.put("port", port);
            result.put("reachable", true);
            result.put("message", "端口连接成功");
            result.put("responseTimeMs", endTime - startTime);

            log.info("Telnet测试成功: {}:{}, 耗时: {}ms", host, port, endTime - startTime);

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            result.put("host", host);
            result.put("port", port);
            result.put("reachable", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("responseTimeMs", endTime - startTime);

            log.error("Telnet测试失败: {}:{}, error={}", host, port, e.getMessage(), e);
        }

        return result;
    }

    /**
     * 测试百度网络连通性
     *
     * @return 连通性测试结果
     */
    @GetMapping("/network/baidu")
    @Operation(summary = "测试百度连通性", description = "测试 https://www.baidu.com 是否可达")
    public Response<Map<String, Object>> testBaiduConnectivity() {
        String targetUrl = "https://www.baidu.com";
        log.info("开始测试网络连通性: {}", targetUrl);

        Map<String, Object> result = testUrlConnectivity(targetUrl);
        return Response.ok(result);
    }

    /**
     * 测试模型调用（问你好，返回回复）
     *
     * @return 模型回复结果
     */
    @GetMapping("/network/model")
    @Operation(summary = "测试模型调用", description = "向模型发送'你好'，测试模型网络连通性")
    public Response<Map<String, Object>> testModelConnectivity() {
        log.info("开始测试模型调用连通性");

        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            ReActAgent agent = ReActAgent.builder()
                    .name("TestAgent")
                    .model(glmChatModel)
                    .build();

            Msg msg = Msg.builder()
                    .textContent("你好")
                    .build();

            Msg response = agent.call(msg).block();
            long endTime = System.currentTimeMillis();

            result.put("success", true);
            result.put("message", "模型调用成功");
            result.put("response", response.getTextContent());
            result.put("responseTimeMs", endTime - startTime);

            log.info("模型调用成功, 耗时: {}ms, 回复: {}", endTime - startTime, response.getTextContent());

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            result.put("success", false);
            result.put("message", "模型调用失败");
            result.put("error", e.getMessage());
            result.put("responseTimeMs", endTime - startTime);

            log.error("模型调用失败, 耗时: {}ms", endTime - startTime, e);
        }

        return Response.ok(result);
    }

    /**
     * 测试URL连通性
     *
     * @param targetUrl 目标URL
     * @return 测试结果
     */
    private Map<String, Object> testUrlConnectivity(String targetUrl) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            long endTime = System.currentTimeMillis();

            result.put("url", targetUrl);
            result.put("reachable", responseCode >= 200 && responseCode < 500);
            result.put("responseCode", responseCode);
            result.put("responseMessage", connection.getResponseMessage());
            result.put("responseTimeMs", endTime - startTime);

            log.info("网络测试完成: url={}, responseCode={}, responseTimeMs={}",
                    targetUrl, responseCode, endTime - startTime);

            connection.disconnect();

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            result.put("url", targetUrl);
            result.put("reachable", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("responseTimeMs", endTime - startTime);

            log.error("网络测试失败: url={}, error={}", targetUrl, e.getMessage(), e);
        }

        return result;
    }
}
