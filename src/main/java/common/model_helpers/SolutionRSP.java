package common.model_helpers;
import java.util.ArrayList;

public class SolutionRSP extends Solution {

    // Private class variables
    private ArrayList<JobRSP> RSPjobs = new ArrayList<>();

    // Constructor
    public SolutionRSP(int numjob, double obj, double time, String status) {

        super(numjob, obj, time, status);

    }

    // Add jobs
    public void addJobs(ArrayList<JobRSP> jobs) {

        this.RSPjobs = jobs;

    }

    // Accessors
    public ArrayList<JobRSP> RSPjobs() {return this.RSPjobs;}

}
