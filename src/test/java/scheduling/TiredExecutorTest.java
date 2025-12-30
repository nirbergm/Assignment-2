package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TiredExecutorTest {

    private TiredExecutor executor;
    private final int POOL_SIZE = 3; // מספר עובדים לבדיקה

    @BeforeEach
    public void setUp() {
        // אתחול לפי הבנאי החדש שלך (מקבל רק מספר תהליכונים)
        executor = new TiredExecutor(POOL_SIZE);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        // סגירה נקייה אחרי כל טסט
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    public void testInitializationAndReport() {
        // מאחר ואין גטרים למערך העובדים, נשתמש ב-Report כדי לוודא שהם נוצרו
        String report = executor.getWorkerReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Worker Report"), "Report should contain header");
        
        // בדיקה שנוצרו שורות עבור כל העובדים (0, 1, 2)
        for (int i = 0; i < POOL_SIZE; i++) {
            assertTrue(report.contains("Worker " + i), "Report should contain Worker " + i);
        }
    }

    @Test
    public void testSubmitAll_BarrierLogic() {
        // זה הטסט הקריטי ל-wait/notify שלך ב-submitAll
        int taskCount = 20;
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(10); // עבודה קצרה
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                counter.incrementAndGet();
            });
        }

        // מוודאים ש-submitAll לא חוזר לפני שכולם סיימו
        // משתמשים ב-assertTimeout כדי לזהות Deadlock (אם ה-notify לא עובד)
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            executor.submitAll(tasks);
        }, "Deadlock detected! submitAll did not return in time.");

        assertEquals(taskCount, counter.get(), "All tasks should be finished after submitAll returns");
    }

    @Test
    public void testConcurrency() {
        // בדיקה שהמשימות רצות במקביל ולא אחת אחרי השניה
        // נריץ 3 משימות ארוכות (100ms) על 3 עובדים.
        // אם זה מקבילי -> ייקח בערך 100ms.
        // אם זה סדרתי -> ייקח 300ms.
        
        List<Long> startTimes = Collections.synchronizedList(new ArrayList<>());
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < POOL_SIZE; i++) {
            tasks.add(() -> {
                startTimes.add(System.currentTimeMillis());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            });
        }

        long startTotal = System.currentTimeMillis();
        executor.submitAll(tasks);
        long endTotal = System.currentTimeMillis();

        long totalDuration = endTotal - startTotal;

        // אנחנו נותנים מרווח נדיב (250ms), אבל זה עדיין חייב להיות פחות מ-300
        assertTrue(totalDuration < 280, 
            "Tasks ran structurally! Expected parallel execution (~100ms), took: " + totalDuration);
    }

    @Test
    public void testTaskExceptionHandling() {
        // בדיקה שהמערכת לא נתקעת אם משימה אחת זורקת שגיאה (Exception)
        // ה-wrapper שלך אמור לתפוס את ה-finally ולשחרר את המונה בכל מקרה
        
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        // משימה 1: תקינה
        tasks.add(counter::incrementAndGet);
        
        // משימה 2: זורקת שגיאה
        tasks.add(() -> {
            throw new RuntimeException("Boom!");
        });
        
        // משימה 3: תקינה
        tasks.add(counter::incrementAndGet);

        // הציפייה: submitAll יסתיים בהצלחה ולא ייתקע, למרות השגיאה באמצע
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            executor.submitAll(tasks);
        });

        // רק 2 משימות אמורות להצליח לעדכן את המונה
        assertEquals(2, counter.get());
    }

    @Test
    public void testLargeBatchReuse() {
        // בדיקה שהמנגנון עובד גם כשמספר המשימות גדול ממספר העובדים (Queue recycling)
        int tasksCount = 50; // הרבה יותר מ-3 עובדים
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < tasksCount; i++) {
            tasks.add(counter::incrementAndGet);
        }

        executor.submitAll(tasks);
        assertEquals(tasksCount, counter.get(), "All 50 tasks should finish");
    }
}