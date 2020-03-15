package models.cluster_pack;
import common.model_helpers.Tool;
import java.util.*;

public class Cluster {

    // Private class variables
    private int centroid;
    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Point> VFI = new ArrayList<>();

    // Constructor
    public Cluster(int centroid) {

        this.centroid = centroid;

    }

    // Add point to cluster
    public void addPoint(Point p) {

        this.points.add(p);

    }

    // Recompute centroid for k means
    public int recompute() {

        return Math.round(points.stream().map(Point::location).reduce(0, Integer::sum) / (float) points().size());

    }

    // Calculate FI and VFI points
    public void calcVFI(int autoCap) {

        // Reset points
        ArrayList<Point> FI = new ArrayList<>();
        this.VFI = new ArrayList<>();

        // Get list of relevant bottom tools
        ArrayList<Tool> tools = new ArrayList<>();
        for (Point p : this.points) {
            if (!tools.contains(p.b1().bottomTool())) {
                tools.add(p.b1().bottomTool());
            }
        }

        // Get list of FI points
        this.points.sort(Comparator.comparing(x -> - Math.abs(x.location() - this.centroid)));
        for (Tool b : tools) {
            ArrayList<Point> pointsInTool = new ArrayList<>();
            for (Point p : this.points) {
                if (p.b1().bottomTool().equals(b)) {
                    pointsInTool.add(p);
                }
            }
            while (pointsInTool.size() > b.qty()) {
                FI.add(pointsInTool.get(0));
                pointsInTool.remove(0);
            }
        }
        int size = 0;
        for (Point p : this.points) {
            size += p.b1().size();
        }
        if (size > autoCap) {
            for (Point p : this.points) {
                if (!FI.contains(p)) {
                    FI.add(p);
                }
                size -= p.b1().size();
                if (size <= autoCap) {
                    break;
                }
            }
        }

        // Get list of VFI points
        for (Point p : FI) {
            int numerator = 0;
            for (Point p1 : this.points) {
                if (!FI.contains(p1)) {
                    numerator += p1.location();
                }
            }
            int denominator = this.points.size() - FI.size();
            int loc = 2 * (numerator / denominator) - p.location();
            Point newP = new Point(0, null, 0, loc);
            this.VFI.add(newP);
        }

    }

    // Recompute centroid for k means S
    public int recomputeKMS() {

        int sumPoints = this.points.stream().map(Point::location).reduce(Integer::sum).orElseThrow();
        int VFIpenalty = this.VFI.stream().map(Point::location).reduce(Integer::sum).orElseThrow();
        return ((sumPoints + VFIpenalty) / (this.points.size() + this.VFI.size()));

    }

    // Reset cluster to new centroid
    public void reset(int newC) {

        this.centroid = newC;
        this.points = new ArrayList<>();
        this.VFI = new ArrayList<>();

    }

    // Static helper method to check which cluster point p is closest to
    public static Cluster checkCluster(ArrayList<Cluster> clusters, Point p) {

        int diff = (int) Double.POSITIVE_INFINITY;
        Cluster best = null;
        for (Cluster c : clusters) {
            int countTool = 0;
            for (Point p1 : c.points()) {
                if (p1.b1().bottomTool().equals(p.b1().bottomTool())) {
                    countTool++;
                }
            }
            if (countTool < p.b1().bottomTool().qty() && Math.abs(p.location() - c.centroid()) < diff) {
                diff = Math.abs(p.location() - c.centroid());
                best = c;
            }
        }
        return best;

    }

    // Static helper method to check if clusters have converged for k means
    public static boolean checkConverge(ArrayList<Cluster> clusters) {

        boolean flag = true;
        for (Cluster c : clusters) {
            if (c.recompute() != c.centroid()) {
                flag = false;
                break;
            }
        }
        return flag;

    }

    // Static helper method to check if clusters have converged for k means S
    public static boolean checkConvergeKMS(ArrayList<Cluster> clusters) {

        boolean flag = true;
        for (Cluster c : clusters) {
            if (!c.points().isEmpty() && !c.VFI().isEmpty()) {
                if (c.recomputeKMS() != c.centroid()) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;

    }

    // Static helper method to find closest point
    public static Point findPoint(Collection<Point> points, Cluster c) {

        int diff = (int) Double.POSITIVE_INFINITY;
        Point best = null;
        for (Point p : points) {
            if (Math.abs(p.location() - c.centroid()) < diff) {
                diff = Math.abs(p.location() - c.centroid());
                best = p;
            }
        }
        return best;

    }

    // Accessors
    public int centroid() {return this.centroid;}
    public ArrayList<Point> points() {return this.points;}
    public ArrayList<Point> VFI() {return this.VFI;}

}
