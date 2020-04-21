package models.cluster_pack;
import common.model_helpers.ToolBatch;

public class Point {

    // Private class variables
    private final ToolBatch b1;
    private final int size;
    private final int location;
    private final int index;

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
