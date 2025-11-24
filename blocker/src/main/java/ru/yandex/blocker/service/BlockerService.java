package ru.yandex.blocker.service;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BlockerService {

    private static final Logger log = LoggerFactory.getLogger(BlockerService.class);

    private final Tracer tracer;


    public boolean isSuspicious(){
        if (ThreadLocalRandom.current().nextDouble(0, 1) < 0.1){
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.info("Suspicious operation");
            ThreadContext.clearAll();
            return true;
        } else {
            return false;
        }
    }

}
