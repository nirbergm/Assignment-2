package spl.lae;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;

    @BeforeEach
    public void setUp() {
        // אתחול המנוע עם 3 תהליכונים (כפי שמוגדר בבנאי שלך)
        engine = new LinearAlgebraEngine(3);
    }

    // --- פונקציית עזר להשוואת מטריצות ---
    private void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertEquals(expected.length, actual.length, "Rows count mismatch");
        if (expected.length > 0) {
            assertEquals(expected[0].length, actual[0].length, "Columns count mismatch");
        }
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], 0.001, "Row " + i + " mismatch");
        }
    }

    @Test
    public void testAddOperation() {
        // בדיקת חיבור: A + B
        double[][] dataA = {{1, 2}, {3, 4}};
        double[][] dataB = {{10, 20}, {30, 40}};
        double[][] expected = {{11, 22}, {33, 44}};

        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        
        // יצירת עץ חישוב פשוט
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));

        // הרצת המנוע
        ComputationNode resultNode = engine.run(addNode);

        // וידוא שהתוצאה נכונה
        assertNotNull(resultNode.getMatrix());
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    public void testMultiplyOperation() {
        // בדיקת כפל: A * B
        // (2x3) * (3x2) -> (2x2)
        double[][] dataA = {
            {1, 2, 3},
            {4, 5, 6}
        };
        double[][] dataB = {
            {7, 8},
            {9, 1},
            {2, 3}
        };
        // חישוב צפוי:
        // [1*7+2*9+3*2, 1*8+2*1+3*3] = [31, 19]
        // [4*7+5*9+6*2, 4*8+5*1+6*3] = [85, 55]
        double[][] expected = {
            {31.0, 19.0},
            {85.0, 55.0}
        };

        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        ComputationNode mulNode = new ComputationNode(ComputationNodeType.MULTIPLY, List.of(nodeA, nodeB));

        ComputationNode resultNode = engine.run(mulNode);

        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    public void testNegateOperation() {
        // בדיקת שלילה (Unary)
        double[][] data = {{1, -5}, {0, 10}};
        double[][] expected = {{-1, 5}, {-0.0, -10}};

        ComputationNode node = new ComputationNode(data);
        ComputationNode negNode = new ComputationNode(ComputationNodeType.NEGATE, List.of(node));

        ComputationNode resultNode = engine.run(negNode);

        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    public void testTransposeOperation() {
        // בדיקת שחלוף
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6}
        };
        double[][] expected = {
            {1, 4},
            {2, 5},
            {3, 6}
        };

        ComputationNode node = new ComputationNode(data);
        ComputationNode transNode = new ComputationNode(ComputationNodeType.TRANSPOSE, List.of(node));

        ComputationNode resultNode = engine.run(transNode);

        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    public void testComplexTreeExecution() {
        // בדיקת עץ מורכב: (A + B) * C
        // A=[1,2], B=[3,4] -> Sum=[4,6]
        // C=[[2],[0.5]] (column vector)
        // Result = [4*2 + 6*0.5] = [8 + 3] = [11] (1x1 matrix)
        
        double[][] dataA = {{1, 2}};
        double[][] dataB = {{3, 4}};
        double[][] dataC = {{2}, {0.5}};
        double[][] expected = {{11.0}};

        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        ComputationNode nodeC = new ComputationNode(dataC);

        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));
        ComputationNode rootNode = new ComputationNode(ComputationNodeType.MULTIPLY, List.of(addNode, nodeC));

        // הפעלת ה-run שאמור לפתור קודם את ה-ADD ואז את ה-MULTIPLY
        ComputationNode resultNode = engine.run(rootNode);

        assertMatrixEquals(expected, resultNode.getMatrix());
    }
}