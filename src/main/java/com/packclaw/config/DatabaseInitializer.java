package com.packclaw.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * 数据库初始化配置
 * 应用启动时自动执行SQL脚本创建表
 */
@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        log.info("开始执行数据库初始化脚本...");
        
        try {
            // 检查数据库连接
            try (Connection connection = dataSource.getConnection()) {
                log.info("数据库连接成功: {}", connection.getMetaData().getURL());
            } catch (Exception e) {
                log.error("数据库连接失败: {}", e.getMessage(), e);
                throw new RuntimeException("数据库连接失败", e);
            }

            // 读取并执行建表SQL脚本
            executeSqlFile("sql/schema.sql");
            
            // 验证表是否创建成功
            verifyTableExists("session");
            verifyTableExists("session_chat");
            verifyTableExists("model_config");
            verifyTableExists("skill_config");
            
            log.info("数据库初始化脚本执行成功！");
        } catch (Exception e) {
            log.error("数据库初始化脚本执行失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void verifyTableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String checkSql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = '" + tableName + "')";
                try (var rs = statement.executeQuery(checkSql)) {
                    if (rs.next() && rs.getBoolean(1)) {
                        log.info("表 {} 已存在", tableName);
                    } else {
                        log.error("表 {} 不存在！SQL脚本可能未正确执行", tableName);
                        throw new RuntimeException("表 " + tableName + " 不存在");
                    }
                }
            }
        } catch (Exception e) {
            log.error("验证表 {} 是否存在时出错: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("验证表 " + tableName + " 失败", e);
        }
    }
    
    private void executeSqlFile(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            InputStream inputStream = resource.getInputStream();
            String sql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));
            
            log.info("SQL文件内容长度: {} 字符", sql.length());
            
            try (Connection connection = dataSource.getConnection()) {
                // 使用分号分割 SQL 语句，支持多种结尾格式（包括文件末尾无换行的情况）
                String[] statements = sql.split(";\\s*(?=\\n|$)");
                
                log.info("共分割出 {} 条SQL语句", statements.length);
                
                for (int i = 0; i < statements.length; i++) {
                    String stmt = statements[i].trim();
                    
                    log.info("处理第 {} 条语句，长度: {} 字符", i + 1, stmt.length());
                    
                    // 跳过空语句
                    if (stmt.isEmpty()) {
                        log.info("跳过空语句 [{}]", i + 1);
                        continue;
                    }
                    
                    // 如果整个语句都是注释（没有实际的SQL命令），则跳过
                    // 但如果语句中包含 CREATE/INSERT/UPDATE 等关键字，即使以注释开头也要执行
                    boolean hasSqlCommand = stmt.toUpperCase().contains("CREATE ") ||
                                           stmt.toUpperCase().contains("INSERT ") ||
                                           stmt.toUpperCase().contains("UPDATE ") ||
                                           stmt.toUpperCase().contains("DELETE ") ||
                                           stmt.toUpperCase().contains("SELECT ") ||
                                           stmt.toUpperCase().contains("ALTER ");
                    
                    if (!hasSqlCommand) {
                        log.info("跳过纯注释 [{}]: {}", i + 1, stmt.substring(0, Math.min(50, stmt.length())));
                        continue;
                    }
                    
                    try (Statement statement = connection.createStatement()) {
                        String preview = stmt.substring(0, Math.min(100, stmt.length()));
                        log.info(">>> 执行SQL [{}/{}]: {}", i + 1, statements.length, preview);
                        statement.execute(stmt);
                        log.info("<<< SQL [{}] 执行成功", i + 1);
                    }
                }
            }
            log.info("成功执行SQL文件: {}", filePath);
        } catch (Exception e) {
            log.error("执行SQL文件 {} 失败: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
}
