package models.genetic_sched;
import common.model_helpers.*;
import java.util.ArrayList;

public class AutoBatchA extends AutoBatch {

    // Private class variables
    private ArrayList<ToolBatchA> b1s = new ArrayList<>();
    private ActivityG autoAct;
    private int due = (int) Double.POSITIVE_INFINITY;
    private MachineH autoMachine;

    // Coonstructor
    public AutoBatchA(int cap) {

        super(cap);

    }

    // Add tool batch
    public void addToolBatchA(ToolBatchA newtool) {

        this.addToolBatch(newtool);
        for (Job j : this.jobs()) {
            if (j.due() < this.due) {
                this.due = j.due();
            }
        }

    }

    // Create activity
    public int createActivity(int index) {

        this.autoAct = new ActivityG(this.jobs().get(0).steps()[2],
                index++, 2, this.due,
                this.jobs().get(0).stepTimes()[2]);
        return index;

    }

    // Set curing machine
    public void setAutoMachineH(MachineH m) {

        this.autoMachine = m;

    }

    // Accessors
    public ArrayList<ToolBatchA> b1sA() {return this.b1s;}
    public ActivityG autoAct() {return this.autoAct;}
    public int due() {return this.due;}
    public MachineH autoMachine() {return this.autoMachine;}

}
