package common.model_helpers;
import java.util.*;

public class SolutionSched extends Solution {

    // Private class variables
    private ArrayList<AutoBatchS> b2sS = new ArrayList<>();
    private ArrayList<Activity> acts = new ArrayList<>();

    // Constructor
    public SolutionSched(int numjob, double obj, double time, String status) {

        super(numjob, obj, time, status);

    }

    // Add auto batches of AutoBatchS type
    public void addAutoBatchS(ArrayList<AutoBatchS> b2objs) {

        this.b2sS = b2objs;

    }

    // Add activities
    public void addActivities(ArrayList<Activity> activities) {

        this.acts = activities;

    }

    // Accessors
    public ArrayList<AutoBatchS> b2sS() {return this.b2sS;}
    public ArrayList<Activity> acts() {return this.acts;}

}
