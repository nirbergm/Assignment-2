package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);
// Test change for git+
    public TiredExecutor(int numThreads) {
        // TODO
        workers = new TiredThread[numThreads];
        Random rand = new Random();

        for (int i = 0; i < numThreads; i++) {
            // הגרלת פקטור עייפות בין 0.5 ל-1.5 כנדרש
            double fatigueFactor = 0.5 + rand.nextDouble(); 
            
            workers[i] = new TiredThread(i, fatigueFactor);
            workers[i].start(); // הפעלת ה-Thread
            
            // בהתחלה כולם פנויים, אז מכניסים אותם לתור
            idleMinHeap.add(workers[i]);
        }
    }

    public void submit(Runnable task) {
      // סימון שיש משימה באוויר
        inFlight.incrementAndGet();

        try {
            // שליפת העובד הכי פחות עייף (חוסם אם אין פנויים)
            TiredThread worker = idleMinHeap.take();

            // יצירת המעטפת (Wrapper) ישירות כאן
            Runnable wrappedTask = () -> {
                try {
                    task.run();
                } finally {
                    // 1. החזרת העובד לתור (מתמיין מחדש לפי העייפות החדשה)
                    idleMinHeap.offer(worker);

                    // 2. עדכון מונה משימות ובדיקת סיום
                    if (inFlight.decrementAndGet() == 0) {
                        synchronized (this) {
                            this.notifyAll();
                        }
                    }
                }
            };

            // שליחה לביצוע
            worker.newTask(wrappedTask);

        } catch (Exception e) {
            inFlight.decrementAndGet(); // תיקון המונה במקרה של כישלון
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        // 1. שליחת כל המשימות
        for (Runnable task : tasks) {
            submit(task);
        }

        // 2. המתנה עד שהכל יסתיים (Barrier)
        // אנחנו נועלים את 'this' כי זה האובייקט שעשינו עליו notifyAll למעלה
        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    this.wait();
                } catch (Exception e) {
                    break;
                }
            }
        }

    }

    public void shutdown() throws InterruptedException {
        for (TiredThread worker : workers) {
            worker.shutdown();
        }
        // המתנה שהם באמת ימותו
        for (TiredThread worker : workers) {
            worker.join();
        }
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        StringBuilder sb = new StringBuilder();
        sb.append("Worker Report:\n");
        for (TiredThread worker : workers) {
            sb.append("Worker ").append(worker.getWorkerId()).append(": ")
              .append("Fatigue=").append(String.format("%.2f", worker.getFatigue())).append(", ")
              .append("TimeUsed=").append(worker.getTimeUsed()).append(", ")
              .append("TimeIdle=").append(worker.getTimeIdle()).append("\n");
        }
        return sb.toString();
    }
}
