package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    @Test
    public void testAddVectors() {
        // Arrange
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0, 6.0};
        SharedVector v1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);

        // Act
        v1.add(v2);

        // Assert - v1 should be modified, v2 should remain the same
        assertEquals(5.0, v1.get(0), 0.001);
        assertEquals(7.0, v1.get(1), 0.001);
        assertEquals(9.0, v1.get(2), 0.001);
        assertEquals(4.0, v2.get(0), 0.001); // v2 didn't change
    }

    @Test
    public void testDimensionMismatch() {
        // בדיקה שמערכת זורקת שגיאה כשמנסים לחבר וקטורים באורך שונה
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> {
            v1.add(v2);
        });
    }

    @Test
    public void testTranspose() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        
        v.transpose();
        
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
    }

    @Test
    public void testDot() {
        SharedVector v1 = new SharedVector(new double[] {1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[] {1, 2, 3}, VectorOrientation.COLUMN_MAJOR);
        double dot = v1.dot(v2);

        assertEquals(14, dot);

        v2 = new SharedVector(new double[] {1, 2, 3}, VectorOrientation.ROW_MAJOR);     
    }

    @Test
    public void testDotProductSuccess() {
        // Arrange
        // לפי הקוד שלך, dot מחייב אוריינטציה שונה (אחד שורה, אחד עמודה)
        SharedVector vRow = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        SharedVector vCol = new SharedVector(new double[]{4.0, 5.0, 6.0}, VectorOrientation.COLUMN_MAJOR);

        // Act
        double result = vRow.dot(vCol);

        // Assert
        // Calculation: 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32.0, result, 0.001);
    }

    @Test
    public void testDotProductOrientationMismatch() {
        // בדיקה שהפונקציה זורקת שגיאה אם מנסים לעשות dot בין שני וקטורים עם אותו כיוון
        // (בהתבסס על שורות 109-111 ב-SharedVector.java)
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3, 4}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> {
            v1.dot(v2);
        });
    }

    @Test
    public void testVecMatMulSuccess() {
        // Arrange
        // וקטור (1x3)
        SharedVector v = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        
        // מטריצה (3x2)
        double[][] matrixData = {
            {1.0, 4.0},
            {2.0, 5.0},
            {3.0, 6.0}
        };
        SharedMatrix matrix = new SharedMatrix(matrixData);

        // Act
        // הפונקציה משנה את הוקטור v עצמו (In-place update)
        v.vecMatMul(matrix);

        // Assert
        // התוצאה צריכה להיות וקטור באורך 2 (מספר העמודות במטריצה)
        assertEquals(2, v.length());
        
        // חישוב צפוי:
        // Col 0: 1*1 + 2*2 + 3*3 = 1 + 4 + 9 = 14
        // Col 1: 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(14.0, v.get(0), 0.001);
        assertEquals(32.0, v.get(1), 0.001);
    }

    @Test
    public void testVecMatMulDimensionMismatch() {
        // Arrange
        // וקטור באורך 2
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        
        // מטריצה עם 3 שורות (אי התאמה - אורך הוקטור חייב להיות שווה למספר השורות)
        double[][] matrixData = {
            {1.0, 4.0},
            {2.0, 5.0},
            {3.0, 6.0}
        };
        SharedMatrix matrix = new SharedMatrix(matrixData);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            v.vecMatMul(matrix);
        });
    }
}