package common.model_helpers;
import java.util.ArrayList;

public class AutoBatch {

    // Private class variables
    private int index;
    private ArrayList<Job> jobs = new ArrayList<>();
    private ArrayList<ToolBatch> b1s = new ArrayList<>();
    private int capacity;
    private int sumToolSize = 0;
    private int autoStart;
    private int autoEnd;
    private Machine autoMachine;

    // Constructor
    public AutoBatch(int cap) {

        this.capacity = cap;

    }
    public AutoBatch(int cap, int index) {

        this.capacity = cap;
        this.index = index;

    }

    // Add index
    public void addIndex(int index) {

        this.index = index;

    }

    // Add tool batch
    public void addToolBatch(ToolBatch newtool) {

        this.b1s.add(newtool);
        this.jobs.addAll(newtool.jobs());
        this.sumToolSize += newtool.size();

    }

    // Add tool size
    public void addToolSize(int size) {

        this.sumToolSize += size;

    }

    // Set capacity
    public void setCapacity(int cap) {

        this.capacity = cap;

    }

    // Set curing machine
    public void setAutoMachine(Machine m) {

        this.autoMachine = m;

    }

    // Schedule
    public void schedule(int as, int ae) {

        this.autoStart = as;
        this.autoEnd = ae;

    }

    // Accessors
    public ArrayList<Job> jobs() {return this.jobs;}
    public ArrayList<ToolBatch> b1s() {return this.b1s;}
    public int capacity() {return this.capacity;}
    public int sumToolSize() {return this.sumToolSize;}
    public int autoStart() {return this.autoStart;}
    public int autoEnd() {return this.autoEnd;}
    public Machine autoMachine() {return this.autoMachine;}
    public int index() {return this.index;}

}