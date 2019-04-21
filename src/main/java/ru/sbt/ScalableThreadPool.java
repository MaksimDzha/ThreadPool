package ru.sbt;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * Пул потоков с возможностью увеличения количества потоков во время исполнения
 * в зависимости от количества заданий в очереди workQueue. Используется либо
 * минимальное количество потоков, либо максимальное. При отсутствии заданий в
 * очереди количество потоков возвращается к минимальному при условии, что было
 * увеличение при выполнении предыдущих заданий.
 */
public class ScalableThreadPool implements Executor {
    private int minThreads = 1, maxThreads = 4; //Значения по-умолчанию
    private boolean boost = false;
    private TaskWorker[] workers = new TaskWorker[maxThreads]; //Массив внутренних "рабочих объектов", запускающих задания из очереди workQueue
    private Thread[] threads = new Thread[maxThreads]; //Массив используемых потоков
    private final Queue<Runnable> workQueue = new ConcurrentLinkedQueue<>(); //Очередь для заданий
    private boolean isRunning = true, isStarting = false;

    public ScalableThreadPool(){
        System.out.println("Используются значения по-умолчанию.");
    }

    /**
     *
     * @param minThreads Минимальное количество потоков
     * @param maxThreads Максимально допустимое количество потоков
     */
    public ScalableThreadPool(int minThreads, int maxThreads) {
        if ((minThreads > maxThreads) || (minThreads <=0)){
            System.out.println("Неправильно заданы параметры. Используются значения по-умолчанию.");
        } else {
            this.minThreads = minThreads;
            this.maxThreads = maxThreads;
            workers = new TaskWorker[maxThreads];
            threads = new Thread[maxThreads];
        }
    }

    /**
     * Метод для возможности "ручного" запуска пула
     */
    public void start() {
        if (!isStarting) {
            for (int i = 0; i < minThreads; i++) {
                threads[i] = new Thread(workers[i] = new TaskWorker());
                threads[i].start();
                System.out.println(threads[i].getName() + " is started");
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
            if (workQueue.size() > minThreads) {
                boostOn();
            }
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
     * Проверка запуска дополнительных потоков
     * @return True - дополнительные потоки активны; false - работает минимальное количество потоков.
     */
    public boolean isBoost(){
        return boost;
    }

    /**
     * Деактивация пула потоков
     */
    public void shutdown() {
        boostOff();
        for (int i = 0; i < minThreads; i++) {
            workers[i].stop();
            threads[i].interrupt();
            System.out.println(threads[i].getName() + " is interrupted? " + threads[i].isInterrupted());
        }
        isRunning = false;
        System.out.println("ThreadPool stopped");
    }

    /**
     * Пуск дополнительных потоков
     */
    private void boostOn() {
        if (!boost) {
            for (int i = minThreads; i < maxThreads; i++) {
                threads[i] = new Thread(workers[i] = new TaskWorker());
                threads[i].start();
                System.out.println(threads[i].getName() + " is started (boost!)");
            }
            boost = true;
        }
    }

    /**
     * Деактивация дополнительных потоков
     */
    private void boostOff() {
        if (boost) {
            for (int i = minThreads; i < maxThreads; i++) {
                workers[i].stop();
                threads[i].interrupt();
                System.out.println(threads[i].getName() + " is interrupted? " + threads[i].isInterrupted());
            }
            boost = false;
        }
    }

    /**
     * Загрузчик заданий из очереди. Для каждого потока используются разный экземпляр,
     * поэтому при выключении режима "boost" необходимо останавливать дополнительно созданных
     * "работников" с помощью метода stop().
     */
    private final class TaskWorker implements Runnable {

        private boolean working = true;

        @Override
        public void run() {
            while (working) {
                Runnable nextTask = workQueue.poll();
                if (nextTask != null) {
//                    System.out.println(Thread.currentThread().getName() + " get " + nextTask.toString());
                    nextTask.run();
                } else {
                    boostOff();
                }
            }
        }

        /**
         * Остановка текущего "работника"
         */
        private void stop() {
            working = false;
        }

    }

}
