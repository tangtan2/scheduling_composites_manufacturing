package models.basic_cp_sched;
import common.model_helpers.*;
import ilog.concert.IloIntervalVar;
import java.util.ArrayList;
import java.util.List;

public class ToolBatchS extends ToolBatch {

    // Private class variables
    private IloIntervalVar prepVar;
    private IloIntervalVar layupVar;
    private IloIntervalVar demouldVar;
    private ArrayList<JobS> jobs = new ArrayList<>();
    private ArrayList<TopBatchS> b0s = new ArrayList<>();
    private AutoBatchS b2;

    // Constructor
    public ToolBatchS(Tool bottom) {

        super(bottom);

    }

    // Add top batch
    public void addTopBatchS(TopBatchS newtop) {

        this.b0s.add(newtop);
        this.jobs.addAll(newtop.jobsS());

    }
    public void addTopBatchS(List<TopBatchS> newtops) {

        this.b0s.addAll(newtops);
        for (TopBatchS t : newtops) {
            this.jobs.addAll(t.jobsS());
        }

    }

    // Set variables
    public void setVars(IloIntervalVar p, IloIntervalVar l, IloIntervalVar d) {

        this.prepVar = p;
        this.layupVar = l;
        this.demouldVar = d;

    }

    // Assign tool batch to an autoclave batch
    public void setAutoBatchS(AutoBatchS b2) {

        this.b2 = b2;

    }

    // Accessors
    public IloIntervalVar prepVar() {return this.prepVar;}
    public IloIntervalVar layupVar() {return this.layupVar;}
    public IloIntervalVar demouldVar() {return this.demouldVar;}
    public ArrayList<JobS> jobsS() {return this.jobs;}
    public ArrayList<TopBatchS> b0sS() {return this.b0s;}
    public AutoBatchS b2S() {return this.b2;}

}
