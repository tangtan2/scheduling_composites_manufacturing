package common.model_helpers;
import java.util.ArrayList;

public class Solution {

    // Private class variables
    private int numJob;
    private double objVal;
    private double elapsedTime;
    private String status;
    private ArrayList<Job> jobs = new ArrayList<>();
    private ArrayList<TopBatch> b0s = new ArrayList<>();
    private ArrayList<ToolBatch> b1s = new ArrayList<>();
    private ArrayList<AutoBatch> b2s = new ArrayList<>();
    private int iteration;
    private int overHour;
    private double totElapsedTime;
    private int nonOptimal;

    // Constructor
    public Solution(int numjob, double obj, double time, String status) {

        this.numJob = numjob;
        this.objVal = obj;
        this.elapsedTime = time;
        this.status = status;

    }

    // Add LBBD iterations
    public void addLBBDIter(int it) {

        this.iteration = it;

    }

    // Add LBBD summary
    public void addLBBD(int over, double tot, int non) {

        this.overHour = over;
        this.totElapsedTime = tot;
        this.nonOptimal = non;

    }

    // Add autoclave batch objects
    public void addAutoBatch(ArrayList<AutoBatch> b2s) {

        this.b2s = b2s;
        for (AutoBatch a : b2s) {
            this.jobs.addAll(a.jobs());
            this.b1s.addAll(a.b1s());
            for (ToolBatch t : a.b1s()) {
                this.b0s.addAll(t.b0s());
            }
        }

    }

    // Accessors
    public int numJob() {return this.numJob;}
    public double objVal() {return this.objVal;}
    public double elapsedTime() {return this.elapsedTime;}
    public String status() {return this.status;}
    public ArrayList<Job> jobs() {return this.jobs;}
    public ArrayList<TopBatch> b0s() {return this.b0s;}
    public ArrayList<ToolBatch> b1s() {return this.b1s;}
    public ArrayList<AutoBatch> b2s() {return this.b2s;}
    public int iteration() {return this.iteration;}
    public int overHour() {return this.overHour;}
    public double totElapsedTime() {return this.totElapsedTime;}
    public int nonOptimal() {return this.nonOptimal;}

}
