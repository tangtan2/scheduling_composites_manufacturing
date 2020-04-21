package common.model_helpers;
import ilog.concert.IloCumulFunctionExpr;
import java.util.ArrayList;

public class Labour {

    // Private class variables
    private final String name;
    private final ArrayList<String> skills;
    private final int[] qtyPerShift;
    private final int[] shiftStart;
    private final int[] shiftEnd;
    private IloCumulFunctionExpr cumul;

    // Constructor
    public Labour(String name, ArrayList<String> skills, int[] qty, int[] start, int[] end) {

        this.name = name;
        this.skills = skills;
        this.qtyPerShift = qty;
        this.shiftStart = start;
        this.shiftEnd = end;

    }

    // Add cumulative expression
    public void setCumul(IloCumulFunctionExpr cumul) {

        this.cumul = cumul;

    }

    // Accessors
    public String name() {return this.name;}
    public ArrayList<String> skills() {return this.skills;}
    public int[] qtyPerShift() {return this.qtyPerShift;}
    public int[] shiftStart() {return this.shiftStart;}
    public int[] shiftEnd() {return this.shiftEnd;}
    public IloCumulFunctionExpr cumul() {return this.cumul;}

}
