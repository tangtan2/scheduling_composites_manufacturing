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
    private ArrayList<JobS> jobsS = new ArrayList<>();
    private ArrayList<TopBatchS> b0sS = new ArrayList<>();
    private ArrayList<ToolBatchS> b1sS = new ArrayList<>();
    private ArrayList<AutoBatchS> b2sS = new ArrayList<>();
    private ArrayList<Activity> acts = new ArrayList<>();

    // Constructor
    public Solution(int numjob, double obj, double time, String status) {

        this.numJob = numjob;
        this.objVal = obj;
        this.elapsedTime = time;
        this.status = status;

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
    public void addAutoBatchS(ArrayList<AutoBatchS> b2s) {

        this.b2sS = b2s;
        for (AutoBatchS a : b2s) {
            this.jobsS.addAll(a.jobsS());
            this.b1sS.addAll(a.b1sS());
            for (ToolBatchS t : a.b1sS()) {
                this.b0sS.addAll(t.b0sS());
            }
        }

    }

    // Add activities
    public void addActivities(ArrayList<Activity> activities) {

        this.acts = activities;

    }

    // Replace time
    public void replaceTime(double time) {

        this.elapsedTime = time;

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
    public ArrayList<JobS> jobsS() {return this.jobsS;}
    public ArrayList<TopBatchS> b0sS() {return this.b0sS;}
    public ArrayList<ToolBatchS> b1sS() {return this.b1sS;}
    public ArrayList<AutoBatchS> b2sS() {return this.b2sS;}
    public ArrayList<Activity> acts() {return this.acts;}

}
