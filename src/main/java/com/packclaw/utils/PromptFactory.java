package com.packclaw.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.extern.slf4j.Slf4j;
import com.packclaw.common.exception.ServiceException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Prompt utility with Mustache template support
 */
@Slf4j
public class PromptFactory {

    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();

    public static String loadPrompt(String path) {
        return loadPrompt(path, null);
    }

    public static String loadPrompt(String path, Map<String, Object> variables) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                log.error("Prompt template not found: {}", path);
                throw new ServiceException("Prompt template not found: " + path);
            }

            try (InputStreamReader reader = new InputStreamReader(
                    resource.getInputStream(), StandardCharsets.UTF_8)) {

                Mustache mustache = MUSTACHE_FACTORY.compile(reader, path);

                StringWriter writer = new StringWriter();
                Map<String, Object> context = (variables != null && !variables.isEmpty())
                    ? variables
                    : new java.util.HashMap<>();
                mustache.execute(writer, context).flush();

                String result = writer.toString();
                if (variables != null && !variables.isEmpty()) {
                    log.info("Loaded and rendered Mustache template: {}, {} variables replaced", path, variables.size());
                } else {
                    log.info("Loaded Mustache template: {} (no variable substitution)", path);
                }
                return result;
            }
        } catch (IOException e) {
            log.error("Failed to read or render Mustache template: {}", path, e);
            throw new ServiceException("Failed to read or render Mustache template: " + path + ", error: " + e.getMessage());
        }
    }
}
