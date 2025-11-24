package ru.yandex.account.configuration;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    public DataSourceConfig(Tracer tracer) {
        ZipkinStatementInspector.setTracer(tracer);
    }
}