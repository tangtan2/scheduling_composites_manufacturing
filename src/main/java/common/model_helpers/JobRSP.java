package common.model_helpers;
import ilog.concert.IloIntVar;
import ilog.concert.IloIntervalVar;

public class JobRSP extends Job {

    // Private class variables
    private IloIntervalVar prepVar;
    private IloIntervalVar layupVar;
    private IloIntervalVar autoVar;
    private IloIntervalVar demouldVar;
    private IloIntVar tardyVar;
    private Machine prepMachine;
    private Machine layupMachine;
    private Machine demouldMachine;

    // Constructor
    public JobRSP(String name, String family, int index, int size, int due, String[] steps, int[] stepTimes) {

        super(name, family, 0, index, size, due, steps, stepTimes);

    }

    // Set variables
    public void setVars(IloIntervalVar p, IloIntervalVar l, IloIntervalVar a, IloIntervalVar d, IloIntVar tardy) {

        this.prepVar = p;
        this.layupVar = l;
        this.autoVar = a;
        this.demouldVar = d;
        this.tardyVar = tardy;

    }

    // Set auto machine
    public void setPrepMachine(Machine prepMachine) {

        this.prepMachine = prepMachine;

    }

    // Set layup machine
    public void setLayupMachine(Machine layupMachine) {

        this.layupMachine = layupMachine;

    }

    // Set demould machine
    public void setDemouldMachine(Machine demouldMachine) {

        this.demouldMachine = demouldMachine;

    }

    // Accessors
    public IloIntervalVar prepVar() {return this.prepVar;}
    public IloIntervalVar layupVar() {return this.layupVar;}
    public IloIntervalVar autoVar() {return this.autoVar;}
    public IloIntervalVar demouldVar() {return this.demouldVar;}
    public IloIntVar tardyVar() {return this.tardyVar;}
    public Machine prepMachine() {return this.prepMachine;}
    public Machine layupMachine() {return this.layupMachine;}
    public Machine demouldMachine() {return this.demouldMachine;}

}
