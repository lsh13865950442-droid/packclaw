package com.packclaw.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 列表数据适配工具类
 *
 * @author PackClaw
 */
@Slf4j
public class AdapterListDataUtils {

    /**
     * 将列表数据适配为Markdown表格格式
     *
     * @param headers 表头列表
     * @param rows 数据行列表，每行是一个字符串列表
     * @return Markdown格式的表格字符串
     */
    public static String toMarkdownTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 添加表头
        sb.append("| ");
        sb.append(String.join(" | ", headers));
        sb.append(" |\n");

        // 添加分隔行
        sb.append("| ");
        sb.append(headers.stream().map(h -> "---").collect(Collectors.joining(" | ")));
        sb.append(" |\n");

        // 添加数据行
        if (rows != null) {
            for (List<String> row : rows) {
                sb.append("| ");
                sb.append(String.join(" | ", row));
                sb.append(" |\n");
            }
        }

        return sb.toString();
    }

    /**
     * 将键值对列表适配为Markdown表格格式
     *
     * @param keyValueList 键值对列表，每个元素是一个包含key和value的列表
     * @return Markdown格式的表格字符串
     */
    public static String keyValueToMarkdownTable(List<List<String>> keyValueList) {
        if (keyValueList == null || keyValueList.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("| Key | Value |\n");
        sb.append("| --- | --- |\n");

        for (List<String> pair : keyValueList) {
            if (pair.size() >= 2) {
                sb.append("| ").append(pair.get(0)).append(" | ").append(pair.get(1)).append(" |\n");
            }
        }

        return sb.toString();
    }
}
