package ru.yandex.account.configuration;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.NoArgsConstructor;
import org.hibernate.resource.jdbc.spi.StatementInspector;

@NoArgsConstructor
public class ZipkinStatementInspector implements StatementInspector {

    private static Tracer tracer;

    public static void setTracer(Tracer t) {
        tracer = t;
    }

    @Override
    public String inspect(String sql) {
        if (tracer == null) return sql;

        Span currentSpan = tracer.currentSpan();
        Span sqlSpan = tracer.nextSpan(currentSpan).name("hibernate.query").remoteServiceName("postgres").start();
        try (Tracer.SpanInScope scope = tracer.withSpan(sqlSpan)) {
            sqlSpan.tag("sql.query", sql);
            return sql;
        } finally {
            sqlSpan.end();
        }
    }
}
