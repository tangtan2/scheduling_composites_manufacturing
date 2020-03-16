package common.model_helpers;
import java.util.ArrayList;

public class AutoBatchA extends AutoBatch {

    // Private class variables
    private ArrayList<ToolBatchA> b1s = new ArrayList<>();
    private Activity autoAct;
    private int due = (int) Double.POSITIVE_INFINITY;
    private MachineH autoMachine;

    // Coonstructor
    public AutoBatchA(int cap, int index) {

        super(cap, index);

    }

    // Add tool batch
    public void addToolBatchA(ToolBatchA newtool) {

        this.b1s.add(newtool);
        this.addToolBatch(newtool);
        for (Job j : this.jobs()) {
            if (j.due() < this.due) {
                this.due = j.due();
            }
        }

    }

    // Create activity
    public int createActivity(int index) {

        this.autoAct = new Activity(this.jobs().get(0).steps()[2], index++, 2, this.due, this.jobs().get(0).stepTimes()[2]);
        this.autoAct.setBatch(this);
        return index;

    }

    // Link activity to layup predecessor
    public void linkActivities() {

        for (ToolBatchA t : this.b1s) {
            this.autoAct.addPredecessor(t.layupAct());
            this.autoAct.setBatch(t);
            t.prepAct().setBatch(this);
            t.layupAct().setBatch(this);
            t.demouldAct().setBatch(this);
        }

    }

    // Set curing machine
    public void setAutoMachineH(MachineH m) {

        this.autoMachine = m;

    }

    // Accessors
    public ArrayList<ToolBatchA> b1sA() {return this.b1s;}
    public Activity autoAct() {return this.autoAct;}
    public int due() {return this.due;}
    public MachineH autoMachineH() {return this.autoMachine;}

}
