package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

    // עזר לבדיקת שוויון בין מטריצות (מערכים דו-ממדיים) עם סטייה מותרת (delta)
    private void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertEquals(expected.length, actual.length, "Number of rows mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], 0.001, "Row " + i + " mismatch");
        }
    }

    @Test
    public void testLoadRowMajor() {
        // Arrange
        double[][] data = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        };
        SharedMatrix matrix = new SharedMatrix();

        // Act
        matrix.loadRowMajor(data);

        // Assert
        // 1. בדיקה שהאורך הוא כמספר השורות
        assertEquals(2, matrix.length());
        
        // 2. בדיקה שהאוריינטציה היא שורות
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());

        // 3. בדיקה שהווקטור הראשון הוא באמת השורה הראשונה
        assertEquals(1.0, matrix.get(0).get(0), 0.001);
        assertEquals(2.0, matrix.get(0).get(1), 0.001);
        assertEquals(3.0, matrix.get(0).get(2), 0.001);

        // 4. בדיקה שקריאה מחדש מחזירה את המטריצה המקורית
        double[][] readBack = matrix.readRowMajor();
        assertMatrixEquals(data, readBack);
    }

    @Test
    public void testLoadColumnMajor() {
        // המטרה: לוודא שהטעינה באמת הופכת את המטריצה בזיכרון (Transpose)
        // אבל ש readRowMajor יודע להחזיר אותה למצב המקורי לוגית.
        
        // Arrange
        double[][] data = {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        }; // מטריצה 3x2
        SharedMatrix matrix = new SharedMatrix();

        // Act
        matrix.loadColumnMajor(data);

        // Assert
        // 1. בדיקה שהאורך הוא כמספר העמודות (כי הפכנו אותה)
        // במטריצה 3x2 יש 2 עמודות, לכן ה-SharedMatrix יחזיק 2 וקטורים
        assertEquals(2, matrix.length());

        // 2. בדיקה שהאוריינטציה היא עמודות
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());

        // 3. בדיקה שהווקטור הפנימי הראשון הוא העמודה הראשונה {1, 3, 5}
        SharedVector col0 = matrix.get(0);
        assertEquals(3, col0.length()); // אורך הוקטור צריך להיות כמספר השורות המקוריות
        assertEquals(1.0, col0.get(0), 0.001);
        assertEquals(3.0, col0.get(1), 0.001);
        assertEquals(5.0, col0.get(2), 0.001);

        // 4. הקסם: בדיקה ש readRowMajor מחזיר את המטריצה המקורית (3x2)
        // הפונקציה אמורה להבין שזה שמור כעמודות ולהפוך חזרה בקריאה
        double[][] readBack = matrix.readRowMajor();
        assertMatrixEquals(data, readBack);
    }

    @Test
    public void testGetAndOrientation() {
        double[][] data = {{1, 2}, {3, 4}};
        SharedMatrix m = new SharedMatrix(data); // בנאי דיפולטיבי טוען RowMajor

        assertNotNull(m.get(0));
        assertNotNull(m.get(1));
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        assertEquals(VectorOrientation.ROW_MAJOR, m.get(0).getOrientation());
    }
    
    @Test
    public void testEmptyMatrix() {
        // בדיקת מקרי קצה
        double[][] empty = {};
        SharedMatrix m = new SharedMatrix(empty);
        
        assertEquals(0, m.length());
        // readRowMajor אמור להחזיר מערך ריק ולא לקרוס
        double[][] result = m.readRowMajor();
        assertEquals(0, result.length);
    }
}