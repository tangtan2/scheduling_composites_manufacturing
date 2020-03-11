package common.model_helpers;

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
            periodStart[i] = i * 5;
            periodEnd[i] = i * 5 + 5;
            for (int j = 0; j < qtyPerShift.length; j++) {
                if (time >= shiftStart[j] && (time + 4) < shiftEnd[j]) {
                    qtyPerPeriod[i] = qtyPerShift[j];
                    break;
                }
            }
            time += 5;
            if (time == 7 * 24 * 60) {
                time = 0;
            }
        }

    }

    // Get earliest availability
    public int earliest() {

        for (int i = 0; i < this.periodStart.length; i++) {
            if (qtyPerPeriod[i] > 0) {
                return i * 5;
            }
        }
        return horizonEnd;

    }

    // Check if horizon is free in a certain time for a given qty
    public boolean check(int start, int end, int qty) {

        for (int i = 0; i < this.periodStart.length; i++) {
            if (this.periodStart[i] == start) {
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
            if (this.periodStart[i] == start) {
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
