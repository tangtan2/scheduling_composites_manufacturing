package common.model_helpers;
import ilog.concert.*;

public class JobS extends Job {

    // Private class variables
    private IloIntVar tardyVar;
    private TopBatchS b0;

    // Constructor
    public JobS(String name, String family, int index, int size, int due, String[] steps, int[] stepTimes) {

        super(name, family, 0, index, size, due, steps, stepTimes);

    }
    public JobS(String name, String family, int autocap, int index, int size, int due, String[] steps, int[] stepTimes) {

        super(name, family, autocap, index, size, due, steps, stepTimes);

    }

    // Assign job to top batch
    public void setTopBatchS(TopBatchS b0) {

        this.b0 = b0;

    }

    // Add variable
    public void addVar(IloIntVar tardyVar) {

        this.tardyVar = tardyVar;

    }

    // Accessors
    public IloIntVar tardyVar() {return this.tardyVar;}
    public TopBatchS b0S() {return this.b0;}

}
