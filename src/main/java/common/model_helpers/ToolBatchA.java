package common.model_helpers;

public class ToolBatchA extends ToolBatch {

    // Private class variables
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
    private int due = (int) Double.POSITIVE_INFINITY;

    // Constructor
    public ToolBatchA(ToolH bottom, int index) {

        super(bottom, index);
        this.bottomTool = bottom;

    }

    // Create activities
    public int createActivities(int index) {

        for (Job j : this.jobs()) {
            if (j.due() < this.due) {
                this.due = j.due();
            }
        }
        this.prepAct = new Activity(this.jobs().get(0).steps()[0], index++, 0, this.due, this.jobs().get(0).stepTimes()[0]);
        this.layupAct = new Activity(this.jobs().get(0).steps()[1], index++, 1, this.due, this.jobs().get(0).stepTimes()[1]);
        this.layupAct.addPredecessor(prepAct);
        this.demouldAct = new Activity(this.jobs().get(0).steps()[3], index++, 3, this.due, this.jobs().get(0).stepTimes()[3]);
        this.demouldAct.addPredecessor(prepAct);
        this.demouldAct.addPredecessor(layupAct);
        return index;

    }

    // Assign tool batch to autoclave batch
    public void linkActivities(AutoBatchA b2) {

        this.prepAct.setBatch(this);
        this.layupAct.setBatch(this);
        this.demouldAct.setBatch(this);
        this.demouldAct.addPredecessor(b2.autoAct());

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

    // Accessors
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
