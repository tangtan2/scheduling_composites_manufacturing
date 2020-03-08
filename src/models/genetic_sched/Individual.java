package models.genetic_sched;
import java.util.ArrayList;

public class Individual {

    // Private class variables
    private ArrayList<ActivityG> activityList;
    private int[][] schedule;
    private int fitness;

    // Constructor
    public Individual(ArrayList<ActivityG> activityList, int[][] schedule, int fitness) {

        this.activityList = activityList;
        this.schedule = schedule;
        this.fitness = fitness;

    }

    // Accessors
    public ArrayList<ActivityG> activityList() {return this.activityList;}
    public int[][] schedule() {return this.schedule;}
    public int fitness() {return this.fitness;}

}
