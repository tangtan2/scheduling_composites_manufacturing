package common.model_helpers;
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
    private int maxPredecessorEnd = 0;
    private int due;
    private int type;
    private int index;

    // Constructor
    public Activity(String name, int index, int type, int due, int length) {

        this.name = name;
        this.length = length;
        this.index = index;
        this.type = type;
        this.due = due;

    }

    // Set associated batches
    public void setBatch(ToolBatchA b1) {

        this.b1 = b1;

    }
    public void setBatch(AutoBatchA b2) {

        this.b2 = b2;

    }

    // Add predecessor activity
    public void addPredecessor(Activity pred) {

        this.predecessors.add(pred);

    }

    // Set maximinum predecessor end time
    public void setMaxPredecessorEnd(int end) {

        this.maxPredecessorEnd = end;

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
    public ArrayList<Activity> predecessors() {return this.predecessors;}
    public int maxPredEnd() {return this.maxPredecessorEnd;}
    public int due() {return this.due;}
    public int type() {return this.type;}
    public int index() {return this.index;}

}
