package models.genetic_sched;
import common.model_helpers.Activity;
import java.util.ArrayList;

public class Individual {

    // Private class variables
    private ArrayList<Activity> activityList;
    private int[][] schedule;
    private int fitness;

    // Constructor
    public Individual(ArrayList<Activity> activityList, int[][] schedule, int fitness) {

        this.activityList = activityList;
        this.schedule = schedule;
        this.fitness = fitness;

    }

    // Accessors
    public ArrayList<Activity> activityList() {return this.activityList;}
    public int[][] schedule() {return this.schedule;}
    public int fitness() {return this.fitness;}

}
