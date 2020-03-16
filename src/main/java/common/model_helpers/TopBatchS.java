package common.model_helpers;
import java.util.ArrayList;

public class TopBatchS extends TopBatch {

    // Private class variables
    private ArrayList<JobS> jobs = new ArrayList<>();
    private ToolBatchS b1;

    // Constructor
    public TopBatchS(Tool top, int i) {

        super(top, i);

    }

    // Add job
    public void addJobS(JobS newjob) {

        this.jobs.add(newjob);

    }

    // Assign top batch to a tool batch
    public void setToolBatchS(ToolBatchS b1) {

        this.b1 = b1;

    }

    // Accessors
    public ArrayList<JobS> jobsS() {return this.jobs;}
    public ToolBatchS b1S() {return this.b1;}

}
