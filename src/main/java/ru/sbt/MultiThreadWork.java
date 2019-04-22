package ru.sbt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.String.format;

public class MultiThreadWork {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ScalableThreadPool threadPool = new ScalableThreadPool(2,4);
//        FixedThreadPool threadPool = new FixedThreadPool(4);
        threadPool.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Начинаем вычисления");
        Compute compute = new Compute();

        long start = System.nanoTime();

        List<Future<Double>> futures = new ArrayList<>();
        System.out.println("ThreadPool is boosted? " + threadPool.isBoost());
//        System.out.println("ThreadPool is running? " + threadPool.isRunning());

        for (int i = 0; i < 200; i++) {
            final int j = i;
            futures.add(
                    CompletableFuture.supplyAsync(
                            () -> compute.doWork(j),
                            threadPool
                    ));
        }
        System.out.println("ThreadPool is boosted? " + threadPool.isBoost());
//        System.out.println("ThreadPool is running? " + threadPool.isRunning());


        double value = 0;
        for (Future<Double> future : futures) {
            value += future.get();
        }

        System.out.println(format("Executed by %d s, value : %f",
                (System.nanoTime() - start) / (1000_000_000),
                value));

        threadPool.shutdown();
    }
}
