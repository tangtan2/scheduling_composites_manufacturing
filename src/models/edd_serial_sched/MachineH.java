package models.edd_serial_sched;
import common.model_helpers.Machine;

public class MachineH extends Machine {

    // Private class variables
    private Horizon horizon;
    private int horizonEnd;

    // Constructor
    public MachineH(String name, int[] qty, int[] start, int[] end, int horizonEnd) {

        super(name, qty, start, end);
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
