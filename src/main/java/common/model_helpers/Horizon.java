package common.model_helpers;
import java.util.*;

public class Horizon {

    // Private class variables
    private final int horizonEnd;
    private final HashMap<Integer, Integer> qtyPerPeriod = new HashMap<>();

    // Tool constructor
    public Horizon(int end, int qty) {

        this.horizonEnd = end;
        int numperiods = Math.floorDiv(end, 5);
        for (int i = 0; i < numperiods; i++) {
            this.qtyPerPeriod.put(i * 5, qty);
        }

    }

    // Machine and labour team constructor
    public Horizon(int end, int[] qtyPerShift, int[] shiftStart, int[] shiftEnd) {

        this.horizonEnd = end;
        int numperiods = Math.floorDiv(end, 5);
        int time = 0;
        for (int i = 0; i < numperiods; i++) {
            for (int j = 0; j < qtyPerShift.length; j++) {
                if (time >= shiftStart[j] && (time + 4) < shiftEnd[j]) {
                    this.qtyPerPeriod.put(i * 5, qtyPerShift[j]);
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

        for (int i = 0; i < this.horizonEnd; i += 5) {
            if (this.qtyPerPeriod.get(i) > 0) {
                return i;
            }
        }
        return horizonEnd;

    }
    public int earliest(int qty) {

        for (int i = 0; i < this.horizonEnd; i += 5) {
            if (this.qtyPerPeriod.get(i) > qty) {
                return i;
            }
        }
        return horizonEnd;

    }

    // Check if horizon is free in a certain time for a given qty
    public boolean check(int start, int end, int qty) {

        for (int i = start; i < end; i += 5) {
            if (this.qtyPerPeriod.get(i) < qty) {
                return false;
            }
        }
        return true;

    }

    // Schedule batch
    public void schedule(int start, int end, int qty) {

        for (int i = start; i < end; i += 5) {
            this.qtyPerPeriod.replace(i, this.qtyPerPeriod.get(i) - qty);
        }

    }

    // Update horizon to start at given time
    public void updateHorizonStart(int start, int qty) {

        for (int i = start; i >= 0; i -= 5) {
            this.qtyPerPeriod.replace(i, qty);
        }

    }

    // Accessors
    public int horizonEnd() {return this.horizonEnd;}

}
