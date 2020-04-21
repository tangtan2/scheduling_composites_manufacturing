package common.model_helpers;
import java.util.ArrayList;

public class Job {

    // Private class variables
    private final String name;
    private final String partFamily;
    private final int autoCap;
    private final int index;
    private final int size;
    private final int due;
    private final String[] steps;
    private final int[] stepTimes;
    private int prepStart;
    private int prepEnd;
    private int layupStart;
    private int layupEnd;
    private int autoStart;
    private int autoEnd;
    private int demouldStart;
    private int demouldEnd;
    private int tardiness;
    private int rspOrder;
    private TopBatch b0;
    private final ArrayList<ToolCombo> mappedCombos;

    // Constructor
    public Job(String name, String family, int autoCap, int index, int size, int due, String[] steps, int[] stepTimes) {

        this.name = name;
        this.partFamily = family;
        this.autoCap = autoCap;
        this.index = index;
        this.size = size;
        this.due = due;
        this.steps = steps;
        this.stepTimes = stepTimes;
        this.mappedCombos = new ArrayList<>();

    }

    // Add tool combo mapping
    public void addToolCombo(ToolCombo newc) {

        this.mappedCombos.add(newc);

    }

    // Remove tool combo mapping
    public void removeToolCombo(ToolCombo oldc) {

        this.mappedCombos.remove(oldc);

    }

    // Set RSP order
    public void setRSP(int rsp) {

        this.rspOrder = rsp;

    }

    // Assign job to a top batch
    public void setTopBatch(TopBatch top) {

        this.b0 = top;

    }

    // Schedule
    public void schedule(int ps, int pe, int ls, int le, int as, int ae, int ds, int de) {

        this.prepStart = ps;
        this.prepEnd = pe;
        this.layupStart = ls;
        this.layupEnd = le;
        this.autoStart = as;
        this.autoEnd = ae;
        this.demouldStart = ds;
        this.demouldEnd = de;
        this.tardiness = Math.max(0, de - this.due);

    }

    // Accessors
    public String name() {return this.name;}
    public String partFamily() {return this.partFamily;}
    public int autoCap() {return this.autoCap;}
    public int index() {return this.index;}
    public int size() {return this.size;}
    public int due() {return this.due;}
    public String[] steps() {return this.steps;}
    public int[] stepTimes() {return this.stepTimes;}
    public int prepStart() {return this.prepStart;}
    public int prepEnd() {return this.prepEnd;}
    public int layupStart() {return this.layupStart;}
    public int layupEnd() {return this.layupEnd;}
    public int autoStart() {return this.autoStart;}
    public int autoEnd() {return this.autoEnd;}
    public int demouldStart() {return this.demouldStart;}
    public int demouldEnd() {return this.demouldEnd;}
    public int tardiness() {return this.tardiness;}
    public int rspOrder() {return this.rspOrder;}
    public TopBatch b0() {return this.b0;}
    public ArrayList<ToolCombo> mappedCombos() {return this.mappedCombos;}

}
