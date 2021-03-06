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
        for (int i = 0; i < data.numTopTool; i++) {
            Tool newtool = new Tool(data.topToolName[i], data.topToolCap[i], data.topToolQty[i], data.topToolMin[i]);
            toptoolobjs.add(newtool);
        }
        ArrayList<Tool> bottomtoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numBottomTool; i++) {
            Tool newtool = new Tool(data.bottomToolName[i], data.bottomToolCap[i], data.bottomToolQty[i], data.bottomToolMin[i], data.bottomToolSize[i]);
            bottomtoolobjs.add(newtool);
        }
        ArrayList<ToolCombo> comboobjs = new ArrayList<>();
        for (int i = 0; i < data.numJobToTool; i++) {
            int ind = 0;
            for (ToolCombo c : comboobjs) {
                if (c.top().name().equals(data.jobToTool[i][1]) && c.bottom().name().equals(data.jobToTool[i][2])) {
                    ind = 1;
                    break;
                }
            }
            if (ind == 0) {
                Tool toptemp = null;
                Tool bottomtemp = null;
                for (Tool t : toptoolobjs) {
                    if (t.name().equals(data.jobToTool[i][1])) {
                        toptemp = t;
                        break;
                    }
                }
                for (Tool b : bottomtoolobjs) {
                    if (b.name().equals(data.jobToTool[i][2])) {
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
        for (int i = 0; i < data.numJob; i++) {
            Job newjob = new Job(data.jobName[i], data.jobPartFamily[i], data.jobAutoCap[i], i, data.jobSize[i], data.jobDue[i], data.jobSteps[i], data.jobStepTimes[i]);
            newjob.setRSP(data.jobRSPOrder[i]);
            for (int j = 0; j < data.numJobToTool; j++) {
                if (data.jobToTool[j][0].equals(newjob.name())) {
                    for (ToolCombo c : comboobjs) {
                        if (c.top().name().equals(data.jobToTool[j][1]) && c.bottom().name().equals(data.jobToTool[j][2])) {
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
                    newb1.calcRSP();
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
            cplex.setParam(IloCplex.DoubleParam.TimeLimit, 900);

            // Solve tool packing
            double start = cplex.getCplexTime();
            double elapsedTime = 0;
            if (cplex.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime += (cplex.getCplexTime() - start);
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
            int infeasCounter = 0;
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
                        int minnum = comboobjs.get(i).bottom().min() * comboobjs.get(i).top().min();
                        int altminnum = 0;
                        int altmaxnum = 0;
                        ToolCombo altc = null;
                        for (ToolCombo c : jobs.get(0).mappedCombos()) {
                            if (!c.equals(comboobjs.get(i))) {
                                altminnum = Math.max(c.bottom().min(), Math.max(c.top().min(), c.bottom().min() * c.top().min()));
                                altmaxnum = c.maxTop() * c.maxJobPerTop();
                                altc = c;
                            }
                        }
                        ToolBatch newb1;
                        if (altc != null && jobs.size() < minnum && jobs.size() >= altminnum && jobs.size() <= altmaxnum) {
                            newb1 = new ToolBatch(altc.bottom());
                            while (!jobs.isEmpty()) {
                                TopBatch newtop = new TopBatch(altc.top());
                                for (int k = 0; k < altc.maxJobPerTop(); k++) {
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
                            if (newb1.jobs().size() < altminnum) {
                                infeasCounter++;
                            }
                        } else {
                            newb1 = new ToolBatch(comboobjs.get(i).bottom());
                            int size = jobs.size();
                            if (jobs.size() < comboobjs.get(i).maxJobPerTop() * comboobjs.get(i).maxTop()) {
                                while (!jobs.isEmpty()) {
                                    TopBatch newtop = new TopBatch(comboobjs.get(i).top());
                                    for (int k = 0; k < comboobjs.get(i).maxJobPerTop(); k++) {
                                        newtop.addJob(jobs.get(0));
                                        jobs.remove(0);
                                        if (jobs.isEmpty()) {
                                            break;
                                        }
                                    }
                                    newb1.addTopBatch(newtop);
                                    newtop.setToolBatch(newb1);
                                }
                            } else {
                                for (int j = 0; j < Math.min(size, comboobjs.get(i).maxTop()); j++) {
                                    TopBatch newtop = new TopBatch(comboobjs.get(i).top());
                                    for (int k = 0; k < comboobjs.get(i).maxJobPerTop(); k++) {
                                        newtop.addJob(jobs.get(0));
                                        jobs.remove(0);
                                    }
                                    newb1.addTopBatch(newtop);
                                    newtop.setToolBatch(newb1);
                                }
                            }
                            if (newb1.jobs().size() < minnum) {
                                infeasCounter++;
                            }
                        }
                        newb1.calcRSP();
                        b1objs.add(newb1);
                    }
                }
            }

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
            for (int i = 0; i < b1objs.size(); i++) {
                b2vol[i] = autocplex.intVar(0, b1objs.get(i).jobs().get(0).autoCap());
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

            // Only pack tool batches into bins with correct capacity
            for (int i = 0; i < b1objs.size(); i++) {
                IloLinearIntExpr temp = autocplex.linearIntExpr();
                for (int j = 0; j < b1objs.size(); j++) {
                    if (b1objs.get(i).jobs().get(0).autoCap() != b1objs.get(j).jobs().get(0).autoCap()) {
                        temp.addTerm(1, b1tob2[i][j]);
                    }
                }
                autocplex.addEq(temp, 0);
            }

            // Calculate number of bins
            IloIntVar[] indicators = new IloIntVar[b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                indicators[i] = autocplex.intVar(0, 1);
                for (int j = 0; j < b1objs.size(); j++) {
                    autocplex.addGe(indicators[i], b1tob2[j][i]);
                }
            }
            autocplex.addGe(numbins, autocplex.sum(indicators));

            // Capacity constraint of autoclave batches
            for (int j = 0; j < b1objs.size(); j++) {
                IloLinearIntExpr temp = autocplex.linearIntExpr();
                for (int i = 0; i < b1objs.size(); i++) {
                    temp.addTerm(b1tob2[i][j], b1objs.get(i).size());
                }
                autocplex.addEq(temp, b2vol[j]);
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
            autocplex.setParam(IloCplex.DoubleParam.TimeLimit, 900);

            // Solve autoclave packing
            start = autocplex.getCplexTime();
            if (autocplex.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime += (autocplex.getCplexTime() - start);
                writer.write("Autoclave packing found after time " + Math.round(elapsedTime) + " with objective value of " + autocplex.getObjValue() + "\n");

                // Print to output
                System.out.println("Objective value: " + autocplex.getObjValue());
                for (int i = 0; i < b1objs.size(); i++) {
                    for (int j = 0; j < b1objs.size(); j++) {
                        if (Math.round(autocplex.getValue(b1tob2[i][j])) == 1) {
                            System.out.println("Tool batch " + i + " has volume " + b1objs.get(i).size() + " and is packed into autoclave batch " + j);
                        }
                    }
                }
                int it = 0;
                for (int i = 0; i < b1objs.size(); i++) {
                    if (Math.round(autocplex.getValue(b2vol[i])) > 0) {
                        System.out.println("Autoclave batch " + it++ + " has volume " + autocplex.getValue(b2vol[i]));
                    }
                }

            } else {
                writer.write("Autoclave packing was not completed\n");
                throw new Exception("Autoclave packing was not completed");
            }

            // Obtain solution values
            ArrayList<AutoBatch> b2objs = new ArrayList<>();
            for (int i = 0; i < b1objs.size(); i++) {
                if (Math.round(autocplex.getValue(b2vol[i])) > 0) {
                    AutoBatch newb2 = new AutoBatch(0);
                    for (int j = 0; j < b1objs.size(); j++) {
                        if (Math.round(autocplex.getValue(b1tob2[j][i])) == 1) {
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
            }

            // Add indices
            int it = 0;
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
            SolutionPack soln = new SolutionPack(data.numJob, autocplex.getObjValue(), elapsedTime, autocplex.getStatus().toString());
            soln.addAutoBatch(b2objs);
            soln.addInfeas(infeasCounter);

            // Write solution to file
            data.writePack(soln);
            data.writeSum(soln);

            // Close modelers and intermediate file
            cplex.end();
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
