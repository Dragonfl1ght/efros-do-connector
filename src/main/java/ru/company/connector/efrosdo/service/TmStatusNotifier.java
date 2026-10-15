package ru.company.connector.efrosdo.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.company.connector.efrosdo.client.TmClient;
import ru.company.connector.efrosdo.config.AppProperties;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TmStatusNotifier {

    private final TmClient tmClient;
    private final Duration statusDelay;

    public TmStatusNotifier(TmClient tmClient, AppProperties props) {
        this.tmClient = tmClient;
        this.statusDelay = props.tm().statusDelay();
    }

    public void reportFreeAfterResponse() {
        CompletableFuture.runAsync(tmClient::reportFree,
                CompletableFuture.delayedExecutor(statusDelay.toMillis(), TimeUnit.MILLISECONDS));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reportFreeOnStartup() {
        tmClient.reportFree();
    }
}
