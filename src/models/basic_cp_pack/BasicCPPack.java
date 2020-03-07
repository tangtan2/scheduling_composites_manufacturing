package models.basic_cp_pack;
import common.model_helpers.*;
import java.util.*;
import java.io.*;
import ilog.cp.*;
import ilog.concert.*;

public class BasicCPPack {

    public static void main(String[] args) {

        // Get settings from json file
        // TBD: Parse JSON information
        String filepath = "";
        String sumfile = "";
        String interfile = "";
        int pi = 0;

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
            cp.setParameter(IloCP.DoubleParam.TimeLimit, 1800);

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
                            System.out.println("Job " + remainingjobs.get(i).index() + " is assigned to combo " + j);
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

            // Close tool packing modeler
            cp.end();

            // Create autoclave packing modeler
            IloCP autocp = new IloCP();

            // Define decision variables
            // x_k
            IloIntVar[] b1pack = new IloIntVar[b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                b1pack[i] = autocp.intVar(0, b1objs.size() - 1);
            }

            // v_i
            IloIntVar[] b2vol = new IloIntVar[b1objs.size()];
            int maxcap = Arrays.stream(data.autocapperjob).max().getAsInt();
            for (int i = 0; i < b1objs.size(); i++) {
                b2vol[i] = autocp.intVar(0, maxcap);
            }

            // a
            IloIntVar bins = autocp.intVar(0, b1objs.size());

            // Create constraints
            // Pack tool batches into autoclave batches
            int[] b1vol = new int[b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                b1vol[i] = b1objs.get(i).size();
            }
            autocp.add(autocp.pack(b2vol, b1pack, b1vol));

            // Calculate number of bins
            for (int i = 0; i < b1objs.size(); i++) {
                autocp.addGe(bins, b1pack[i]);
            }

            // Only tool batches with the same cycle can be batched into the same autoclave batch
            for (int i = 0; i < b1objs.size(); i++) {
                for (int j = 0; j < b1objs.size(); j++) {
                    if (!b1objs.get(i).jobs().get(0).steps()[2].equals(b1objs.get(j).jobs().get(0).steps()[2])) {
                        autocp.add(autocp.allDiff(new IloIntVar[]{b1pack[i], b1pack[j]}));
                    }
                }
            }

            // Only the present quantity of tools can be in the same autoclave batch
            ArrayList<Tool> completedtools = new ArrayList<>();
            for (ToolBatch b1 : b1objs) {
                if (!completedtools.contains(b1.bottomTool())) {
                    ArrayList<IloIntVar> toolvars = new ArrayList<>();
                    for (ToolBatch newb1 : b1objs) {
                        if (newb1.bottomTool().equals(b1.bottomTool())) {
                            toolvars.add(b1pack[b1objs.indexOf(newb1)]);
                        }
                    }
                    IloIntVar[] cards = new IloIntVar[b1objs.size()];
                    int[] values = new int[b1objs.size()];
                    for (int i = 0; i < b1objs.size(); i++) {
                        cards[i] = autocp.intVar(0, b1.bottomTool().qty());
                        values[i] = i;
                    }
                    IloIntVar[] vars = new IloIntVar[toolvars.size()];
                    for (int i = 0; i < toolvars.size(); i++) {
                        vars[i] = toolvars.get(i);
                    }
                    autocp.add(autocp.distribute(cards, values, vars));
                    completedtools.add(b1.bottomTool());
                }
            }

            // Calculate spread of RSP orders in autoclave batches
            IloIntVar[] intervalStarts = new IloIntVar[b1objs.size()];
            IloIntVar[] intervalEnds = new IloIntVar[b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                intervalStarts[i] = autocp.intVar(0, data.horizon);
                intervalEnds[i] = autocp.intVar(0, data.horizon);
            }
            for (int i = 0; i < b1objs.size(); i++) {
                autocp.addLe(autocp.element(intervalStarts, b1pack[i]), b1objs.get(i).rspOrder());
                autocp.addGe(autocp.element(intervalEnds, b1pack[i]), b1objs.get(i).rspOrder());
            }

            // Objective function
            IloNumExpr spread = autocp.numExpr();
            for (int i = 0; i < b1objs.size(); i++) {
                spread = autocp.sum(spread, autocp.diff(intervalEnds[i], intervalStarts[i]));
            }
            autocp.add(autocp.minimize(autocp.staticLex(bins, spread)));

            // Autoclave packing modeler parameters
            autocp.setParameter(IloCP.IntParam.Workers, 1);
            autocp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Terse);
            autocp.setParameter(IloCP.DoubleParam.TimeLimit, 1800);

            // Solve autoclave packing
            if (autocp.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime += autocp.getInfo(IloCP.DoubleInfo.SolveTime);
                writer.write("Autoclave packing found after time " + Math.round(elapsedTime) + " with objective value of " + autocp.getObjValue() + "\n");

                // Print to output
                System.out.println("Objective value: " + autocp.getObjValue());
                for (int i = 0; i < b1objs.size(); i++) {
                    System.out.println("Tool batch " + i + " has volume " + b1vol[i] + " and is packed into autoclave batch " + autocp.getValue(b1pack[i]));
                }
                for (int i = 0; i < autocp.getValue(bins) + 1; i++) {
                    System.out.println("Autoclave batch " + i + " has volume " + autocp.getValue(b2vol[i]));
                }

            } else {

                // Autoclave packing not found
                writer.write("Autoclave packing was not completed\n");
                throw new Exception("Autoclave packing was not completed");

            }

            // Obtain solution values
            ArrayList<AutoBatch> b2objs = new ArrayList<>();
            for (int i = 0; i < autocp.getValue(bins) + 1; i++) {
                AutoBatch newb2 = new AutoBatch(0);
                for (int j = 0; j < b1objs.size(); j++) {
                    if (autocp.getValue(b1pack[j]) == i) {
                        if (newb2.capacity() == 0) {
                            newb2.setCapacity(b1objs.get(j).jobs().get(0).autoCap());
                        }
                        newb2.addToolBatch(b1objs.get(j));
                        b1objs.get(j).setAutoBatch(newb2);
                        for (TopBatch top : b1objs.get(j).b0s()) {
                            top.setToolBatch(b1objs.get(j));
                            for (Job currentj : top.jobs()) {
                                currentj.setTopBatch(top);
                            }
                        }
                    }
                }
                b2objs.add(newb2);
            }

            // Make solution object
            SolutionPack soln = new SolutionPack(data.numjob, autocp.getObjValue(), elapsedTime, autocp.getStatusString());
            soln.addAutoBatch(b2objs);

            // Write solution to file
            data.writePack(soln);
            data.writeSum(soln);

            // Close modeler and intermediate file
            autocp.end();
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
