package logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoggerImpl implements LogClient {


    private final Map<String, Process> map;
    private final ConcurrentSkipListMap<Long, List<Process>> queue;
    private final BlockingDeque<CompletableFuture<String>> pendingPolls;
    private final Lock lock;
    private final ExecutorService[] executorService;


    public LoggerImpl(int threads) {
        this.map = new ConcurrentHashMap<>();
        this.queue = new ConcurrentSkipListMap<>();
        this.pendingPolls = new LinkedBlockingDeque<>();
        this.lock = new ReentrantLock();
        this.executorService = new ExecutorService[threads];
        for (int i = 0; i < threads; i++) {
            executorService[i] = Executors.newSingleThreadExecutor();
        }

    }

    @Override
    public void start(String processId, long timestamp) {

        executorService[processId.hashCode() % executorService.length].execute(() -> {

                    final Process process = new Process(processId, timestamp);

                    map.put(processId, process);
                    queue.putIfAbsent(timestamp, new CopyOnWriteArrayList<>());
                    queue.get(timestamp).add(process);

                }
        );
    }

    @Override
    public void end(String taskId) {

        executorService[taskId.hashCode() % executorService.length].execute(() -> {
            map.get(taskId).setEndTime(System.currentTimeMillis());
            lock.lock();
            try {
                String result;
                while (!pendingPolls.isEmpty() && (result = pollNow()) != null) {
                    pendingPolls.take().complete(result);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });

    }

    @Override
    public String poll() {
        final CompletableFuture<String> result = new CompletableFuture<>();
        lock.lock();
        try {
            try {
                String logStatement;
                if (!pendingPolls.isEmpty()) {
                    pendingPolls.offer(result);
                } else if ((logStatement = pollNow()) != null) {
                    return logStatement;
                } else {
                    pendingPolls.offer(result);
                }
            } finally {
                lock.unlock();
            }
            return result.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }


    }

    private String pollNow() {
        if (!queue.isEmpty()) {
            for (final Process earliest : queue.firstEntry().getValue()) {
                if (earliest.getEndTime() != -1) {
                    queue.firstEntry().getValue().remove(earliest);
                    if (queue.firstEntry().getValue().isEmpty()) {
                        queue.pollFirstEntry();
                    }
                    map.remove(earliest.getId());
                    final var logStatement = "task " + earliest.getId() + " started at: " + earliest.getStartTime() + " and ended at: " + earliest.getEndTime();
                    System.out.println(logStatement);
                    return logStatement;
                }
            }
        }
        return null;
    }


}
