package models.genetic_sched;
import java.util.ArrayList;

public class ActivityG {

    // Private class variables
    private String name;
    private int length;
    private int start;
    private int end;
    private AutoBatchA b2;
    private ToolBatchA b1;
    private ArrayList<ActivityG> predecessors = new ArrayList<>();
    private int due;
    private int type;
    private int index;
    private int priority;

    // Constructor
    public ActivityG(String name, int index, int type, int due, int length) {

        this.name = name;
        this.length = length;
        this.index = index;
        this.type = type;
        this.due = due;

    }

    // Set associated batches
    public void setBatches(ToolBatchA b1, AutoBatchA b2) {

        this.b1 = b1;
        this.b2 = b2;

    }
    public void setBatches(AutoBatchA b2) {

        this.b2 = b2;

    }

    // Add priority
    public void addPriority(int priority) {

        this.priority = priority;

    }

    // Add predecessor activity
    public void addPred(ActivityG pred) {

        this.predecessors.add(pred);

    }

    // Schedule activity
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
    public ArrayList<ActivityG> predecessors() {return this.predecessors;}
    public int due() {return this.due;}
    public int type() {return this.type;}
    public int index() {return this.index;}
    public int priority() {return this.priority;}

}
