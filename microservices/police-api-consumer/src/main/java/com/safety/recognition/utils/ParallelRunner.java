package com.safety.recognition.utils;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class ParallelRunner {
    private final ForkJoinPool customThreadPool;
    private final Long duration;

    public ParallelRunner(int numberOfThreads, Duration duration){
        customThreadPool = new ForkJoinPool(numberOfThreads);
        this.duration = duration.toMillis();
    }

    public <T> void submit(Collection<T> items, Consumer<T> consumer) {
        long beforeScheduled = System.currentTimeMillis();
        customThreadPool.submit(() -> items.parallelStream().forEach(consumer)).join();
        long untilDurationLast = duration - (System.currentTimeMillis() - beforeScheduled);
        if(untilDurationLast > 0) {
            try {
                Thread.sleep(untilDurationLast);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
