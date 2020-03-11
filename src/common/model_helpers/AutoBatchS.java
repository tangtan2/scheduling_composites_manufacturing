package common.model_helpers;
import java.util.ArrayList;
import java.util.List;
import ilog.concert.*;

public class AutoBatchS extends AutoBatch {

    // Private class variables
    private ArrayList<JobS> jobs = new ArrayList<>();
    private ArrayList<ToolBatchS> b1s = new ArrayList<>();
    private IloIntervalVar autoVar;

    // Constructor
    public AutoBatchS(int cap, int i) {

        super(cap, i);

    }

    // Add tool batch
    public void addToolBatchS(ToolBatchS newtool) {

        this.b1s.add(newtool);
        this.jobs.addAll(newtool.jobsS());
        addToolSize(newtool.size());

    }
    public void addToolBatchS(List<ToolBatchS> newtools) {

        this.b1s.addAll(newtools);
        for (ToolBatchS t : newtools) {
            this.jobs.addAll(t.jobsS());
            addToolSize(t.size());
        }

    }

    // Set auto interval variable
    public void setAuto(IloIntervalVar auto) {

        this.autoVar = auto;

    }

    // Accessors
    public ArrayList<JobS> jobsS() {return this.jobs;}
    public ArrayList<ToolBatchS> b1sS() {return this.b1s;}
    public IloIntervalVar autoVar() {return this.autoVar;}

}
