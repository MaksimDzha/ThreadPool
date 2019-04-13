package ru.sbt;

import static java.lang.String.format;

public class SingleThreadWork {

    public static void main(String[] args) {
        Compute compute = new Compute();

        long start = System.nanoTime();

        double value = 0;
        for (int i = 0; i < 400; i++) {
            value += compute.doWork(i);
        }

        System.out.println(format("Executed by %d s, value : %f",
                (System.nanoTime() - start) / (1000_000_000),
                value));
    }
}
