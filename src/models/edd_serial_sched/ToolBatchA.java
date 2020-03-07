package models.edd_serial_sched;
import common.model_helpers.*;

public class ToolBatchA extends ToolBatch {

    // Private class variables
    private AutoBatchA b2;
    private Activity prepAct;
    private Activity layupAct;
    private Activity demouldAct;

    // Constructor
    public ToolBatchA(Tool bottom) {

        super(bottom);

    }

    // Create activities
    public void createActivities() {

        this.prepAct = new Activity(this.jobs().get(0).steps()[0],
                this.jobs().stream().map(Job::stepTimes).map(x -> x[0]).reduce(0, Integer::sum),
                this);
        this.layupAct = new Activity(this.jobs().get(0).steps()[1],
                this.jobs().stream().map(Job::stepTimes).map(x -> x[1]).reduce(0, Integer::sum),
                this);
        this.layupAct.addPred(prepAct);
        this.demouldAct = new Activity(this.jobs().get(0).steps()[3],
                this.jobs().stream().map(Job::stepTimes).map(x -> x[3]).reduce(0, Integer::sum),
                this);
        this.demouldAct.addPred(prepAct);
        this.demouldAct.addPred(layupAct);

    }

    // Assign tool batch to autoclave batch
    public void setAutoBatchA(AutoBatchA b2) {

        this.b2 = b2;
        this.demouldAct.addPred(b2.autoAct());

    }

    // Schedule in prep
    public void prepSched(int s, int e) {

        this.prepAct.schedule(s, e);

    }

    // Schedule in layup
    public void layupSched(int s, int e) {

        this.layupAct.schedule(s, e);

    }

    // Schedule in demould
    public void demouldSched(int s, int e) {

        this.demouldAct.schedule(s, e);

    }

    // Accessors
    public AutoBatchA b2A() {return this.b2;}
    public Activity prepAct() {return this.prepAct;}
    public Activity layupAct() {return this.layupAct;}
    public Activity demouldAct() {return this.demouldAct;}

}
