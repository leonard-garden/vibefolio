package com.vibefolio.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Anthropic SDK client as a Spring bean.
 * API key is injected from {@link VibefolioProperties} — never hardcoded.
 */
@Configuration
class AnthropicConfig {

    @Bean
    AnthropicClient anthropicClient(VibefolioProperties properties) {
        return AnthropicOkHttpClient.builder()
                .apiKey(properties.ai().apiKey())
                .build();
    }
}
