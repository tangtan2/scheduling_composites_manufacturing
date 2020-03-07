package models.edd_serial_sched;
import common.model_helpers.Labour;
import java.util.ArrayList;

public class LabourH extends Labour {

    // Private class variables
    private Horizon horizon;
    private int horizonEnd;

    // Constructor
    public LabourH(String name, ArrayList<String> skills, int[] qty, int[] start, int[] end, int horizonEnd) {

        super(name, skills, qty, start, end);
        this.horizon = createHorizon(horizonEnd);
        this.horizonEnd = horizonEnd;

    }

    // Create horizon object
    private Horizon createHorizon(int end) {

        return new Horizon(end, this.qtyPerShift(), this.shiftStart(), this.shiftEnd());

    }

    // Reset horizon object
    public void resetHorizon() {

        this.horizon = new Horizon(this.horizonEnd, this.qtyPerShift(), this.shiftStart(), this.shiftEnd());

    }

    // Accessors
    public Horizon horizon() {return this.horizon;}

}
