package models.basic_mip_pack;
import common.model_helpers.*;
import java.util.*;
import java.io.*;
import ilog.cplex.*;
import ilog.concert.*;

public class BasicMIPPack {

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
                ToolCombo newcombo = new ToolCombo(toptemp, bottomtemp, i);
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
            IloCplex cplex = new IloCplex();

            // Define decision variables
            // x_{j, c}
            IloIntVar[][] jobtocombo = new IloIntVar[remainingjobs.size()][comboobjs.size()];
            for (int i = 0; i < remainingjobs.size(); i++) {
                for (int j = 0; j < comboobjs.size(); j++) {
                    jobtocombo[i][j] = cplex.intVar(0, 1);
                }
            }

            // \lambda_c
            IloIntVar[] lambda = new IloIntVar[comboobjs.size()];
            for (int i = 0; i < comboobjs.size(); i++) {
                lambda[i] = cplex.intVar(0, remainingjobs.size());
            }

            // Create constraints
            // Each job is assigned to one combo
            for (int i = 0; i < remainingjobs.size(); i++) {
                IloLinearIntExpr temp = cplex.linearIntExpr();
                for (int j = 0; j < comboobjs.size(); j++) {
                    temp.addTerm(jobtocombo[i][j], 1);
                }
                cplex.addEq(temp, 1);
            }

            // Restrict job to combo assignments
            for (int i = 0; i < remainingjobs.size(); i++) {
                for (int j = 0; j < comboobjs.size(); j++) {
                    if (!remainingjobs.get(i).mappedCombos().contains(comboobjs.get(j))) {
                        cplex.addEq(jobtocombo[i][j], 0);
                    }
                }
            }

            // Enforce the total number of jobs to combo to be between min and max
            for (int i = 0; i < comboobjs.size(); i++) {
                IloLinearIntExpr temp = cplex.linearIntExpr();
                for (int j = 0; j < remainingjobs.size(); j++) {
                    temp.addTerm(jobtocombo[j][i], 1);
                }
                IloLinearIntExpr max = cplex.linearIntExpr();
                max.addTerm(comboobjs.get(i).maxTop() * comboobjs.get(i).maxJobPerTop(), lambda[i]);
                cplex.addLe(temp, max);
            }

            // Objective function
            IloLinearIntExpr obj = cplex.linearIntExpr();
            for (int i = 0; i < comboobjs.size(); i++) {
                obj.addTerm(lambda[i], comboobjs.get(i).bottom().size());
            }
            cplex.addMinimize(obj);

            // Tool packing modeler parameters
            cplex.setParam(IloCplex.Param.Threads, 1);
            cplex.setParam(IloCplex.IntParam.MIP.Display, 1);
            cplex.setParam(IloCplex.DoubleParam.TimeLimit, 1800);

            // Solve tool packing
            double elapsedTime = 0;
            if (cplex.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime += cplex.getCplexTime();
                writer.write("Tool packing found after time " + Math.round(elapsedTime) + " with objective value of " + cplex.getObjValue() + "\n");

                // Print to output
                System.out.println("Objective value: " + cplex.getObjValue());
                for (int i = 0; i < remainingjobs.size(); i++) {
                    for (int j = 0; j < comboobjs.size(); j++) {
                        if (cplex.getValue(jobtocombo[i][j]) == 1) {
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
                    if (cplex.getValue(jobtocombo[k][i]) == 1) {
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
            cplex.end();

            // Create autoclave packing modeler
            IloCplex autocplex = new IloCplex();

            // Define decision variables
            // x_{k, i}
            IloIntVar[][] b1tob2 = new IloIntVar[b1objs.size()][b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                for (int j = 0; j < b1objs.size(); j++) {
                    b1tob2[i][j] = autocplex.intVar(0, 1);
                }
            }

            // vol_i
            IloIntVar[] b2vol = new IloIntVar[b1objs.size()];
            int maxcap = Arrays.stream(data.autocapperjob).max().getAsInt();
            for (int i = 0; i < b1objs.size(); i++) {
                b2vol[i] = autocplex.intVar(0, maxcap);
            }

            // a
            IloIntVar numbins = autocplex.intVar(0, b1objs.size());

            // Create constraints
            // Each tool batch is assigned to one autoclave batch
            for (int i = 0; i < b1objs.size(); i++) {
                IloLinearIntExpr temp = autocplex.linearIntExpr();
                for (int j = 0; j < b1objs.size(); j++) {
                    temp.addTerm(1, b1tob2[i][j]);
                }
                autocplex.addEq(temp, 1);
            }

            // Calculate number of bins
            for (int i = 0; i < b1objs.size(); i++) {
                IloLinearIntExpr temp = autocplex.linearIntExpr();
                for (int j = 0; j < b1objs.size(); j++) {
                    temp.addTerm(j, b1tob2[i][j]);
                }
                autocplex.addGe(numbins, temp);
            }

            // Capacity constraint of autoclave batches
            for (int j = 0; j < b1objs.size(); j++) {
                IloLinearIntExpr temp = autocplex.linearIntExpr();
                for (int i = 0; i < b1objs.size(); i++) {
                    temp.addTerm(b1tob2[i][j], b1objs.get(i).size());
                }
                autocplex.addEq(temp, b2vol[j]);
            }

            // Only tool batches with the same cycle can be batched into the same autoclave batch
            for (int i = 0; i < b1objs.size(); i++) {
                for (int j = 0; j < b1objs.size(); j++) {
                    if (b1objs.get(i).jobs().get(0).autoCap() == b1objs.get(j).jobs().get(0).autoCap()) {
                        IloLinearIntExpr left = autocplex.linearIntExpr();
                        IloLinearIntExpr right = autocplex.linearIntExpr();
                        for (int k = 0; k < b1objs.size(); k++) {
                            left.addTerm(b1tob2[i][k], b1objs.get(i).jobs().get(0).autoCap());
                            right.addTerm(b1tob2[j][k], b1objs.get(j).jobs().get(0).autoCap());
                        }
                        autocplex.addEq(left, right);
                    }
                }
            }

            // Only the present quantity of tools can be in the same autoclave batch
            ArrayList<Tool> completedtools = new ArrayList<>();
            for (int i = 0; i < b1objs.size(); i++) {
                if (!completedtools.contains(b1objs.get(i).bottomTool())) {
                    for (int j = 0; j < b1objs.size(); j++) {
                        IloLinearIntExpr temp = autocplex.linearIntExpr();
                        for (int k = 0; k < b1objs.size(); k++) {
                            if (b1objs.get(k).bottomTool().equals(b1objs.get(i).bottomTool())) {
                                temp.addTerm(b1tob2[k][j], 1);
                            }
                        }
                        autocplex.addLe(temp, b1objs.get(i).bottomTool().qty());
                    }
                    completedtools.add(b1objs.get(i).bottomTool());
                }
            }

            // Calculate spread of rsp orders in autoclave batches
            IloIntVar[] intervalStarts = new IloIntVar[b1objs.size()];
            IloIntVar[] intervalEnds = new IloIntVar[b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                intervalStarts[i] = autocplex.intVar(0, data.horizon);
                intervalEnds[i] = autocplex.intVar(0, data.horizon);
            }
            for (int i = 0; i < b1objs.size(); i++) {
                for (int j = 0; j < b1objs.size(); j++) {
                    IloLinearIntExpr temp = autocplex.linearIntExpr();
                    temp.addTerm(intervalStarts[j], 1);
                    temp.addTerm(data.horizon, b1tob2[i][j]);
                    autocplex.addLe(temp, b1objs.get(i).rspOrder() + data.horizon);
                    IloLinearIntExpr temp1 = autocplex.linearIntExpr();
                    temp1.addTerm(intervalEnds[j], 1);
                    temp1.addTerm(-data.horizon, b1tob2[i][j]);
                    autocplex.addGe(temp1, b1objs.get(i).rspOrder() - data.horizon);
                }
            }

            // Objective function
            IloNumExpr spread = autocplex.numExpr();
            for (int i = 0; i < b1objs.size(); i++) {
                spread = autocplex.sum(spread, autocplex.diff(intervalEnds[i], intervalStarts[i]));
            }
            autocplex.add(autocplex.minimize(autocplex.staticLex(new IloNumExpr[]{numbins, spread})));

            // Autoclave packing modeler parameters
            autocplex.setParam(IloCplex.Param.Threads, 1);
            autocplex.setParam(IloCplex.IntParam.MIP.Display, 1);
            autocplex.setParam(IloCplex.DoubleParam.TimeLimit, 1800);

            // Solve autoclave packing
            if (autocplex.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime += autocplex.getCplexTime();
                writer.write("Autoclave packing found after time " + Math.round(elapsedTime) + " with objective value of " + autocplex.getObjValue() + "\n");

                // Print to output
                System.out.println("Objective value: " + autocplex.getObjValue());
                for (int i = 0; i < b1objs.size(); i++) {
                    for (int j = 0; j < b1objs.size(); j++) {
                        if (autocplex.getValue(b1tob2[i][j]) == 1) {
                            System.out.println("Tool batch " + i + " has volume " + b1objs.get(i).size() + " and is packed into autoclave batch " + j);
                        }
                    }
                }
                for (int i = 0; i < autocplex.getValue(numbins) + 1; i++) {
                    System.out.println("Autoclave batch " + i + " has volume " + autocplex.getValue(b2vol[i]));
                }

            } else {
                writer.write("Autoclave packing was not completed\n");
                throw new Exception("Autoclave packing was not completed");
            }

            // Obtain solution values
            ArrayList<AutoBatch> b2objs = new ArrayList<>();
            for (int i = 0; i < autocplex.getValue(numbins) + 1; i++) {
                AutoBatch newb2 = new AutoBatch(0);
                for (int j = 0; j < b1objs.size(); j++) {
                    if (autocplex.getValue(b1tob2[j][i]) == 1) {
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
            SolutionPack soln = new SolutionPack(data.numjob, autocplex.getObjValue(), elapsedTime, autocplex.getStatus().toString());
            soln.addAutoBatch(b2objs);

            // Write solution to file
            data.writePack(soln);
            data.writeSum(soln);

            // Close modeler and intermediate file
            autocplex.end();
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
