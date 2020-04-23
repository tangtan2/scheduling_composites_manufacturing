package common.model_helpers;

public class ToolCombo {

    // Private class variables
    private Tool top;
    private Tool bottom;
    private int size;
    private int qty;
    private int index;
    private int maxTop;
    private int maxJobPerTop;

    // Constructor
    public ToolCombo(Tool top, Tool bottom, int index) {

        this.top = top;
        this.bottom = bottom;
        this.index = index;
        this.maxTop = bottom.capacity();
        this.maxJobPerTop = top.capacity();
        this.size = bottom.size();
        this.qty = bottom.qty();

    }

    // Accessors
    public Tool top() {return this.top;}
    public Tool bottom() {return this.bottom;}
    public int index() {return this.index;}
    public int maxTop() {return this.maxTop;}
    public int maxJobPerTop() {return this.maxJobPerTop;}
    public int size() {return this.size;}
    public int qty() {return this.qty;}

}
