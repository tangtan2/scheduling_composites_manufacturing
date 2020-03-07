package common.model_helpers;
import java.util.ArrayList;
import java.util.List;

public class ToolBatch {

    // Private class variables
    private ArrayList<Job> jobs = new ArrayList<>();
    private ArrayList<TopBatch> b0s = new ArrayList<>();
    private Tool bottomTool;
    private AutoBatch b2;
    private int size;
    private int prepStart;
    private int prepEnd;
    private int layupStart;
    private int layupEnd;
    private int demouldStart;
    private int demouldEnd;
    private Machine prepMachine;
    private Machine layupMachine;
    private Machine demouldMachine;
    private Labour prepLabour;
    private Labour layupLabour;
    private Labour demouldLabour;
    private int prepQty;
    private int layupQty;
    private int demouldQty;
    private int rspOrder;

    // Constructor
    public ToolBatch(Tool bottom) {

        this.bottomTool = bottom;
        this.size = bottom.size();

    }

    // Add top batch
    public void addTopBatch(TopBatch newtop) {

        this.b0s.add(newtop);
        this.jobs.addAll(newtop.jobs());

    }
    public void addTopBatch(List<TopBatch> newtops) {

        this.b0s.addAll(newtops);
        for (TopBatch t : newtops) {
            this.jobs.addAll(t.jobs());
        }

    }

    // Calculate RSP value
    public void calcRSP() {

        int sumrsp = 0;
        for (Job j : this.jobs) {
            sumrsp += j.rspOrder();
        }
        this.rspOrder = Math.round(sumrsp / (float) this.jobs.size());

    }

    // Assign tool batch to autoclave batch
    public void setAutoBatch(AutoBatch auto) {

        this.b2 = auto;

    }

    // Set prep machine
    public void setPrepMachine(Machine m) {

        this.prepMachine = m;

    }

    // Set layup machine
    public void setLayupMachine(Machine m) {

        this.layupMachine = m;

    }

    // Set demould machine
    public void setDemouldMachine(Machine m) {

        this.demouldMachine = m;

    }

    // Set prep labour
    public void setPrepLabour(Labour l, int qty) {

        this.prepLabour = l;
        this.prepQty = qty;

    }

    // Set layup labour
    public void setLayupLabour(Labour l, int qty) {

        this.layupLabour = l;
        this.layupQty = qty;

    }

    // Set demould labour
    public void setDemouldLabour(Labour l, int qty) {

        this.demouldLabour = l;
        this.demouldQty = qty;

    }

    // Schedule
    public void schedule(int ps, int pe, int ls, int le, int ds, int de) {

        this.prepStart = ps;
        this.prepEnd = pe;
        this.layupStart = ls;
        this.layupEnd = le;
        this.demouldStart = ds;
        this.demouldEnd = de;

    }

    // Accessors
    public ArrayList<Job> jobs() {return this.jobs;}
    public ArrayList<TopBatch> b0s() {return this.b0s;}
    public Tool bottomTool() {return this.bottomTool;}
    public AutoBatch b2() {return this.b2;}
    public int size() {return this.size;}
    public int prepStart() {return this.prepStart;}
    public int prepEnd() {return this.prepEnd;}
    public int layupStart() {return this.layupStart;}
    public int layupEnd() {return this.layupEnd;}
    public int demouldStart() {return this.demouldStart;}
    public int demouldEnd() {return this.demouldEnd;}
    public Machine prepMachine() {return this.prepMachine;}
    public Machine layupMachine() {return this.layupMachine;}
    public Machine demouldMachine() {return this.demouldMachine;}
    public Labour prepLabour() {return this.prepLabour;}
    public Labour layupLabour() {return this.layupLabour;}
    public Labour demouldLabour() {return this.demouldLabour;}
    public int prepQty() {return this.prepQty;}
    public int layupQty() {return this.layupQty;}
    public int demouldQty() {return this.demouldQty;}
    public int rspOrder() {return this.rspOrder;}

}
