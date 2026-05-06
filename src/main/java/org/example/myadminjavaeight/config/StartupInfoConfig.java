package org.example.myadminjavaeight.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupInfoConfig implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupInfoConfig.class);

    @Value("${server.port}")
    private int serverPort;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("项目已启动：");
        log.info("  - Swagger UI：http://localhost:{}/swagger-ui.html", serverPort);
        log.info("  - OpenAPI JSON：http://localhost:{}/v3/api-docs", serverPort);
    }
}
