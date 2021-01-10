package logger;

import java.util.*;

public interface LogClient {


    void start(String processId);

    void end(String processId);


    void poll();
}

class LoggerImpl implements LogClient {


    private final Map<String, Process> processes;
    private final TreeMap<Long, Process> queue;

    public LoggerImpl() {
        this.processes = new HashMap<>();
        this.queue= new TreeMap<>(Comparator.comparingLong(startTime -> startTime));

    }

    @Override
    public void start(String processId) {

        final long now = System.currentTimeMillis();
        final Process process = new Process(processId, now);

        processes.put(processId, process);
        queue.put(now, process);
    }

    @Override
    public void end(String processId) {

        processes.get(processId).setEndTime(System.currentTimeMillis());
       // queue.get(processId).setEndTime(System.currentTimeMillis());


    }

    @Override
    public void poll() {

        if (processes.isEmpty())
        {
            System.out.println("Queue is empty");

        }
        else {

            final Process procees = queue.firstEntry().getValue();

            if (procees.getEndTime() != -1) {
                System.out.println(procees.getId() + " started at " + procees.getStartTime() + "& ended at " + procees.getEndTime());
                processes.remove(procees.getId());
                queue.pollFirstEntry();

            } else {
                System.out.println("No completed tasks in queue " + queue.size());
            }

        }
    }



}

class Process {
    private final String id;
    private final long startTime;
    private long endTime;

    public Process(final String id, final long startTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime=-1;

    }

    public String getId() {
        return id;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}

 class LoggerMain{


    public static void main(String[] args) {

        final LogClient logger = new LoggerImpl();

        logger.start("1");
        logger.poll();
        logger.start("3");
        logger.poll();
        logger.end("1");
        logger.poll();
        logger.start("2");
        logger.poll();
        logger.end("2");
        logger.poll();
        logger.end("3");
        logger.poll();
        logger.poll();
        logger.poll();


     }


}
