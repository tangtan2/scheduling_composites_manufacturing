package common.model_helpers;
import ilog.concert.IloCumulFunctionExpr;

public class Machine {

    // Private class variables
    private final String name;
    private final int[] qtyPerShift;
    private int capacity;
    private final int[] shiftStart;
    private final int[] shiftEnd;
    private IloCumulFunctionExpr cumul;

    // Constructor
    public Machine(String name, int[] qty, int[] start, int[] end) {

        this.name = name;
        this.qtyPerShift = qty;
        this.shiftStart = start;
        this.shiftEnd = end;

    }
    public Machine(String name, int[] qty, int[] start, int[] end, int capacity) {

        this.name = name;
        this.qtyPerShift = qty;
        this.shiftStart = start;
        this.shiftEnd = end;
        this.capacity = capacity;

    }

    // Add cumulative expression
    public void setCumul(IloCumulFunctionExpr cumul) {

        this.cumul = cumul;

    }

    // Accessors
    public String name() {return this.name;}
    public int[] qtyPerShift() {return this.qtyPerShift;}
    public int[] shiftStart() {return this.shiftStart;}
    public int[] shiftEnd() {return this.shiftEnd;}
    public IloCumulFunctionExpr cumul() {return this.cumul;}
    public int capacity() {return this.capacity;}

}
