package logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public interface LogClient {


    void start(String processId, long timestamp);

    void end(String processId);


    String poll();
}


class Process {
    private final String id;
    private final long startTime;
    private long endTime;

    public Process(final String id, final long startTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = -1;

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

class LoggerMain {


    public static void main(String[] args) {

        final LogClient logger = new LoggerImpl(10);

//        logger.start("1");
//        logger.poll();
//        logger.start("3");
//        logger.poll();
//        logger.end("1");
//        logger.poll();
//        logger.start("2");
//        logger.poll();
//        logger.end("2");
//        logger.poll();
//        logger.end("3");
//        logger.poll();
//        logger.poll();
//        logger.poll();


    }


}
