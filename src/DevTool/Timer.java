package DevTool;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Timer {

    private ScheduledThreadPoolExecutor ses;
    protected String file, name;
    private static final AtomicInteger threadNumber = new AtomicInteger(1);


    public void start() {
        if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
            return;
        }
        ses = new ScheduledThreadPoolExecutor(20, new RejectedThreadFactory());
        ses.setKeepAliveTime(10, TimeUnit.MINUTES);
        ses.allowCoreThreadTimeOut(true);
        ses.setMaximumPoolSize(20);
        ses.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    }

    public ScheduledThreadPoolExecutor getSES() {
        return ses;
    }

    public void stop() {
        if (ses != null) {
            ses.shutdown();
        }
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
        if (ses == null) {
            return null;
        }
        return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r, file), delay, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime) {
        if (ses == null) {
            return null;
        }
        return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r, file), 0, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable r, long delay) {
        if (ses == null) {
            return null;
        }
        return ses.schedule(new LoggingSaveRunnable(r, file), delay, TimeUnit.MILLISECONDS);

    }

    public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
        return schedule(r, timestamp - System.currentTimeMillis());
    }

    private static class LoggingSaveRunnable implements Runnable {

        Runnable r;
        String file;

        public LoggingSaveRunnable(final Runnable r, final String file) {
            this.r = r;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                r.run();
            } catch (Throwable t) {
                System.out.println("타이머 에러 : "+t.getStackTrace().toString()+"  "+t.getSuppressed().toString()+"  "+t.getLocalizedMessage());
            }
        }
    }

    private class RejectedThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber2 = new AtomicInteger(1);
        private final String tname;

        public RejectedThreadFactory() {
            Random r = new Random();
            tname = name + r.nextInt();
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r);
            t.setName(tname + "-W-" + threadNumber.getAndIncrement() + "-" + threadNumber2.getAndIncrement());
            return t;
        }
    }

    public static class RoomTimer extends Timer{

        private static RoomTimer instance = new RoomTimer();

        private RoomTimer() {
            name = "RoomTimer";
        }

        public static RoomTimer getInstance() {
            return instance;
        }
    }

    public static class BoomSpinTimer extends Timer{

        private static BoomSpinTimer instance = new BoomSpinTimer();

        private BoomSpinTimer() {
            name = "BoomSpinTimer";
        }

        public static BoomSpinTimer getInstance() {
            return instance;
        }
    }

}
