package common.model_helpers;
import java.util.ArrayList;
import java.util.List;

public class TopBatch {

    // Private class variables
    private final ArrayList<Job> jobs = new ArrayList<>();
    private final Tool topTool;
    private ToolBatch b1;
    private int index;

    // Constructor
    public TopBatch(Tool top) {

        this.topTool = top;

    }
    public TopBatch(Tool top, int index) {

        this.topTool = top;
        this.index = index;

    }

    // Add index
    public void addIndex(int index) {

        this.index = index;

    }

    // Add jobs
    public void addJob(Job newjob) {

        this.jobs.add(newjob);

    }
    public void addJob(List<Job> newjobs) {

        this.jobs.addAll(newjobs);

    }

    // Assign top batch to a tool batch
    public void setToolBatch(ToolBatch tool) {

        this.b1 = tool;

    }

    // Accessors
    public ArrayList<Job> jobs() {return this.jobs;}
    public Tool topTool() {return this.topTool;}
    public ToolBatch b1() {return this.b1;}
    public int index() {return this.index;}

}
