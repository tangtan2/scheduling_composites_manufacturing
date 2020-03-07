package models.basic_lbbd;
import common.model_helpers.Job;
import ilog.concert.IloIntVar;
import ilog.concert.IloIntervalVar;

public class JobS extends Job {

    // Private class variables
    private IloIntervalVar prepVar;
    private IloIntervalVar layupVar;
    private IloIntervalVar autoVar;
    private IloIntervalVar demouldVar;
    private IloIntVar tardyVar;
    private TopBatchS b0;

    // Constructor
    public JobS(String name, String family, int autocap, int index, int size, int due, String[] steps, int[] stepTimes) {

        super(name, family, autocap, index, size, due, steps, stepTimes);

    }

    // Assign job to top batch
    public void setTopBatchS(TopBatchS b0) {

        this.b0 = b0;

    }

    // Set variables
    public void setVars(IloIntervalVar p, IloIntervalVar l, IloIntervalVar a, IloIntervalVar d, IloIntVar tardy) {

        this.prepVar = p;
        this.layupVar = l;
        this.autoVar = a;
        this.demouldVar = d;
        this.tardyVar = tardy;

    }

    // Accessors
    public IloIntervalVar prepVar() {return this.prepVar;}
    public IloIntervalVar layupVar() {return this.layupVar;}
    public IloIntervalVar autoVar() {return this.autoVar;}
    public IloIntervalVar demouldVar() {return this.demouldVar;}
    public IloIntVar tardyVar() {return this.tardyVar;}
    public TopBatchS b0S() {return this.b0;}

}
