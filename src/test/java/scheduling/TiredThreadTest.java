package scheduling;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class TiredThreadTest {

    @Test
    public void testInitialization() {
        TiredThread t = new TiredThread(1, 1.5);
        assertEquals(1, t.getWorkerId());
        assertEquals(0, t.getFatigue(), "Initial fatigue should be 0");
        assertFalse(t.isBusy(), "Should not be busy initially");
    }

    @Test
    public void testTaskExecution() throws InterruptedException {
        // בדיקה שהתהליכון באמת מריץ משימות
        TiredThread t = new TiredThread(1, 1.0);
        t.start();

        // משתמשים ב-Latch כדי לחכות שהמשימה תסתיים
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean taskRan = new AtomicBoolean(false);

        t.newTask(() -> {
            taskRan.set(true);
            latch.countDown();
        });

        // מחכים עד 1 שניות למשימה שתסתיים
        boolean finished = latch.await(1, TimeUnit.SECONDS);
        
        assertTrue(finished, "Task should have finished");
        assertTrue(taskRan.get(), "Task logic should have been executed");

        t.shutdown();
        t.join(1000);
    }

    @Test
    public void testFatigueIncrease() throws InterruptedException {
        // בדיקה שזמן עבודה מעלה את העייפות
        double fatigueFactor = 2.0;
        TiredThread t = new TiredThread(1, fatigueFactor);
        t.start();

        CountDownLatch latch = new CountDownLatch(1);

        t.newTask(() -> {
            try {
                // מדמים עבודה של לפחות 10 מילישניות
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        // נותנים ל-Thread רגע לעדכן את המונים אחרי ה-finally של המשימה שלו
        Thread.sleep(50); 

        long timeUsed = t.getTimeUsed();
        double expectedFatigue = timeUsed * fatigueFactor;

        assertTrue(timeUsed > 0, "Time used should be greater than 0");
        assertEquals(expectedFatigue, t.getFatigue(), 0.001, "Fatigue calculation incorrect");

        t.shutdown();
        t.join();
    }

    @Test
    public void testCompareTo() {
        // בדיקה שהשוואה בין Threads עובדת לפי עייפות
        TiredThread t1 = new TiredThread(1, 1.0);
        TiredThread t2 = new TiredThread(2, 1.0);

        // בהתחלה שניהם 0
        assertEquals(0, t1.compareTo(t2));

        // נדמה כאילו t1 עבד יותר (אנחנו לא יכולים לשנות את timeUsed ישירות מבחוץ בלי להריץ, 
        // אבל נסתמך על זה שאם נריץ משימה ב-t1 הוא יהיה עייף יותר)
        // לצורך הפשטות בטסט יחידה ללא הרצה כבדה, נניח שההשוואה מסתמכת על getFatigue().
        // מכיוון שאין setter ל-timeUsed, הדרך היחידה להשפיע היא להריץ.
        // אבל אפשר לבדוק מתמטית:
        // אם היינו יכולים, היינו בודקים: (fatigue1 - fatigue2).
        
        // כאן נבדוק רק לוגיקה בסיסית של שוויון בהתחלה, 
        // ואת העובדה שהם ברי השוואה.
        assertEquals(0, t1.getFatigue());
        assertEquals(0, t2.getFatigue());
    }

    @Test
    public void testShutdown() throws InterruptedException {
        TiredThread t = new TiredThread(1, 1.0);
        t.start();

        assertTrue(t.isAlive());

        t.shutdown(); // שולח POISON_PILL
        
        // מחכים שימות
        t.join(2000);
        
        assertFalse(t.isAlive(), "Thread should be dead after shutdown");
    }

    @Test
    public void testQueueFullException() {
        // בדיקה שאי אפשר להעמיס על העובד יותר ממשימה אחת בתור
        TiredThread t = new TiredThread(1, 1.0);
        // שים לב: אנחנו *לא* מפעילים אותו (start), כדי שהוא לא ישלוף את המשימה
        
        // משימה ראשונה - נכנסת לתור (גודל 1)
        t.newTask(() -> {}); 

        // משימה שניה - אמורה להיכשל כי התור מלא והוא לא רוקן אותו
        assertThrows(IllegalStateException.class, () -> {
            t.newTask(() -> {});
        }, "Should throw exception when queue is full");
    }
}