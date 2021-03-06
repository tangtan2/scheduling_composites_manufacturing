package models.genetic_sched;
import common.model_helpers.Activity;
import common.model_helpers.AutoBatchA;
import java.util.ArrayList;

public class Individual {

    // Private class variables
    private final ArrayList<AutoBatchA> autoBatchList;
    private final ArrayList<Activity> activityList;
    private final int fitness;

    // Constructor
    public Individual(ArrayList<AutoBatchA> autoList, ArrayList<Activity> activityList, int fitness) {

        this.autoBatchList = autoList;
        this.activityList = activityList;
        this.fitness = fitness;

    }

    // Accessors
    public ArrayList<Activity> activityList() {return this.activityList;}
    public ArrayList<AutoBatchA> autoList() {return this.autoBatchList;}
    public int fitness() {return this.fitness;}

}
