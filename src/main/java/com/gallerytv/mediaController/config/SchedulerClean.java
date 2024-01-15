package com.gallerytv.mediaController.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.*;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.nio.file.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

@Configuration
public class SchedulerClean {
    @Value("${media.location}")
    private String ubicacion;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start() {
        final Runnable cleaner = () -> {
            File file= new File(ubicacion);
            if(file.exists()&&file.isDirectory()){
                File[] listFile= file.listFiles();
                assert listFile != null;
                for (File elem : listFile){
                    if (!elem.isDirectory()){
                        Path path= Paths.get(elem.getAbsolutePath());
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        };
        long initialDelay = getTimeUntilMidnight();
        scheduler.scheduleAtFixedRate(cleaner, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        }
    private long getTimeUntilMidnight() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Duration.between(now, nextMidnight).getSeconds();
    }
    @PostConstruct
    public void init(){
        start();
    }
}
