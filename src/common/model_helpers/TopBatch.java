package common.model_helpers;
import java.util.ArrayList;
import java.util.List;

public class TopBatch {

    // Private class variables
    private ArrayList<Job> jobs = new ArrayList<>();
    private Tool topTool;
    private ToolBatch b1;

    // Constructor
    public TopBatch(Tool top) {

        this.topTool = top;

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

}
