package models.basic_lbbd;
import common.model_helpers.*;
import java.io.*;
import java.util.*;
import ilog.cp.*;
import ilog.concert.*;

public class BasicLBBD {

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
        ArrayList<Machine> machineobjs = new ArrayList<>();
        for (int i = 0; i < data.nummachine; i++) {
            Machine newmachine = new Machine(data.machinename[i], data.machineshift[i], data.shiftstart, data.shiftend);
            machineobjs.add(newmachine);
        }
        ArrayList<Labour> labourobjs = new ArrayList<>();
        for (int i = 0; i < data.numlabourteam; i++) {
            ArrayList<String> skills = new ArrayList<>();
            for (int j = 0; j < data.numlabourskill; j++) {
                if (data.labourmatrix[i][j] == 1) {
                    skills.add(data.labourskillname[j]);
                }
            }
            Labour newlabour = new Labour(data.labourteamname[i], skills, data.labourshift[i], data.shiftstart, data.shiftend);
            labourobjs.add(newlabour);
        }
        labourobjs.add(new Labour("N/A", null, null, null, null));
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
        ArrayList<JobS> jobobjs = new ArrayList<>();
        for (int i = 0; i < data.numjob; i++) {
            JobS newjob = new JobS(data.jobname[i], data.jobpartfamily[i], data.autocapperjob[i], i, data.sj[i], data.dj[i], data.jobsteps[i], data.jobsteptimes[i]);
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

        // Time spent within each component
        int componentLimit = 600;

        // Create list to hold intermediate results
        ArrayList<AutoBatchS> b2objs;

        // Create lists to hold solution qualities over time
        ArrayList<Integer> allObjs = new ArrayList<>();
        ArrayList<Integer> objs = new ArrayList<>();
        ArrayList<Double> times = new ArrayList<>();

        // Create solution object
        Solution best = null;

        try {

            // Create file to write iterative solutions to
            FileWriter writer = new FileWriter(interfile, true);
            String initialtext = "Problem Instance: " + pi + "\n";
            writer.write(initialtext);

            // Make batches with single job to tool mappings
            ArrayList<ToolBatchS> b1objs = new ArrayList<>();
            ArrayList<JobS> remainingjobs = new ArrayList<>();
            for (JobS j : jobobjs) {
                if (j.mappedCombos().size() == 1 &&
                        j.mappedCombos().get(0).maxTop() == 1 &&
                        j.mappedCombos().get(0).maxJobPerTop() == 1) {
                    TopBatchS newtop = new TopBatchS(j.mappedCombos().get(0).top());
                    newtop.addJob(j);
                    ToolBatchS newb1 = new ToolBatchS(j.mappedCombos().get(0).bottom());
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
            cp.setParameter(IloCP.DoubleParam.TimeLimit, componentLimit);

            // Solve tool packing
            double elapsedTime = 0;
            int nonOptimal = 0;
            if (cp.solve()) {

                // Check for optimality status
                if (cp.getStatusString().equals("Feasible")) {
                    nonOptimal = 1;
                }

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
                ArrayList<JobS> jobs = new ArrayList<>();
                for (int k = 0; k < remainingjobs.size(); k++) {
                    if (cp.getValue(jobtocombo[k]) == i) {
                        jobs.add(remainingjobs.get(k));
                    }
                }
                if (jobs.size() > 0) {
                    jobs.sort(Comparator.comparing(JobS::rspOrder));
                    while (!jobs.isEmpty()) {
                        int maxnum = comboobjs.get(i).maxTop() * comboobjs.get(i).maxJobPerTop();
                        ToolBatchS newb1 = new ToolBatchS(comboobjs.get(i).bottom());
                        if (jobs.size() > maxnum) {
                            for (int j = 0; j < comboobjs.get(i).maxTop(); j++) {
                                TopBatchS newtop = new TopBatchS(comboobjs.get(i).top());
                                for (int k = 0; k < comboobjs.get(i).maxJobPerTop(); k++) {
                                    newtop.addJob(jobs.get(0));
                                    jobs.remove(0);
                                }
                                newb1.addTopBatch(newtop);
                                newtop.setToolBatch(newb1);
                            }
                        } else {
                            while (!jobs.isEmpty()) {
                                TopBatchS newtop = new TopBatchS(comboobjs.get(i).top());
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
            int iteration = 1;
            int overHour = 0;
            while (true) {

                // Autoclave packing modeler parameters
                autocp.setParameter(IloCP.IntParam.Workers, 1);
                autocp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Terse);
                if (3600 - elapsedTime > componentLimit) {
                    cp.setParameter(IloCP.DoubleParam.TimeLimit, componentLimit);
                } else if (3600 - elapsedTime > 1) {
                    cp.setParameter(IloCP.DoubleParam.TimeLimit, 3600 - elapsedTime);
                } else {

                    // Time limit reached but there have been feasible solutions found
                    writer.write("Time limit reached before master problem at iteration " + iteration + "\n");
                    break;

                }

                // Solve autoclave packing
                if (autocp.solve()) {

                    // Check for optimality status
                    if (cp.getStatusString().equals("Feasible")) {
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
                    for (int i = 0; i < autocp.getValue(bins) + 1; i++) {
                        System.out.println("Autoclave batch " + i + " has volume " + autocp.getValue(b2vol[i]));
                    }

                } else {

                    // Autoclave packing not found
                    writer.write("Autoclave packing was not completed\n");
                    throw new Exception("Autoclave packing was not completed");

                }

                // Obtain solution values
                b2objs = new ArrayList<>();
                for (int i = 0; i < autocp.getValue(bins) + 1; i++) {
                    AutoBatchS newb2 = new AutoBatchS(0);
                    for (int j = 0; j < b1objs.size(); j++) {
                        if (autocp.getValue(b1pack[j]) == i) {
                            if (newb2.capacity() == 0) {
                                newb2.setCapacity(b1objs.get(j).jobs().get(0).autoCap());
                            }
                            newb2.addToolBatch(b1objs.get(j));
                            b1objs.get(j).setAutoBatch(newb2);
                            for (TopBatchS top : b1objs.get(j).b0sS()) {
                                top.setToolBatch(b1objs.get(j));
                                for (JobS currentj : top.jobsS()) {
                                    currentj.setTopBatch(top);
                                }
                            }
                        }
                    }
                    b2objs.add(newb2);
                }

                // Create subproblem modeler
                IloCP subcp = new IloCP();

                // Define decision variables
                // prep_{j, k}, lay_{j, k}, dem_{j, k}, x_k, y_k, z_i, w_k, l_j
                for (AutoBatchS i : b2objs) {
                    IloIntervalVar commonauto = subcp.intervalVar(i.jobsS().get(0).stepTimes()[2]);
                    commonauto.setPresent();
                    i.setAuto(commonauto);
                    for (ToolBatchS k : i.b1sS()) {
                        for (JobS j : k.jobsS()) {
                            j.setVars(subcp.intervalVar(j.stepTimes()[0]),
                                    subcp.intervalVar(j.stepTimes()[1]),
                                    commonauto,
                                    subcp.intervalVar(j.stepTimes()[3]),
                                    subcp.intVar(0, data.horizon));
                        }
                        k.setVars(subcp.intervalVar(k.jobsS().stream().map(JobS::stepTimes).map(x -> x[0]).reduce(0, Integer::sum)),
                                subcp.intervalVar(k.jobsS().stream().map(JobS::stepTimes).map(x -> x[1]).reduce(0, Integer::sum)),
                                subcp.intervalVar(k.jobsS().stream().map(JobS::stepTimes).map(x -> x[3]).reduce(0, Integer::sum)));
                        k.prepVar().setPresent();
                        k.layupVar().setPresent();
                        k.demouldVar().setPresent();
                    }
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
                // Span all activities per job and precedence
                for (JobS j : jobobjs) {
                    IloIntervalSequenceVar totjob = subcp.intervalSequenceVar(
                            new IloIntervalVar[]{j.prepVar(),
                                    j.layupVar(),
                                    j.autoVar(),
                                    j.demouldVar()});
                    subcp.add(subcp.first(totjob, j.prepVar()));
                    subcp.add(subcp.last(totjob, j.demouldVar()));
                    subcp.add(subcp.before(totjob, j.layupVar(), j.autoVar()));
                    subcp.add(subcp.startBeforeEnd(j.layupVar(), j.autoVar(), -data.waittime));
                    subcp.add(subcp.noOverlap(totjob));
                }

                // Span tool prep activities
                for (ToolBatchS t : b1objs) {
                    IloIntervalVar[] temp = new IloIntervalVar[t.jobsS().size()];
                    for (int j = 0; j < t.jobsS().size(); j++) {
                        temp[j] = t.jobsS().get(j).prepVar();
                    }
                    cp.add(cp.noOverlap(temp));
                    cp.add(cp.span(t.prepVar(), temp));
                }

                // Span layup activities
                for (ToolBatchS t : b1objs) {
                    IloIntervalVar[] temp = new IloIntervalVar[t.jobsS().size()];
                    for (int j = 0; j < t.jobsS().size(); j++) {
                        temp[j] = t.jobsS().get(j).layupVar();
                    }
                    subcp.add(subcp.noOverlap(temp));
                    subcp.add(subcp.span(t.layupVar(), temp));
                    subcp.add(subcp.endBeforeEnd(t.prepVar(), t.layupVar()));
                }

                // Span demould activities
                for (ToolBatchS t : b1objs) {
                    IloIntervalVar[] temp = new IloIntervalVar[t.jobsS().size()];
                    for (int j = 0; j < t.jobsS().size(); j++) {
                        temp[j] = t.jobsS().get(j).demouldVar();
                    }
                    subcp.add(subcp.noOverlap(temp));
                    subcp.add(subcp.span(t.demouldVar(), temp));
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
                    for (int j = 0; j < data.numbatchtomachine; j++) {
                        if (data.batchtomachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.batchtomachine[j][0])) {
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
                    for (int j = 0; j < data.numbatchtomachine; j++) {
                        if (data.batchtomachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.batchtomachine[j][0])) {
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
                    for (int j = 0; j < data.numbatchtomachine; j++) {
                        if (data.batchtomachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.batchtomachine[j][0])) {
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
                    for (int j = 0; j < data.numbatchtomachine; j++) {
                        if (data.batchtomachine[j][1].equals(name)) {
                            for (Machine m : machineobjs) {
                                if (m.name().equals(data.batchtomachine[j][0])) {
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
                        for (int j = 0; j < data.numshift; j++) {
                            subcp.add(subcp.alwaysIn(m.cumul(), time + data.shiftstart[j], time + data.shiftend[j], 0, m.qtyPerShift()[j]));
                        }
                        time += 7 * 24 * 60;
                    }
                }

                // Resource utilization for prep labour
                for (ToolBatchS i : b1objs) {
                    int ind = 0;
                    for (int j = 0; j < data.numskilltomachine; j++) {
                        if (data.labourtomachine[j][0].equals(i.prepMachine().name()) &&
                                (i.jobs().get(0).partFamily().equals(data.labourtomachine[j][1]) ||
                                        data.labourtomachine[j][1].equals("N/A"))) {
                            String skill = data.labourtomachine[j][2];
                            for (Labour l : labourobjs) {
                                if (l.skills().contains(skill)) {
                                    l.cumul().add(subcp.pulse(i.prepVar(), data.labourqtyrequired[j]));
                                    i.setPrepLabour(l, data.labourqtyrequired[j]);
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
                    for (int j = 0; j < data.numskilltomachine; j++) {
                        if (data.labourtomachine[j][0].equals(i.layupMachine().name()) &&
                                (i.jobs().get(0).partFamily().equals(data.labourtomachine[j][1]) ||
                                        data.labourtomachine[j][1].equals("N/A"))) {
                            String skill = data.labourtomachine[j][2];
                            for (Labour l : labourobjs) {
                                if (l.skills().contains(skill)) {
                                    l.cumul().add(subcp.pulse(i.layupVar(), data.labourqtyrequired[j]));
                                    i.setLayupLabour(l, data.labourqtyrequired[j]);
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
                    for (int j = 0; j < data.numskilltomachine; j++) {
                        if (data.labourtomachine[j][0].equals(i.demouldMachine().name()) &&
                                (i.jobs().get(0).partFamily().equals(data.labourtomachine[j][1]) ||
                                        data.labourtomachine[j][1].equals("N/A"))) {
                            String skill = data.labourtomachine[j][2];
                            for (Labour l : labourobjs) {
                                if (l.skills().contains(skill)) {
                                    l.cumul().add(subcp.pulse(i.demouldVar(), data.labourqtyrequired[j]));
                                    i.setDemouldLabour(l, data.labourqtyrequired[j]);
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
                            for (int j = 0; j < data.numshift; j++) {
                                subcp.add(subcp.alwaysIn(i.cumul(), time + data.shiftstart[j], time + data.shiftend[j], 0, i.qtyPerShift()[j]));
                            }
                            time += 7 * 24 * 60;
                        }
                    }
                }

                // Job tardiness
                for (JobS j : jobobjs) {
                    subcp.addGe(j.tardyVar(), subcp.diff(subcp.endOf(j.demouldVar()), j.due()));
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
                if (3600 - elapsedTime > componentLimit) {
                    subcp.setParameter(IloCP.DoubleParam.TimeLimit, componentLimit);
                } else if (3600 - elapsedTime > 1) {
                    subcp.setParameter(IloCP.DoubleParam.TimeLimit, 3600 - elapsedTime);
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
                    elapsedTime = subcp.getInfo(IloCP.DoubleInfo.SolveTime);
                    String filecontent = "Schedule found after time " + Math.round(elapsedTime) + " with objective value of " + subcp.getObjValue() + "\n";
                    writer.write(filecontent);

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

                // Obtain solution values
                for (JobS j : jobobjs) {
                    j.schedule(subcp.getStart(j.prepVar()),
                            subcp.getEnd(j.prepVar()),
                            subcp.getStart(j.layupVar()),
                            subcp.getEnd(j.layupVar()),
                            subcp.getStart(j.autoVar()),
                            subcp.getEnd(j.autoVar()),
                            subcp.getStart(j.demouldVar()),
                            subcp.getEnd(j.demouldVar()));
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
                    for (int j = 0; j < data.numb2; j++) {
                        autocp.add(autocp.ifThen(autocp.eq(omega[i], 1), autocp.le(autocp.count(cutvar, j), b2objs.get(i).b1sS().size() - 1)));
                    }
                }
                autocp.addGe(cutsum, 1);

                // Make solution object and add to solution list
                Solution newsoln = new Solution(data.numjob, subcp.getObjValue(), elapsedTime, subcp.getStatusString());
                newsoln.addAutoBatch(new ArrayList<>(b2objs));
                newsoln.addLBBDIter(iteration);
                if (iteration > 1) {
                    allObjs.sort(Comparator.comparing(Integer::intValue));
                    if ((int) Math.round(subcp.getObjValue()) < allObjs.get(0)) {
                        allObjs.add((int) Math.round(subcp.getObjValue()));
                        best = newsoln;
                        objs.add((int) Math.round(subcp.getObjValue()));
                        times.add(elapsedTime);
                    }
                } else {
                    allObjs.add((int) Math.round(subcp.getObjValue()));
                    best = newsoln;
                    objs.add((int) Math.round(subcp.getObjValue()));
                    times.add(elapsedTime);
                }

                // Close subproblem modeler and increment iteration
                subcp.end();
                iteration++;

            }

            // Check if elapsed time is over one hour
            if (elapsedTime >= 3598) {
                overHour = 1;
            }

            // Print iterations and objective values for each iteration
            for (Integer i : objs) {
                writer.write("Iteration: " + (objs.indexOf(i)) + " Objective Value: " + i + "\n");
            }

            // Write solution to file
            best.addLBBD(overHour, elapsedTime, nonOptimal);
            data.writeLBBD(best, objs, times);
            data.writeSum(best);

            // Close modeler and intermediate file
            autocp.end();
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
