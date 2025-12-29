package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {

    }

    public SharedMatrix(double[][] matrix) {
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i < newVectors.length; i++) {
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        } 
        this.vectors = newVectors;
    }

    public void loadColumnMajor(double[][] matrix) {
       if (matrix == null || matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }

        int rows = matrix.length;
        int cols = matrix[0].length;

        // שים לב: המערך החדש הוא בגודל מספר העמודות!
        SharedVector[] newVectors = new SharedVector[cols];

        for (int j = 0; j < cols; j++) {
            // יוצרים מערך חדש עבור כל עמודה
            double[] columnData = new double[rows];
            
            // מעתיקים את הנתונים: רצים על השורות (i) ולוקחים את האיבר ה-j
            for (int i = 0; i < rows; i++) {
                columnData[i] = matrix[i][j];
            }
            
            // עכשיו הוקטור הזה באמת מייצג עמודה
            newVectors[j] = new SharedVector(columnData, VectorOrientation.COLUMN_MAJOR);
        } 
        
        this.vectors = newVectors;
    }

    public double[][] readRowMajor() {
        SharedVector[] currentVectors = this.vectors;
        acquireAllVectorReadLocks(currentVectors);
        try{
            int row = 0;
            int col = 0;
            if (currentVectors.length > 0) {
                if (currentVectors[0].getOrientation() == VectorOrientation.ROW_MAJOR) {
                    row = currentVectors.length;
                    col = currentVectors[0].length();
                }
                else{
                    row = currentVectors[0].length();
                    col = currentVectors.length;
                }
            }
            double matrix[][] = new double[row][col];
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    if (currentVectors.length > 0 && currentVectors[0].getOrientation() == VectorOrientation.ROW_MAJOR){
                        matrix[i][j] = currentVectors[i].get(j);
                    }
                    else {
                        matrix[i][j] = currentVectors[j].get(i);
                    }
                }
            }
            return matrix;
        }
        finally{
            releaseAllVectorReadLocks(currentVectors);
        }
    }

    public SharedVector get(int index) {
        return vectors[index];
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if (vectors.length > 0) {
            return vectors[0].getOrientation();
        }
        return VectorOrientation.ROW_MAJOR;
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vec: vecs){
            vec.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.writeUnlock();
        }
    }
}
