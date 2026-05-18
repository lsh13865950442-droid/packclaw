package com.packclaw;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class PackClawApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext application = SpringApplication.run(PackClawApplication.class, args);
        Environment env = application.getEnvironment();
        log.info(
                "\n\tApplication '{}' started successfully! Access URLs:\n\t" +
                        "Swagger UI: http://{}:{}{}/swagger-ui/index.html\n\t" +
                        "Knife4j: http://{}:{}{}/doc.html\n\t",
                env.getProperty("spring.application.name"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port", "8080"),
                env.getProperty("server.servlet.context-path", ""),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port", "8080"),
                env.getProperty("server.servlet.context-path", ""));
    }
}
