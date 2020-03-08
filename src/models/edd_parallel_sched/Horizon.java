package models.edd_parallel_sched;

public class Horizon {

    // Private class variables
    private int horizonEnd;
    private int[] qtyPerPeriod;
    private int[] periodStart;
    private int[] periodEnd;

    // Tool constructor
    public Horizon(int end, int qty) {

        this.horizonEnd = end;
        int numperiods = Math.floorDiv(end, 5);
        this.qtyPerPeriod = new int[numperiods];
        this.periodStart = new int[numperiods];
        this.periodEnd = new int[numperiods];
        for (int i = 0; i < numperiods; i++) {
            this.qtyPerPeriod[i] = qty;
            this.periodStart[i] = i * 5;
            this.periodEnd[i] = i * 5 + 5;
        }

    }

    // Machine and labour team constructor
    public Horizon(int end, int[] qtyPerShift, int[] shiftStart, int[] shiftEnd) {

        this.horizonEnd = end;
        int numperiods = Math.floorDiv(end, 5);
        this.qtyPerPeriod = new int[numperiods];
        this.periodStart = new int[numperiods];
        this.periodEnd = new int[numperiods];
        int time = 0;
        for (int i = 0; i < numperiods; i++) {
            for (int j = 0; j < shiftStart.length; j++) {
                if (shiftStart[j] <= time && time < shiftEnd[j]) {
                    qtyPerPeriod[i] = qtyPerShift[j];
                    time += 5;
                    if (time == shiftEnd[shiftEnd.length - 1]) {
                        time = 0;
                    }
                    break;
                }
            }
        }

    }

    // Check if horizon is free in a certain time for a given qty
    public boolean check(int start, int end, int qty) {

        for (int i = 0; i < this.periodStart.length; i++) {
            if (this.periodStart[i] <= start) {
                for (int j = i; j < this.periodStart.length; j++) {
                    if (this.qtyPerPeriod[j] < qty) {
                        return false;
                    }
                    if (this.periodStart[j] <= end && end < this.periodEnd[j]) {
                        break;
                    }
                }
                break;
            }
        }
        return true;

    }

    // Schedule batch
    public void schedule(int start, int end, int qty) {

        for (int i = 0; i < this.periodStart.length; i++) {
            if (this.periodStart[i] <= start) {
                for (int j = i; j < this.periodStart.length; j++) {
                    this.qtyPerPeriod[j] -= qty;
                    if (this.periodStart[j] <= end && end < this.periodEnd[j]) {
                        break;
                    }
                }
                break;
            }
        }

    }

    // Accessors
    public int horizonEnd() {return this.horizonEnd;}
    public int[] qtyPerPeriod() {return this.qtyPerPeriod;}
    public int[] periodStart() {return this.periodStart;}
    public int[] periodEnd() {return this.periodEnd;}

}
