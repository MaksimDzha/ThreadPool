package ru.sbt;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * Пул потоков с фиксированным количеством потоков.
 */
public class FixedThreadPool implements Executor {
    private int threadsCount = 1; //Значение по-умолчанию
    private final Queue<Runnable> workQueue = new ConcurrentLinkedQueue<>(); //Очередь для заданий
    private volatile boolean isRunning = true;
    private boolean isStarting = false;

    /**
     * Конструктор для использования значений по-умолчанию
     */
    public FixedThreadPool() {
        System.out.println("Используются значения по-умолчанию.");
    }

    /**
     * Задание количества потоков.
     * @param threadsCount Количество потоков. Допустимо threadCount > 1
     */
    public FixedThreadPool(int threadsCount) {
        if (threadsCount > 1)
            this.threadsCount = threadsCount;
        else System.out.println("Неправильно указано количсетво потоков. Будут использованы значени по-умолчанию.");
    }

    /**
     * Метод для возможности "ручного" запуска пула
     */
    public void start() {
        if (!isStarting) {
            for (int i = 0; i < threadsCount; i++) {
                Thread t = new Thread(new TaskWorker());
                t.start();
                System.out.println(t.getName() + " is started");
            }
            isStarting = true;
        }
    }

    @Override
    public void execute(Runnable command) {
        if (!isStarting)
            start(); //Автозапуск, если забыли "стартануть"
        if (isRunning) {
            workQueue.offer(command);
        }
    }

    /**
     * Проверка состояния пула
     * @return True - потоки активны; false - потоки не запущены.
     */
    public boolean isStarting(){
        return isStarting;
    }

    /**
     * Деактивация пула потоков
     */
    public void shutdown() {
        System.out.println("ThreadPool stopped");
        isRunning = false;
    }


    /**
     * Загрузчик заданий из очереди.
     */
    private final class TaskWorker implements Runnable {

        @Override
        public void run() {
            while (isRunning) {
                Runnable nextTask = workQueue.poll();
                if (nextTask != null) {
                    nextTask.run();
                }
            }
        }
    }
}
