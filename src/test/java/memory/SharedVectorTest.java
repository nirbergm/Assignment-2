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
}