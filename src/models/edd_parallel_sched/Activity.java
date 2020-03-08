package models.edd_parallel_sched;
import common.model_helpers.AutoBatch;
import common.model_helpers.ToolBatch;

import java.util.ArrayList;

public class Activity {

    // Private class variables
    private String name;
    private int length;
    private int start;
    private int end;
    private AutoBatchA b2 = null;
    private ToolBatchA b1 = null;
    private ArrayList<Activity> predecessors = new ArrayList<>();
    private int maxPredEnd = 0;

    // Constructor
    public Activity(String name, int length, AutoBatchA associatedbatch) {

        this.name = name;
        this.length = length;
        this.b2 = associatedbatch;

    }
    public Activity(String name, int length, ToolBatchA associatedbatch) {

        this.name = name;
        this.length = length;
        this.b1 = associatedbatch;

    }

    // Add predecessor activity
    public void addPred(Activity pred) {

        this.predecessors.add(pred);

    }

    // Set maximinum predecessor end time
    public void setMaxPred(int end) {

        this.maxPredEnd = end;

    }

    // Schedule activituy
    public void schedule(int start, int end) {

        assert (end - start) == length;
        this.start = start;
        this.end = end;

    }

    // Accessors
    public String name() {return this.name;}
    public int length() {return this.length;}
    public int start() {return this.start;}
    public int end() {return this.end;}
    public AutoBatchA b2() {return this.b2;}
    public ToolBatchA b1() {return this.b1;}
    public ArrayList<Activity> predecessors() {return this.predecessors;}
    public int maxPredEnd() {return this.maxPredEnd;}

}
