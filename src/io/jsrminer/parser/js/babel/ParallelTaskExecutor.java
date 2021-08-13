package io.jsrminer.parser.js.babel;

import io.jsrminer.uml.UMLModel;

public class ParallelTaskExecutor extends Thread {
    public interface ParallelTaskRunner {
        void run();
    }

    private ParallelTaskRunner runner;

    ParallelTaskExecutor(ParallelTaskRunner runner) {
        this.runner = runner;
    }

    UMLModel model;

    ParallelTaskExecutor(UMLModel model) {
        this.model = model;
    }

    public void run() {
        //model.printCount();
        //System.out.println("Thread " +  threadName + " exiting.");
    }
}
