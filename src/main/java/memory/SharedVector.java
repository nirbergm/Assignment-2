package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        readLock();
        try {
            return vector[index];
        } 
        finally {
            readUnlock();
        }
    }

    public int length() {
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        return orientation;
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        if (orientation == VectorOrientation.ROW_MAJOR) {
            orientation = VectorOrientation.COLUMN_MAJOR;
        } 
        else {
            orientation = VectorOrientation.ROW_MAJOR;
        }
    }

    public void add(SharedVector other) {
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("Dimensions mismatch"); 
        }
        if (this.getOrientation() != other.getOrientation()) { 
             throw new IllegalArgumentException("Vectors must be of the same orientation to compute.");
        }
        writeLock();
        other.readLock(); 
        try {
            for (int i = 0; i < vector.length; i++) {
                vector[i] += other.get(i);
            }
        } 
        finally {
            other.readUnlock();
            writeUnlock();
        }
    }

    public void negate() {
        writeLock();
        try {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = -vector[i];
            }
        } 
        finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("Vectors must be of the same length to compute dot product.");
        }
        if (this.getOrientation() == other.getOrientation()) {
             throw new IllegalArgumentException("Vectors must be of the same length to compute dot product.");
        }
        readLock();
        other.readLock();
        double result = 0.0;
        try {
            for (int i = 0; i < vector.length; i++) {
                result += vector[i] * other.get(i);
            }
            return result;
        }
        finally {
            readLock();
            other.readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
       // שלב 1: בדיקת מימדים בסיסית ללא נעילות כבדות
        int matRows = matrix.length(); // SharedMatrix חושף את האורך ללא נעילה (מערך ג'אווה)
        
        // הנחה: המטריצה לא ריקה (אחרת צריך לטפל בנפרד)
        int matCols = (matRows > 0) ? matrix.get(0).length() : 0; 

        if (this.length() != matRows) {
             throw new IllegalArgumentException("Dimension mismatch: Vector length (" + 
                                                this.length() + ") != Matrix rows (" + matRows + ")");
        }

        // המערך החדש שיצבור את התוצאה (מאותחל ל-0.0)
        double[] newVector = new double[matCols];

        // שלב 2: חישוב תחת נעילת קריאה של הוקטור *שלנו* בלבד
        readLock();
        try {
            if (this.vector.length != matRows) {
                 throw new IllegalArgumentException("Dimension mismatch detected under lock");
            }

            // אופטימיזציה: במקום לרוץ על עמודות (שגורם לקפיצות בזיכרון), 
            // נרוץ על שורות המטריצה ונוסיף את התרומה של כל שורה לתוצאה.
            // זה נקרא Linear Combination of Rows.
            
            for (int r = 0; r < matRows; r++) {
                double scalar = this.vector[r]; // הערך מהוקטור שלנו (למשל x)
                
                // אם הסקלר הוא 0, אין טעם לחשב את כל השורה (שיפור ביצועים)
                if (scalar == 0.0) continue;

                // גישה לוקטור השורה הספציפי מתוך המטריצה המשותפת
                SharedVector matrixRow = matrix.get(r); 

                for (int c = 0; c < matCols; c++) {
                    // כאן אנחנו משתמשים ב-matrixRow.get(c).
                    // יתרון: זה נועל רק את השורה הספציפית הזו לזמן קצר מאוד, 
                    // ולא את כל המטריצה. זה מאפשר לתהליכונים אחרים לעבוד על שורות אחרות במקביל.
                    newVector[c] += scalar * matrixRow.get(c);
                }
            }
        } finally {
            readUnlock();
        }

        // שלב 3: עדכון הפוינטר (פעולה אטומית מהירה)
        writeLock();
        try {
            this.vector = newVector;
        } finally {
            writeUnlock();
        }
    }
}
