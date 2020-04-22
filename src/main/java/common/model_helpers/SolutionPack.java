package common.model_helpers;

public class SolutionPack extends Solution {

    // Private class variables
    private int infeasCounter;
    private int sumRSPDist;

    // Constructor
    public SolutionPack(int numjob, double obj, double time, String status) {

        super(numjob, obj, time, status);

    }

    // Add infeasible bin counter
    public void addInfeas(int count) {
        this.infeasCounter = count;
    }

    // Set RSP dist
    public void setRSP(int rsp) {
        this.sumRSPDist = rsp;
    }

    // Accessors
    public int infeasCounter() {return this.infeasCounter;}
    public int sumRSPDist() {return this.sumRSPDist;}

}
