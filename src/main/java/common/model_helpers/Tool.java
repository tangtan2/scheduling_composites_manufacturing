package common.model_helpers;
import ilog.concert.IloCumulFunctionExpr;

public class Tool {

    // Private class variables
    private String name;
    private int capacity;
    private int qty;
    private int min;
    private int size;
    private IloCumulFunctionExpr cumul;

    // Constructor
    public Tool(String name, int cap, int qty, int min, int size) {

        this.name = name;
        this.capacity = cap;
        this.qty = qty;
        this.min = min;
        this.size = size;

    }
    public Tool(String name, int cap, int qty, int min) {

        this.name = name;
        this.capacity = cap;
        this.qty = qty;
        this.min = min;
        this.size = 0;

    }

    // Add cumulative expression
    public void setCumul(IloCumulFunctionExpr cumul) {

        this.cumul = cumul;

    }

    // Accessors
    public String name() {return this.name;}
    public int capacity() {return this.capacity;}
    public int qty() {return this.qty;}
    public int min() {return this.min;}
    public int size() {return this.size;}
    public IloCumulFunctionExpr cumul() {return this.cumul;}

}
