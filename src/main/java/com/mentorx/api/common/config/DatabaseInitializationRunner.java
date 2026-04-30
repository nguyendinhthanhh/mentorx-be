package com.mentorx.api.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseInitializationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Value("${app.database.initialize-on-startup:true}")
    private boolean initializeOnStartup;

    @Value("${app.database.run-sample-data:true}")
    private boolean runSampleData;

    @Value("${app.database.schema-script:classpath:db/data/init-schema.sql}")
    private Resource schemaScript;

    @Value("${app.database.sample-data-script:classpath:db/data/sample-data.sql}")
    private Resource sampleDataScript;

    @Bean
    @Order(1)
    public CommandLineRunner initializeDatabaseOnStartup() {
        return args -> {
            if (!initializeOnStartup) {
                log.info("Database initialization is disabled by configuration");
                return;
            }

            if (isSchemaAlreadyCreated()) {
                log.info("Database schema already exists, skipping schema initialization");
                return;
            }

            log.info("Database schema not found. Starting initialization scripts...");
            List<Resource> scripts = new ArrayList<>();
            scripts.add(schemaScript);

            if (runSampleData) {
                scripts.add(sampleDataScript);
            }

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(false);
            populator.setIgnoreFailedDrops(true);

            for (Resource script : scripts) {
                if (script == null || !script.exists()) {
                    log.warn("Initialization script not found, skipped");
                    continue;
                }

                String scriptName = StringUtils.hasText(script.getFilename()) ? script.getFilename() : script.getDescription();
                log.info("Executing initialization script: {}", scriptName);
                populator.addScript(script);
            }

            populator.execute(Objects.requireNonNull(dataSource));
            log.info("Database initialization finished successfully");
        };
    }

    private boolean isSchemaAlreadyCreated() {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'users'
                """, Integer.class);
        return tableCount != null && tableCount > 0;
    }
}
