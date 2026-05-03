package com.vibefolio;

import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;

/**
 * Vibefolio API — main entry point.
 * Virtual threads are enabled via Tomcat customizer for high-concurrency AI calls.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class VibeFolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeFolioApplication.class, args);
    }

    /**
     * Configure Tomcat to dispatch each request on a virtual thread.
     * Requires Java 21+. Allows blocking I/O (DB, Anthropic) without pinning platform threads.
     */
    @Bean
    TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return (ProtocolHandler protocolHandler) ->
                protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
