package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) {
            ComputationNode cn = computationRoot.findResolvable();
            if (cn == null) {
                break;
            }
            loadAndCompute(cn);
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        List<ComputationNode> children = node.getChildren();
        leftMatrix.loadRowMajor(children.getFirst().getMatrix());
        if (children.size() > 1) {
            rightMatrix.loadRowMajor(children.getLast().getMatrix());
        }
        List<Runnable> tasks = null;
        switch (node.getNodeType()) {
            case ADD:
                tasks = createAddTasks();
                break;
            case MULTIPLY:
                tasks = createMultiplyTasks();
                break;
            case NEGATE:
                tasks = createNegateTasks();
                break;
            case TRANSPOSE:
                tasks = createTransposeTasks();
                break;
            default:
                throw new IllegalArgumentException("Unknown operation"); 
        }
        executor.submitAll(tasks);
        node.resolve(leftMatrix.readRowMajor());
        

    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();
        for (int i = 0; i < rows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                SharedVector v1 = leftMatrix.get(rowIndex);
                SharedVector v2 = rightMatrix.get(rowIndex);
                v1.add(v2);
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();
        for (int i = 0; i < rows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                SharedVector v = leftMatrix.get(rowIndex);
                v.vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();
        for (int i = 0; i < rows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                SharedVector v = leftMatrix.get(rowIndex);
                v.negate();
            });
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
                List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();
        for (int i = 0; i < rows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                SharedVector v = leftMatrix.get(rowIndex);
                v.transpose();
            });
        }
        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }
}
