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
        double[][] matData = matrix.readRowMajor();
        int matRows = matData.length;
        int matCols = matData[0].length;
        if (vector.length != matRows) {
             throw new IllegalArgumentException("Matrix's rows quantity has to be the same as vector's length");
        }
        double[] newVector = new double[matCols];
        try {
            for (int i = 0; i < matCols; i++) {
                double sum = 0;
                for (int j = 0; j < matRows; j++) {
                    sum += vector[i] * matData[j][i]; 
                }
                vector[i] = sum;
            }
        }
        finally {
            this.readUnlock();
        }
        this.writeLock();
        try {
            vector = newVector;
        }
        finally {
            this.writeUnlock();
        }
    }
}
