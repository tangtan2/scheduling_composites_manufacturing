package models.cluster_pack;
import common.model_helpers.*;
import java.util.*;
import java.io.*;
import ilog.cp.*;
import ilog.concert.*;

public class ClusterPack {

    public static void main(String[] args) {

        // Get settings from json file
        String filepath = args[0];
        String sumfile = args[1];
        String interfile = args[2];
        int pi = Integer.parseInt(args[3]);

        // Make data object and import raw data
        Data data = new Data(filepath, sumfile, pi);
        data.readInstanceParams();

        // Create objects
        ArrayList<Tool> toptoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numtop; i++) {
            Tool newtool = new Tool(data.topname[i], data.topcap[i], data.topqty[i], data.topmin[i]);
            toptoolobjs.add(newtool);
        }
        ArrayList<Tool> bottomtoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numbottom; i++) {
            Tool newtool = new Tool(data.bottomname[i], data.bottomcap[i], data.bottomqty[i], data.bottommin[i], data.bottomsize[i]);
            bottomtoolobjs.add(newtool);
        }
        ArrayList<ToolCombo> comboobjs = new ArrayList<>();
        int it = 0;
        for (int i = 0; i < data.numjobtotool; i++) {
            int ind = 0;
            for (ToolCombo c : comboobjs) {
                if (c.top().name().equals(data.jobtotools[i][1]) && c.bottom().name().equals(data.jobtotools[i][2])) {
                    ind = 1;
                    break;
                }
            }
            if (ind == 0) {
                Tool toptemp = null;
                Tool bottomtemp = null;
                for (Tool t : toptoolobjs) {
                    if (t.name().equals(data.jobtotools[i][1])) {
                        toptemp = t;
                        break;
                    }
                }
                for (Tool b : bottomtoolobjs) {
                    if (b.name().equals(data.jobtotools[i][2])) {
                        bottomtemp = b;
                        break;
                    }
                }
                assert toptemp != null;
                assert bottomtemp != null;
                ToolCombo newcombo = new ToolCombo(toptemp, bottomtemp, it++);
                comboobjs.add(newcombo);
            }
        }
        ArrayList<Job> jobobjs = new ArrayList<>();
        for (int i = 0; i < data.numjob; i++) {
            Job newjob = new Job(data.jobname[i], data.jobpartfamily[i], data.autocapperjob[i], i, data.sj[i], data.dj[i], data.jobsteps[i], data.jobsteptimes[i]);
            newjob.setRSP(data.rspjoborder[i]);
            for (int j = 0; j < data.numjobtotool; j++) {
                if (data.jobtotools[j][0].equals(newjob.name())) {
                    for (ToolCombo c : comboobjs) {
                        if (c.top().name().equals(data.jobtotools[j][1]) && c.bottom().name().equals(data.jobtotools[j][2])) {
                            newjob.addToolCombo(c);
                            break;
                        }
                    }
                }
            }
            jobobjs.add(newjob);
        }

        try {

            // Create file to write iterative solutions to
            FileWriter writer = new FileWriter(interfile, true);
            String initialtext = "Problem Instance: " + pi + "\n";
            writer.write(initialtext);

            // Make batches with single job to tool mappings
            ArrayList<ToolBatch> b1objs = new ArrayList<>();
            ArrayList<Job> remainingjobs = new ArrayList<>();
            for (Job j : jobobjs) {
                if (j.mappedCombos().size() == 1 &&
                        j.mappedCombos().get(0).maxTop() == 1 &&
                        j.mappedCombos().get(0).maxJobPerTop() == 1) {
                    TopBatch newtop = new TopBatch(j.mappedCombos().get(0).top());
                    newtop.addJob(j);
                    ToolBatch newb1 = new ToolBatch(j.mappedCombos().get(0).bottom());
                    newb1.addTopBatch(newtop);
                    newtop.setToolBatch(newb1);
                    j.setTopBatch(newtop);
                    newb1.calcRSP();
                    b1objs.add(newb1);
                    System.out.println("Job " + j.index() + " is assigned to a single tool batch");
                } else {
                    remainingjobs.add(j);
                }
            }

            // Create tool packing modeler
            IloCP cp = new IloCP();

            // Define decision variables
            // x_j
            IloIntVar[] jobtocombo = new IloIntVar[remainingjobs.size()];
            for (int i = 0; i < remainingjobs.size(); i++) {
                jobtocombo[i] = cp.intVar(0, comboobjs.size());
            }

            // \lambda_c
            IloIntVar[] lambda = new IloIntVar[comboobjs.size()];
            for (int i = 0; i < comboobjs.size(); i++) {
                lambda[i] = cp.intVar(0, remainingjobs.size());
            }

            // Create constraints
            // Allowed assignments of jobs to combos
            for (int i = 0; i < remainingjobs.size(); i++) {
                int[] allowed = new int[remainingjobs.get(i).mappedCombos().size()];
                for (int j = 0; j < remainingjobs.get(i).mappedCombos().size(); j++) {
                    allowed[j] = remainingjobs.get(i).mappedCombos().get(j).index();
                }
                cp.add(cp.allowedAssignments(jobtocombo[i], allowed));
            }

            // Number of jobs in combo is less than max number of spots
            for (int i = 0; i < comboobjs.size(); i++) {
                cp.add(cp.le(cp.count(jobtocombo, i), cp.prod(lambda[i], comboobjs.get(i).maxJobPerTop() * comboobjs.get(i).maxTop())));
            }

            // Objective function
            IloLinearIntExpr obj = cp.linearIntExpr();
            for (int i = 0; i < comboobjs.size(); i++) {
                obj.addTerm(lambda[i], comboobjs.get(i).bottom().size());
            }
            cp.addMinimize(obj);

            // Tool packing modeler parameters
            cp.setParameter(IloCP.IntParam.Workers, 1);
            cp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Terse);
            cp.setParameter(IloCP.DoubleParam.TimeLimit, 10);

            // Solve tool packing
            double elapsedTime = 0;
            if (cp.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime += cp.getInfo(IloCP.DoubleInfo.SolveTime);
                writer.write("Tool packing found after time " + Math.round(elapsedTime) + " with objective value of " + cp.getObjValue() + "\n");

                // Print to output
                System.out.println("Objective value: " + cp.getObjValue());
                for (int i = 0; i < remainingjobs.size(); i++) {
                    for (int j = 0; j < comboobjs.size(); j++) {
                        if (cp.getValue(jobtocombo[i]) == j) {
                            System.out.println("Job " + remainingjobs.get(i).index() + " is assigned to combo " + j + " with top tool " + comboobjs.get(j).top().name() + " and bottom tool " + comboobjs.get(j).bottom().name());
                        }
                    }
                }

            } else {

                // Tool packing not found
                writer.write("Tool packing was not completed\n");
                throw new Exception("Tool packing was not completed");

            }

            // Obtain solution values
            for (int i = 0; i < comboobjs.size(); i++) {
                ArrayList<Job> jobs = new ArrayList<>();
                for (int k = 0; k < remainingjobs.size(); k++) {
                    if (cp.getValue(jobtocombo[k]) == i) {
                        jobs.add(remainingjobs.get(k));
                    }
                }
                if (jobs.size() > 0) {
                    jobs.sort(Comparator.comparing(Job::rspOrder));
                    while (!jobs.isEmpty()) {
                        int maxnum = comboobjs.get(i).maxTop() * comboobjs.get(i).maxJobPerTop();
                        ToolBatch newb1 = new ToolBatch(comboobjs.get(i).bottom());
                        if (jobs.size() > maxnum) {
                            for (int j = 0; j < comboobjs.get(i).maxTop(); j++) {
                                TopBatch newtop = new TopBatch(comboobjs.get(i).top());
                                for (int k = 0; k < comboobjs.get(i).maxJobPerTop(); k++) {
                                    newtop.addJob(jobs.get(0));
                                    jobs.get(0).setTopBatch(newtop);
                                    jobs.remove(0);
                                }
                                newb1.addTopBatch(newtop);
                                newtop.setToolBatch(newb1);
                            }
                        } else {
                            while (!jobs.isEmpty()) {
                                TopBatch newtop = new TopBatch(comboobjs.get(i).top());
                                for (int k = 0; k < comboobjs.get(i).maxJobPerTop(); k++) {
                                    if (!jobs.isEmpty()) {
                                        newtop.addJob(jobs.get(0));
                                        jobs.get(0).setTopBatch(newtop);
                                        jobs.remove(0);
                                    } else {
                                        break;
                                    }
                                }
                                newb1.addTopBatch(newtop);
                                newtop.setToolBatch(newb1);
                            }
                        }
                        newb1.calcRSP();
                        b1objs.add(newb1);
                    }
                }
            }

            // Cluster point definitions
            HashMap<Integer, Point> pointMap = new HashMap<>();
            for (ToolBatch t : b1objs) {
                pointMap.put(b1objs.indexOf(t), new Point(b1objs.indexOf(t), t, t.size(), t.rspOrder()));
            }

            // Collect different autoclave cycle types
            ArrayList<Integer> autoCapList = new ArrayList<>();
            for (Job j : jobobjs) {
                if (!autoCapList.contains(j.autoCap())) {
                    autoCapList.add(j.autoCap());
                }
            }

            // Iterate through each autoclave cycle type
            double start = System.nanoTime();
            ArrayList<AutoBatch> b2objs = new ArrayList<>();
            for (Integer i : autoCapList) {

                // Collect clusters pertaining to current autoclave cycle type
                HashMap<Integer, Point> pointMapPerAuto = new HashMap<>();
                ArrayList<Tool> toolsPerAuto = new ArrayList<>();
                for (Map.Entry<Integer, Point> point : pointMap.entrySet()) {
                    if (point.getValue().b1().jobs().get(0).autoCap() == i) {
                        pointMapPerAuto.put(point.getKey(), point.getValue());
                        if (!toolsPerAuto.contains(point.getValue().b1().bottomTool())) {
                            toolsPerAuto.add(point.getValue().b1().bottomTool());
                        }
                    }
                }

                // Calculate lower bound on number of autoclave batches
                int k = 0;
                for (Tool b : toolsPerAuto) {
                    int temp = 0;
                    for (Point c : pointMapPerAuto.values()) {
                        if (c.b1().bottomTool().equals(b)) {
                            temp++;
                        }
                    }
                    int numneeded = (int) Math.ceil((float) temp / b.qty());
                    if (numneeded > k) {
                        k = numneeded;
                    }
                }

                // PCS algorithm iterate until find minimum k value
                while (true) {

                    // Parameters
                    boolean feasible = true;
                    int maxiter = 400;

                    // Initialize cluster centroids
                    ArrayList<Cluster> clusters = new ArrayList<>();
                    int maxC = 0;
                    for (Point p : pointMapPerAuto.values()) {
                        if (p.location() > maxC) {
                            maxC = p.location();
                        }
                    }
                    for (int j = 0; j < k; j++) {
                        clusters.add(new Cluster(Math.max(1, (int) Math.round(Math.random() * maxC))));
                    }

                    // Perform k means to obtain initial solution
                    int iter = 0;
                    while (iter <= maxiter) {
                        Collection<Point> iterate = new ArrayList<>(pointMapPerAuto.values());
                        for (Cluster c : clusters) {
                            Point bestPoint = Cluster.findPoint(iterate, c);
                            c.addPoint(bestPoint);
                            iterate.remove(bestPoint);
                        }
                        for (Point p : iterate) {
                            Cluster.checkCluster(clusters, p).addPoint(p);
                        }
                        if (Cluster.checkConverge(clusters)) {
                            for (Cluster c : clusters) {
                                c.reset(c.centroid());
                            }
                            break;
                        } else {
                            for (Cluster c : clusters) {
                                if (c.points().isEmpty()) {
                                    c.reset(Math.max(1, (int) Math.round(Math.random() * maxC)));
                                } else {
                                    int newC = c.recompute();
                                    c.reset(newC);
                                }
                            }
                            iter++;
                        }
                    }

                    // Perform k means S to obtain actual solution
                    iter = 0;
                    while (iter <= maxiter) {
                        Collection<Point> iterate = new ArrayList<>(pointMapPerAuto.values());
                        for (Cluster c : clusters) {
                            Point bestPoint = Cluster.findPoint(iterate, c);
                            c.addPoint(bestPoint);
                            iterate.remove(bestPoint);
                        }
                        for (Point p : iterate) {
                            Cluster.checkCluster(clusters, p).addPoint(p);
                        }
                        for (Cluster c : clusters) {
                            c.calcVFI(i);
                        }
                        if (Cluster.checkConvergeKMS(clusters)) {
                            break;
                        } else {
                            iter++;
                            if (iter > maxiter) {
                                for (Cluster c : clusters) {
                                    if (c.points().stream().map(Point::size).reduce(Integer::sum).orElseThrow() > i) {
                                        feasible = false;
                                    }
                                    for (Point p : c.points()) {
                                        int temp = 0;
                                        for (Point p1 : c.points()) {
                                            if (p.b1().bottomTool().equals(p1.b1().bottomTool())) {
                                                temp++;
                                            }
                                        }
                                        if (temp > p.b1().bottomTool().qty()) {
                                            feasible = false;
                                        }
                                    }
                                }
                                break;
                            }
                            for (Cluster c : clusters) {
                                if (c.points().isEmpty() || c.VFI().isEmpty()) {
                                    c.reset(Math.max(1, (int) Math.round(Math.random() * maxC)));
                                } else {
                                    int newC = c.recomputeKMS();
                                    c.reset(newC);
                                }
                            }
                        }
                    }

                    // If feasible solution is found
                    if (feasible) {

                        // Obtain solution values
                        for (Cluster c : clusters) {
                            AutoBatch newauto = new AutoBatch(i);
                            for (Point p : c.points()) {
                                newauto.addToolBatch(p.b1());
                                p.b1().setAutoBatch(newauto);
                            }
                            b2objs.add(newauto);
                        }

                        // Break out of loop
                        break;

                    } else {

                        // Increase k and try again
                        k++;

                    }

                }

            }

            // Add indices
            it = 0;
            for (int i = 0; i < b1objs.size(); i++) {
                b1objs.get(i).addIndex(i);
                for (TopBatch t : b1objs.get(i).b0s()) {
                    t.addIndex(it++);
                }
            }
            for (int i = 0; i < b2objs.size(); i++) {
                b2objs.get(i).addIndex(i);
            }

            // Make solution object
            elapsedTime += (System.nanoTime() - start) / 1_000_000_000;
            SolutionPack soln = new SolutionPack(data.numjob, b2objs.size(), elapsedTime, "Feasible");
            soln.addAutoBatch(b2objs);

            // Write solution to file
            data.writePack(soln);
            data.writeSum(soln);

            // Close tool packing modeler and intermediate file
            cp.end();
            writer.close();

        } catch (Exception e) {

            try {

                FileWriter writer = new FileWriter(interfile, true);
                writer.write("Error: " + e + "\n");
                writer.close();

            } catch (IOException e1) {

                e1.printStackTrace();

            }

        }

    }

}