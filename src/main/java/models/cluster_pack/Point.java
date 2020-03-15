package models.cluster_pack;
import common.model_helpers.ToolBatch;

public class Point {

    // Private class variables
    private ToolBatch b1;
    private int size;
    private int location;
    private int index;

    // Constructor
    public Point(int index, ToolBatch indexB1, int size, int location) {

        this.index = index;
        this.b1 = indexB1;
        this.size = size;
        this.location = location;

    }

    // Accessors
    public int index() {return this.index;}
    public ToolBatch b1() {return this.b1;}
    public int size() {return this.size;}
    public int location() {return this.location;}

}
