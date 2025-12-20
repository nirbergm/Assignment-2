package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
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
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i < newVectors.length; i++) {
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.COLUMN_MAJOR);
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
        // TODO: return vector at index
        return null;
    }

    public int length() {
        // TODO: return number of stored vectors
        return 0;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        return null;
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
    }
}
