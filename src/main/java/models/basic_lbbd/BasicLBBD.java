package models.basic_lbbd;
import common.model_helpers.*;
import java.io.*;
import java.util.*;
import ilog.cp.*;
import ilog.concert.*;
import ilog.cplex.IloCplex;

public class BasicLBBD {

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
        ArrayList<Machine> machineobjs = new ArrayList<>();
        for (int i = 0; i < data.numMachine; i++) {
            Machine newmachine = new Machine(data.machineName[i], data.machineShift[i], data.shiftStart, data.shiftEnd);
            machineobjs.add(newmachine);
        }
        ArrayList<Labour> labourobjs = new ArrayList<>();
        for (int i = 0; i < data.numLabour; i++) {
            ArrayList<String> skills = new ArrayList<>();
            for (int j = 0; j < data.numLabourSkill; j++) {
                if (data.labourMatrix[i][j] == 1) {
                    skills.add(data.labourSkillName[j]);
                }
            }
            Labour newlabour = new Labour(data.labourName[i], skills, data.labourShift[i], data.shiftStart, data.shiftEnd);
            labourobjs.add(newlabour);
        }
        labourobjs.add(new Labour("N/A", null, null, null, null));
        ArrayList<ToolCombo> comboobjs = new ArrayList<>();
        int it = 0;
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
                ToolCombo newcombo = new ToolCombo(toptemp, bottomtemp, it++);
                comboobjs.add(newcombo);
            }
        }
        ArrayList<JobS> jobobjs = new ArrayList<>();
        for (int i = 0; i < data.numJob; i++) {
            JobS newjob = new JobS(data.jobName[i], data.jobPartFamily[i], data.jobAutoCap[i], i, data.jobSize[i], data.jobDue[i], data.jobSteps[i], data.jobStepTimes[i]);
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

        // Time spent within each component
        int componentLimit = 120;
        int totalLimit = 3600;

        // Extra LBBD summary variable indicating component nonoptimality
        int nonOptimal = 0;

        // Create lists to hold solution qualities over time
        ArrayList<Integer> allObjs = new ArrayList<>();
        ArrayList<Double> objs = new ArrayList<>();
        ArrayList<Double> times = new ArrayList<>();

        try {

            // Create file to write iterative solutions to
            FileWriter writer = new FileWriter(interfile, true);
            String initialtext = "Problem Instance: " + pi + "\n";
            writer.write(initialtext);

            // Make batches with single job to tool mappings
            ArrayList<ToolBatchS> b1objs = new ArrayList<>();
            ArrayList<JobS> remainingjobs = new ArrayList<>();
            int b0it = 0;
            int b1it = 0;
            for (JobS j : jobobjs) {
                if (j.mappedCombos().size() == 1 &&
                        j.mappedCombos().get(0).maxTop() == 1 &&
                        j.mappedCombos().get(0).maxJobPerTop() == 1) {
                    TopBatchS newtop = new TopBatchS(j.mappedCombos().get(0).top(), b0it++);
                    newtop.addJobS(j);
                    j.setTopBatchS(newtop);
                    ToolBatchS newb1 = new ToolBatchS(j.mappedCombos().get(0).bottom(), b1it++);
                    newtop.setToolBatchS(newb1);
                    newb1.addTopBatchS(newtop);
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
                ArrayList<JobS> jobs = new ArrayList<>();
                for (int k = 0; k < remainingjobs.size(); k++) {
                    if (cplex.getValue(jobtocombo[k][i]) == 1) {
                        jobs.add(remainingjobs.get(k));
                    }
                }
                if (jobs.size() > 0) {
                    jobs.sort(Comparator.comparing(JobS::rspOrder));
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
                        ToolBatchS newb1;
                        if (altc != null && jobs.size() < minnum && jobs.size() >= altminnum && jobs.size() <= altmaxnum) {
                            newb1 = new ToolBatchS(altc.bottom(), b1it++);
                            while (!jobs.isEmpty()) {
                                TopBatchS newtop = new TopBatchS(altc.top(), b0it++);
                                for (int k = 0; k < altc.maxJobPerTop(); k++) {
                                    if (!jobs.isEmpty()) {
                                        newtop.addJobS(jobs.get(0));
                                        jobs.remove(0);
                                    } else {
                                        break;
                                    }
                                }
                                newb1.addTopBatchS(newtop);
                                newtop.setToolBatchS(newb1);
                            }
                            if (newb1.jobsS().size() < altminnum) {
                                infeasCounter++;
                            }
                        } else {
                            newb1 = new ToolBatchS(comboobjs.get(i).bottom(), b1it++);
                            int size = jobs.size();
                            if (jobs.size() < comboobjs.get(i).maxJobPerTop() * comboobjs.get(i).maxTop()) {
                                while (!jobs.isEmpty()) {
                                    TopBatchS newtop = new TopBatchS(comboobjs.get(i).top(), b0it++);
                                    for (int k = 0; k < comboobjs.get(i).maxJobPerTop(); k++) {
                                        newtop.addJobS(jobs.get(0));
                                        jobs.remove(0);
                                        if (jobs.isEmpty()) {
                                            break;
                                        }
                                    }
                                    newb1.addTopBatchS(newtop);
                                    newtop.setToolBatchS(newb1);
                                }
                            } else {
                                for (int j = 0; j < Math.min(size, comboobjs.get(i).maxTop()); j++) {
                                    TopBatchS newtop = new TopBatchS(comboobjs.get(i).top(), b0it++);
                                    for (int k = 0; k < comboobjs.get(i).maxJobPerTop(); k++) {
                                        newtop.addJobS(jobs.get(0));
                                        jobs.remove(0);
                                    }
                                    newb1.addTopBatchS(newtop);
                                    newtop.setToolBatchS(newb1);
                                }
                            }
                            if (newb1.jobsS().size() < minnum) {
                                infeasCounter++;
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
            IloCP autocp = new IloCP();

            // Define decision variables
            // x_k
            IloIntVar[] b1pack = new IloIntVar[b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                b1pack[i] = autocp.intVar(0, b1objs.size() - 1);
            }

            // v_i
            IloIntVar[] b2vol = new IloIntVar[b1objs.size()];
            for (int i = 0; i < b1objs.size(); i++) {
                b2vol[i] = autocp.intVar(0, b1objs.get(i).jobsS().get(0).autoCap());
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

            // Only pack tool batches into bins with correct capacity
            for (int i = 0; i < b1objs.size(); i++) {
                ArrayList<Integer> allowedBins = new ArrayList<>();
                for (int j = 0; j < b1objs.size(); j++) {
                    if (b1objs.get(i).jobsS().get(0).autoCap() == b1objs.get(j).jobsS().get(0).autoCap()) {
                        allowedBins.add(j);
                    }
                }
                autocp.add(autocp.allowedAssignments(b1pack[i], allowedBins.stream().mapToInt(x -> x).toArray()));
            }

            // Calculate number of bins
            autocp.addEq(bins, autocp.diff(b1objs.size(), autocp.count(b2vol, 0)));

            // Only tool batches with the same cycle can be batched into the same autoclave batch
            for (int i = 0; i < b1objs.size(); i++) {
                for (int j = 0; j < b1objs.size(); j++) {
                    if (!b1objs.get(i).jobsS().get(0).steps()[2].equals(b1objs.get(j).jobsS().get(0).steps()[2])) {
                        autocp.add(autocp.allDiff(new IloIntVar[]{b1pack[i], b1pack[j]}));
                    }
                }
            }

            // Only the present quantity of tools can be in the same autoclave batch
            ArrayList<Tool> completedtools = new ArrayList<>();
            for (ToolBatchS b1 : b1objs) {
                if (!completedtools.contains(b1.bottomTool())) {
                    ArrayList<IloIntVar> toolvars = new ArrayList<>();
                    for (ToolBatchS newb1 : b1objs) {
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

            // Enter loop
            int iteration = 0;
            int overHour = 0;
            while (true) {

                // Autoclave packing modeler parameters
                autocp.setParameter(IloCP.IntParam.Workers, 1);
                autocp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Terse);
                if (totalLimit - elapsedTime > componentLimit) {
                    autocp.setParameter(IloCP.DoubleParam.TimeLimit, componentLimit);
                } else if (totalLimit - elapsedTime > 1) {
                    autocp.setParameter(IloCP.DoubleParam.TimeLimit, totalLimit - elapsedTime);
                } else {

                    // Time limit reached but there have been feasible solutions found
                    writer.write("Time limit reached before master problem at iteration " + iteration + "\n");
                    break;

                }

                // Solve autoclave packing
                if (autocp.solve()) {

                    // Check for optimality status
                    if (autocp.getStatusString().equals("Feasible")) {
                        nonOptimal = 1;
                    }

                    // Get solution time and write summary to intermediate file
                    elapsedTime += autocp.getInfo(IloCP.DoubleInfo.SolveTime);
                    writer.write("Autoclave packing found after time " + Math.round(elapsedTime) + " with objective value of " + autocp.getObjValue() + "\n");

                    // Print to output
                    System.out.println("Objective value: " + autocp.getObjValue());
                    for (int i = 0; i < b1objs.size(); i++) {
                        System.out.println("Tool batch " + i + " has volume " + b1vol[i] + " and is packed into autoclave batch " + autocp.getValue(b1pack[i]));
                    }
                    for (int i = 0; i < b1objs.size(); i++) {
                        if (autocp.getValue(b2vol[i]) > 0) {
                            System.out.println("Autoclave batch " + i + " has volume " + autocp.getValue(b2vol[i]));
                        }
                    }

                } else {

                    // Autoclave packing not found
                    writer.write("Autoclave packing was not completed\n");
                    throw new Exception("Autoclave packing was not completed");

                }

                // Obtain solution values
                ArrayList<AutoBatchS> b2objs = new ArrayList<>();
                int b2it = 0;
                for (int i = 0; i < b1objs.size(); i++) {
                    if (autocp.getValue(b2vol[i]) > 0) {
                        AutoBatchS newb2 = new AutoBatchS(0, b2it++);
                        for (int j = 0; j < b1objs.size(); j++) {
                            if (autocp.getValue(b1pack[j]) == i) {
                                if (newb2.capacity() == 0) {
                                    newb2.setCapacity(b1objs.get(j).jobsS().get(0).autoCap());
                                }
                                newb2.addToolBatchS(b1objs.get(j));
                                b1objs.get(j).setAutoBatchS(newb2);
                                for (TopBatchS top : b1objs.get(j).b0sS()) {
                                    top.setToolBatchS(b1objs.get(j));
                                    for (JobS currentj : top.jobsS()) {
                                        currentj.setTopBatchS(top);
                                    }
                                }
                            }
                        }
                        b2objs.add(newb2);
                    }
                }

                // Create subproblem modeler
                IloCP subcp = new IloCP();

                // Define decision variables
                // prep_{j, k}, lay_{j, k}, dem_{j, k}, x_k, y_k, z_i, w_k
                for (AutoBatchS i : b2objs) {
                    IloIntervalVar commonauto = subcp.intervalVar(i.jobsS().get(0).stepTimes()[2]);
                    commonauto.setPresent();
                    i.setAuto(commonauto);
                    for (ToolBatchS k : i.b1sS()) {
                        k.setVariables(subcp.intervalVar(k.jobsS().get(0).stepTimes()[0]),
                                subcp.intervalVar(k.jobsS().get(0).stepTimes()[1]),
                                subcp.intervalVar(k.jobsS().get(0).stepTimes()[3]));
                        k.prepVar().setPresent();
                        k.layupVar().setPresent();
                        k.demouldVar().setPresent();
                        k.demouldVar().setEndMax(data.horizon);
                    }
                }

                // l_j
                for (JobS j : jobobjs) {
                    j.addVar(subcp.intVar(0, data.horizon));
                }

                // CE
                for (Tool b : bottomtoolobjs) {
                    b.setCumul(subcp.cumulFunctionExpr());
                }
                for (Machine m : machineobjs) {
                    m.setCumul(subcp.cumulFunctionExpr());
                }
                for (Labour l : labourobjs) {
                    l.setCumul(subcp.cumulFunctionExpr());
                }

                // Create constraints
                // Precedence between activities
                for (ToolBatchS t : b1objs) {
                    subcp.add(subcp.endBeforeStart(t.prepVar(), t.layupVar()));
                    subcp.add(subcp.endBeforeStart(t.layupVar(), t.b2S().autoVar()));
                    subcp.add(subcp.endBeforeStart(t.b2S().autoVar(), t.demouldVar()));
                }

                // Resource utilization for bottom tools
                for (ToolBatchS i : b1objs) {
                    IloIntervalVar temp = subcp.intervalVar();
                    temp.setPresent();
                    subcp.add(subcp.span(temp, new IloIntervalVar[]{i.prepVar(), i.demouldVar()}));
                    i.bottomTool().cumul().add(subcp.pulse(temp, 1));
                }

                // Tool availability
                for (Tool t : bottomtoolobjs) {
                    subcp.add(subcp.alwaysIn(t.cumul(), 0, data.horizon, 0, t.qty()));
                }

                // Resource utilization for prep machine
                for (ToolBatchS i : b1objs) {
                    String name = i.jobsS().get(0).steps()[0];
                    for (int j = 0; j < data.numStepToMachine; j++) {
                        if (data.stepToMachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.stepToMachine[j][0])) {
                                    m.cumul().add(subcp.pulse(i.prepVar(), 1));
                                    i.setPrepMachine(m);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                // Resource utilization for layup machine
                for (ToolBatchS i : b1objs) {
                    String name = i.jobsS().get(0).steps()[1];
                    for (int j = 0; j < data.numStepToMachine; j++) {
                        if (data.stepToMachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.stepToMachine[j][0])) {
                                    m.cumul().add(subcp.pulse(i.layupVar(), 1));
                                    i.setLayupMachine(m);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                // Resource utilization for auto machine
                for (AutoBatchS i : b2objs) {
                    String name = i.jobsS().get(0).steps()[2];
                    for (int j = 0; j < data.numStepToMachine; j++) {
                        if (data.stepToMachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.stepToMachine[j][0])) {
                                    m.cumul().add(subcp.pulse(i.autoVar(), 1));
                                    i.setAutoMachine(m);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                // Resource utilization for demould machine
                for (ToolBatchS i : b1objs) {
                    String name = i.jobsS().get(0).steps()[3];
                    for (int j = 0; j < data.numStepToMachine; j++) {
                        if (data.stepToMachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.stepToMachine[j][0])) {
                                    m.cumul().add(subcp.pulse(i.demouldVar(), 1));
                                    i.setDemouldMachine(m);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                // Machine availability
                for (Machine m : machineobjs) {
                    int time = 0;
                    while (time < data.horizon) {
                        for (int j = 0; j < data.numShift; j++) {
                            subcp.add(subcp.alwaysIn(m.cumul(), time + data.shiftStart[j], time + data.shiftEnd[j], 0, m.qtyPerShift()[j]));
                        }
                        time += 7 * 24 * 60;
                    }
                }

                // Resource utilization for prep labour
                for (ToolBatchS i : b1objs) {
                    int ind = 0;
                    for (int j = 0; j < data.numLabourSkillToMachine; j++) {
                        if (data.labourToMachine[j][0].equals(i.prepMachine().name()) &&
                                (i.jobsS().get(0).partFamily().equals(data.labourToMachine[j][1]) ||
                                        data.labourToMachine[j][1].equals("N/A"))) {
                            String skill = data.labourToMachine[j][2];
                            for (Labour l : labourobjs) {
                                if (l.skills().contains(skill)) {
                                    l.cumul().add(subcp.pulse(i.prepVar(), data.labourQtyRequired[j]));
                                    i.setPrepLabour(l, data.labourQtyRequired[j]);
                                    ind = 1;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (ind == 0) {
                        i.setPrepLabour(labourobjs.get(labourobjs.size() - 1), 0);
                    }
                }

                // Resource utilization for layup labour
                for (ToolBatchS i : b1objs) {
                    int ind = 0;
                    for (int j = 0; j < data.numLabourSkillToMachine; j++) {
                        if (data.labourToMachine[j][0].equals(i.layupMachine().name()) &&
                                (i.jobsS().get(0).partFamily().equals(data.labourToMachine[j][1]) ||
                                        data.labourToMachine[j][1].equals("N/A"))) {
                            String skill = data.labourToMachine[j][2];
                            for (Labour l : labourobjs) {
                                if (l.skills().contains(skill)) {
                                    l.cumul().add(subcp.pulse(i.layupVar(), data.labourQtyRequired[j]));
                                    i.setLayupLabour(l, data.labourQtyRequired[j]);
                                    ind = 1;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (ind == 0) {
                        i.setLayupLabour(labourobjs.get(labourobjs.size() - 1), 0);
                    }
                }

                // Resource utilization for demould labour
                for (ToolBatchS i : b1objs) {
                    int ind = 0;
                    for (int j = 0; j < data.numLabourSkillToMachine; j++) {
                        if (data.labourToMachine[j][0].equals(i.demouldMachine().name()) &&
                                (i.jobsS().get(0).partFamily().equals(data.labourToMachine[j][1]) ||
                                        data.labourToMachine[j][1].equals("N/A"))) {
                            String skill = data.labourToMachine[j][2];
                            for (Labour l : labourobjs) {
                                if (l.skills().contains(skill)) {
                                    l.cumul().add(subcp.pulse(i.demouldVar(), data.labourQtyRequired[j]));
                                    i.setDemouldLabour(l, data.labourQtyRequired[j]);
                                    ind = 1;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (ind == 0) {
                        i.setDemouldLabour(labourobjs.get(labourobjs.size() - 1), 0);
                    }
                }

                // Labour availability
                for (Labour i : labourobjs) {
                    if (!i.name().equals("N/A")) {
                        int time = 0;
                        while (time < data.horizon) {
                            for (int j = 0; j < data.numShift; j++) {
                                subcp.add(subcp.alwaysIn(i.cumul(), time + data.shiftStart[j], time + data.shiftEnd[j], 0, i.qtyPerShift()[j]));
                            }
                            time += 7 * 24 * 60;
                        }
                    }
                }

                // Job tardiness
                for (JobS j : jobobjs) {
                    subcp.addGe(j.tardyVar(), subcp.diff(subcp.endOf(j.b0S().b1S().demouldVar()), j.due()));
                }

                // Objective function
                IloIntExpr objfunc2 = subcp.intExpr();
                for (JobS j : jobobjs) {
                    objfunc2 = subcp.sum(objfunc2, j.tardyVar());
                }
                subcp.addMinimize(objfunc2);

                // Subproblem modeler parameters
                subcp.setParameter(IloCP.IntParam.Workers, 1);
                subcp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Terse);
                if (totalLimit - elapsedTime > componentLimit) {
                    subcp.setParameter(IloCP.DoubleParam.TimeLimit, componentLimit);
                } else if (totalLimit - elapsedTime > 1) {
                    subcp.setParameter(IloCP.DoubleParam.TimeLimit, totalLimit - elapsedTime);
                } else {

                    // Time limit reached but there have been feasible solutions found
                    writer.write("Time limit reached before subproblem at iteration " + iteration + "\n");
                    break;

                }

                // Solve subproblem
                if (subcp.solve()) {

                    // Check for optimality status
                    if (subcp.getStatusString().equals("Feasible")) {
                        nonOptimal = 1;
                    }

                    // Get solution time and write summary to intermediate file
                    elapsedTime += subcp.getInfo(IloCP.DoubleInfo.SolveTime);
                    String filecontent = "Schedule found after time " + Math.round(elapsedTime) + " with objective value of " + subcp.getObjValue() + "\n";
                    writer.write(filecontent);

                    // Obtain solution values
                    for (JobS j : jobobjs) {
                        j.schedule(subcp.getStart(j.b0S().b1S().prepVar()),
                                subcp.getEnd(j.b0S().b1S().prepVar()),
                                subcp.getStart(j.b0S().b1S().layupVar()),
                                subcp.getEnd(j.b0S().b1S().layupVar()),
                                subcp.getStart(j.b0S().b1S().b2S().autoVar()),
                                subcp.getEnd(j.b0S().b1S().b2S().autoVar()),
                                subcp.getStart(j.b0S().b1S().demouldVar()),
                                subcp.getEnd(j.b0S().b1S().demouldVar()));
                    }
                    for (ToolBatchS t : b1objs) {
                        t.schedule(subcp.getStart(t.prepVar()),
                                subcp.getEnd(t.prepVar()),
                                subcp.getStart(t.layupVar()),
                                subcp.getEnd(t.layupVar()),
                                subcp.getStart(t.demouldVar()),
                                subcp.getEnd(t.demouldVar()));
                    }
                    for (AutoBatchS a : b2objs) {
                        a.schedule(subcp.getStart(a.autoVar()),
                                subcp.getEnd(a.autoVar()));
                    }

                    // Print to output
                    System.out.println("Objective value: " + subcp.getObjValue());
                    for (AutoBatchS a : b2objs) {
                        for (ToolBatchS t : a.b1sS()) {
                            for (JobS j : t.jobsS()) {
                                System.out.println("Job " + j.index() + " ends at " + j.demouldEnd() + " and is due at " + j.due() + " so has lateness of " + j.tardiness());
                            }
                            System.out.println("Tool batch prep starts at time " + t.prepStart() + " and ends at time " + t.prepEnd());
                            System.out.println("Tool batch layup starts at time " + t.layupStart() + " and ends at time " + t.layupEnd());
                            System.out.println("Tool batch uses prep machine " + t.prepMachine().name() + " and layup machine " + t.layupMachine().name() + " and demould machine " + t.demouldMachine().name());
                            System.out.println("Tool batch uses prep team " + t.prepLabour().name() + " and layup team " + t.layupLabour().name() + " and demould team " + t.demouldLabour().name());
                            System.out.println("Tool batch demould starts at time " + t.demouldStart() + " and ends at time " + t.demouldEnd());
                        }
                        System.out.println("Autoclave batch curing starts at time " + a.autoStart() + " and ends at time " + a.autoEnd());
                        System.out.println("Autoclave batch uses curing machine " + a.autoMachine().name());
                    }

                }

                // Add cuts
                IloIntVar[] omega = new IloIntVar[b2objs.size()];
                IloIntExpr cutsum = autocp.intExpr();
                for (int i = 0; i < b2objs.size(); i++) {
                    omega[i] = autocp.intVar(0, 1);
                    cutsum = autocp.sum(cutsum, omega[i]);
                    IloIntVar[] cutvar = new IloIntVar[b2objs.get(i).b1sS().size()];
                    for (int j = 0; j < b2objs.get(i).b1sS().size(); j++) {
                        cutvar[j] = b1pack[b1objs.indexOf(b2objs.get(i).b1sS().get(j))];
                    }
                    for (int j = 0; j < data.numB2; j++) {
                        autocp.add(autocp.ifThen(autocp.eq(omega[i], 1), autocp.le(autocp.count(cutvar, j), b2objs.get(i).b1sS().size() - 1)));
                    }
                }
                autocp.addGe(cutsum, 1);

                // Make solution object and write to file if better than before
                Solution newsoln = new Solution(data.numJob, subcp.getObjValue(), elapsedTime, subcp.getStatusString());
                newsoln.addAutoBatchS(b2objs);
                objs.add(subcp.getObjValue());
                times.add(elapsedTime);
                if (iteration > 1) {
                    allObjs.sort(Comparator.comparing(Integer::intValue));
                    if ((int) Math.round(subcp.getObjValue()) < allObjs.get(0)) {
                        data.writeLBBD(newsoln);
                        data.writeQual(objs, times);
                        data.writeSum(newsoln, infeasCounter);
                    }
                } else {
                    data.writeLBBD(newsoln);
                    data.writeQual(objs, times);
                    data.writeSum(newsoln, infeasCounter);
                }
                allObjs.add((int) Math.round(subcp.getObjValue()));

                // Close subproblem modeler and increment iteration
                subcp.end();
                iteration++;

            }

            // Check if elapsed time is over one hour
            if (elapsedTime >= 3598) {
                overHour = 1;
            }

            // Print iterations and objective values for each iteration
            for (Double i : objs) {
                writer.write("Iteration: " + (objs.indexOf(i)) + " Objective Value: " + i + "\n");
            }

            // Write extra information to summary file
            data.writeSumExtra(iteration, overHour, elapsedTime, nonOptimal);

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
