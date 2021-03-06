package common.model_helpers;

public class ToolH extends Tool {

    // Private class variables
    private Horizon horizon;
    private int horizonEnd;

    // Constructors
    public ToolH(String name, int cap, int qty, int min, int horizonEnd, int size) {

        super(name, cap, qty, min, size);
        this.horizon = createHorizon(horizonEnd);
        this.horizonEnd = horizonEnd;

    }

    // Create horizon object
    private Horizon createHorizon(int end) {

        return new Horizon(end, this.qty());

    }

    // Reset horizon object
    public void resetHorizon() {

        this.horizon = new Horizon(this.horizonEnd, this.qty());

    }

    // Accessors
    public Horizon horizon() {return this.horizon;}

}
