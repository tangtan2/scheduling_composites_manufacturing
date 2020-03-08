package models.edd_serial_sched;
import common.model_helpers.*;

public class ToolBatchA extends ToolBatch {

    // Private class variables
    private AutoBatchA b2;
    private ToolH bottomTool;
    private Activity prepAct;
    private Activity layupAct;
    private Activity demouldAct;
    private MachineH prepMachine;
    private MachineH layupMachine;
    private MachineH demouldMachine;
    private LabourH prepLabour;
    private LabourH layupLabour;
    private LabourH demouldLabour;
    private int prepQty;
    private int layupQty;
    private int demouldQty;

    // Constructor
    public ToolBatchA(ToolH bottom) {

        super(bottom);
        this.bottomTool = bottom;

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

    // Set prep machine
    public void setPrepMachineH(MachineH m) {

        this.prepMachine = m;

    }

    // Set layup machine
    public void setLayupMachineH(MachineH m) {

        this.layupMachine = m;

    }

    // Set demould machine
    public void setDemouldMachineH(MachineH m) {

        this.demouldMachine = m;

    }

    // Set prep labour
    public void setPrepLabourH(LabourH l, int qty) {

        this.prepLabour = l;
        this.prepQty = qty;

    }

    // Set layup labour
    public void setLayupLabourH(LabourH l, int qty) {

        this.layupLabour = l;
        this.layupQty = qty;

    }

    // Set demould labour
    public void setDemouldLabourH(LabourH l, int qty) {

        this.demouldLabour = l;
        this.demouldQty = qty;

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
    public ToolH bottomToolH() {return this.bottomTool;}
    public Activity prepAct() {return this.prepAct;}
    public Activity layupAct() {return this.layupAct;}
    public Activity demouldAct() {return this.demouldAct;}
    public MachineH prepMachineH() {return this.prepMachine;}
    public MachineH layupMachineH() {return this.layupMachine;}
    public MachineH demouldMachineH() {return this.demouldMachine;}
    public LabourH prepLabourH() {return this.prepLabour;}
    public LabourH layupLabourH() {return this.layupLabour;}
    public LabourH demouldLabourH() {return this.demouldLabour;}
    public int prepQty() {return this.prepQty;}
    public int layupQty() {return this.layupQty;}
    public int demouldQty() {return this.demouldQty;}

}
