package models.edd_serial_sched;
import common.model_helpers.*;
import java.util.ArrayList;

public class AutoBatchA extends AutoBatch {

    // Private class variables
    private ArrayList<ToolBatchA> b1s = new ArrayList<>();
    private Activity autoAct;
    private int due = (int) Double.POSITIVE_INFINITY;

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
    public void createActivity() {

        this.autoAct = new Activity(this.jobs().get(0).steps()[2], this.jobs().get(0).stepTimes()[2], this);

    }

    // Accessors
    public ArrayList<ToolBatchA> b1sA() {return this.b1s;}
    public Activity autoAct() {return this.autoAct;}
    public int due() {return this.due;}

}
