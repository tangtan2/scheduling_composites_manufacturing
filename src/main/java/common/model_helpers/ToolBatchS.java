package common.model_helpers;
import ilog.concert.IloIntervalVar;
import java.util.ArrayList;

public class ToolBatchS extends ToolBatch {

    // Private class variables
    private IloIntervalVar prepVar;
    private IloIntervalVar layupVar;
    private IloIntervalVar demouldVar;
    private ArrayList<JobS> jobs = new ArrayList<>();
    private ArrayList<TopBatchS> b0s = new ArrayList<>();
    private AutoBatchS b2;
    private int rspOrder;

    // Constructor
    public ToolBatchS(Tool bottom, int i) {

        super(bottom, i);

    }

    // Add top batch
    public void addTopBatchS(TopBatchS newtop) {

        this.b0s.add(newtop);
        this.jobs.addAll(newtop.jobsS());

    }

    // Set variables
    public void setVariables(IloIntervalVar p, IloIntervalVar l, IloIntervalVar d) {

        this.prepVar = p;
        this.layupVar = l;
        this.demouldVar = d;

    }

    // Assign tool batch to an autoclave batch
    public void setAutoBatchS(AutoBatchS b2) {

        this.b2 = b2;

    }

    // Calculate RSP value
    public void calcRSP() {

        int sumrsp = 0;
        for (Job j : this.jobs) {
            sumrsp += j.rspOrder();
        }
        this.rspOrder = Math.round(sumrsp / (float) this.jobs.size());

    }

    // Accessors
    public IloIntervalVar prepVar() {return this.prepVar;}
    public IloIntervalVar layupVar() {return this.layupVar;}
    public IloIntervalVar demouldVar() {return this.demouldVar;}
    public ArrayList<JobS> jobsS() {return this.jobs;}
    public ArrayList<TopBatchS> b0sS() {return this.b0s;}
    public AutoBatchS b2S() {return this.b2;}
    public int rspOrder() {return this.rspOrder;}

}
