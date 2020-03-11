package models.basic_cp_sched;
import common.model_helpers.*;
import ilog.cp.*;
import ilog.concert.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BasicCPSched {

    public static void main(String[] args) {

        // Get settings from json file
        String filepath = args[0];
        String sumfile = args[1];
        String interfile = args[2];
        int pi = Integer.parseInt(args[3]);

        // Make data object and import raw data
        Data data = new Data(filepath, sumfile, pi);
        data.readInstanceParams();
        data.readPackParams();

        // Create instance parameter objects
        ArrayList<JobS> jobobjs = new ArrayList<>();
        for (int i = 0; i < data.numjob; i++) {
            JobS newjob = new JobS(data.jobname[i], data.jobpartfamily[i], i, data.sj[i], data.dj[i], data.jobsteps[i], data.jobsteptimes[i]);
            jobobjs.add(newjob);
        }
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

        // Create batch objects
        ArrayList<TopBatchS> b0objs = new ArrayList<>();
        ArrayList<ToolBatchS> b1objs = new ArrayList<>();
        ArrayList<AutoBatchS> b2objs = new ArrayList<>();
        for (int i = 0; i < data.numb0_new; i++) {
            for (Tool t : toptoolobjs) {
                if (data.b0toptool[i].equals(t.name())) {
                    TopBatchS newtop = new TopBatchS(t, data.b0indices[i]);
                    for (int j = 0; j < data.numjob; j++) {
                        if (data.jobtob0[j] == data.b0indices[i]) {
                            newtop.addJobS(jobobjs.get(data.jobindices[j]));
                            jobobjs.get(data.jobindices[j]).setTopBatchS(newtop);
                        }
                    }
                    b0objs.add(newtop);
                }
            }
        }
        b0objs.sort(Comparator.comparing(TopBatchS::index));
        for (int i = 0; i < data.numb1_new; i++) {
            for (Tool t : bottomtoolobjs) {
                if (data.b1bottomtool[i].equals(t.name())) {
                    ToolBatchS newtool = new ToolBatchS(t, data.b1indices[i]);
                    for (int j = 0; j < data.numb0_new; j++) {
                        if (data.b0tob1[j] == data.b1indices[i]) {
                            newtool.addTopBatchS(b0objs.get(data.b0indices[j]));
                            b0objs.get(data.b0indices[j]).setToolBatchS(newtool);
                        }
                    }
                    b1objs.add(newtool);
                }
            }
        }
        b1objs.sort(Comparator.comparing(ToolBatchS::index));
        for (int i = 0; i < data.numb2_new; i++) {
            AutoBatchS newauto = new AutoBatchS(data.b2cap[i], data.b2indices[i]);
            for (int j = 0; j < data.numb1_new; j++) {
                if (data.b1tob2[j] == data.b2indices[i]) {
                    newauto.addToolBatchS(b1objs.get(data.b1indices[j]));
                    b1objs.get(data.b1indices[j]).setAutoBatchS(newauto);
                }
            }
            b2objs.add(newauto);
        }

        try {

            // Create file to write iterative solutions to
            FileWriter writer = new FileWriter(interfile, true);
            String initialtext = "Problem Instance: " + pi + "\n";
            writer.write(initialtext);

            // Create scheduling modeler
            IloCP cp = new IloCP();

            // Define decision variables
            // prep_{j, k}, lay_{j, k}, dem_{j, k}, x_k, y_k, z_i, w_k
            for (AutoBatchS i : b2objs) {
                IloIntervalVar commonauto = cp.intervalVar(i.jobsS().get(0).stepTimes()[2]);
                commonauto.setPresent();
                i.setAuto(commonauto);
                for (ToolBatchS k : i.b1sS()) {
                    k.setVars(cp.intervalVar(k.jobsS().get(0).stepTimes()[0]),
                            cp.intervalVar(k.jobsS().get(0).stepTimes()[1]),
                            cp.intervalVar(k.jobsS().get(0).stepTimes()[3]));
                    k.prepVar().setPresent();
                    k.layupVar().setPresent();
                    k.demouldVar().setPresent();
                    k.demouldVar().setEndMax(data.horizon);
                }
            }

            // l_j
            for (JobS j : jobobjs) {
                j.addVar(cp.intVar(0, data.horizon));
            }

            // CE
            for (Tool b : bottomtoolobjs) {
                b.setCumul(cp.cumulFunctionExpr());
            }
            for (Machine m : machineobjs) {
                m.setCumul(cp.cumulFunctionExpr());
            }
            for (Labour l : labourobjs) {
                l.setCumul(cp.cumulFunctionExpr());
            }

            // Create constraints
            // Precedence between activities
            for (ToolBatchS t : b1objs) {
                cp.add(cp.endBeforeStart(t.prepVar(), t.layupVar()));
                cp.add(cp.endBeforeStart(t.layupVar(), t.b2S().autoVar()));
                cp.add(cp.endBeforeStart(t.b2S().autoVar(), t.demouldVar()));
            }

            // Resource utilization for bottom tools
            for (ToolBatchS i : b1objs) {
                IloIntervalVar temp = cp.intervalVar();
                temp.setPresent();
                cp.add(cp.span(temp, new IloIntervalVar[]{i.prepVar(), i.demouldVar()}));
                i.bottomTool().cumul().add(cp.pulse(temp, 1));
            }

            // Tool availability
            for (Tool t : bottomtoolobjs) {
                cp.add(cp.alwaysIn(t.cumul(), 0, data.horizon, 0, t.qty()));
            }

            // Resource utilization for prep machine
            for (ToolBatchS i : b1objs) {
                String name = i.jobsS().get(0).steps()[0];
                for (int j = 0; j < data.numbatchtomachine; j++) {
                    if (data.batchtomachine[j][1].equals(name)) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.batchtomachine[j][0])) {
                                m.cumul().add(cp.pulse(i.prepVar(), 1));
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
                                m.cumul().add(cp.pulse(i.layupVar(), 1));
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
                                m.cumul().add(cp.pulse(i.autoVar(), 1));
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
                                m.cumul().add(cp.pulse(i.demouldVar(), 1));
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
                        cp.add(cp.alwaysIn(m.cumul(), time + data.shiftstart[j], time + data.shiftend[j], 0, m.qtyPerShift()[j]));
                    }
                    time += 7 * 24 * 60;
                }
            }

            // Resource utilization for prep labour
            for (ToolBatchS i : b1objs) {
                int ind = 0;
                for (int j = 0; j < data.numskilltomachine; j++) {
                    if (data.labourtomachine[j][0].equals(i.prepMachine().name()) &&
                            (i.jobsS().get(0).partFamily().equals(data.labourtomachine[j][1]) ||
                                    data.labourtomachine[j][1].equals("N/A"))) {
                        String skill = data.labourtomachine[j][2];
                        for (Labour l : labourobjs) {
                            if (l.skills().contains(skill)) {
                                l.cumul().add(cp.pulse(i.prepVar(), data.labourqtyrequired[j]));
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
                            (i.jobsS().get(0).partFamily().equals(data.labourtomachine[j][1]) ||
                                    data.labourtomachine[j][1].equals("N/A"))) {
                        String skill = data.labourtomachine[j][2];
                        for (Labour l : labourobjs) {
                            if (l.skills().contains(skill)) {
                                l.cumul().add(cp.pulse(i.layupVar(), data.labourqtyrequired[j]));
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
                            (i.jobsS().get(0).partFamily().equals(data.labourtomachine[j][1]) ||
                                    data.labourtomachine[j][1].equals("N/A"))) {
                        String skill = data.labourtomachine[j][2];
                        for (Labour l : labourobjs) {
                            if (l.skills().contains(skill)) {
                                l.cumul().add(cp.pulse(i.demouldVar(), data.labourqtyrequired[j]));
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
                            cp.add(cp.alwaysIn(i.cumul(), time + data.shiftstart[j], time + data.shiftend[j], 0, i.qtyPerShift()[j]));
                        }
                        time += 7 * 24 * 60;
                    }
                }
            }

            // Job tardiness
            for (JobS j : jobobjs) {
                cp.addGe(j.tardyVar(), cp.diff(cp.endOf(j.b0S().b1S().demouldVar()), j.due()));
            }

            // Objective function
            IloIntExpr objfunc2 = cp.intExpr();
            for (JobS j : jobobjs) {
                objfunc2 = cp.sum(objfunc2, j.tardyVar());
            }
            cp.addMinimize(objfunc2);

            // Scheduling modeler parameters
            cp.setParameter(IloCP.IntParam.Workers, 1);
            cp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Terse);
            cp.setParameter(IloCP.DoubleParam.TimeLimit, 10);

            // Solve scheduling
            double elapsedTime = 0;
            if (cp.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime = cp.getInfo(IloCP.DoubleInfo.SolveTime);
                String filecontent = "Schedule found after time " + Math.round(elapsedTime) + " with objective value of " + cp.getObjValue() + "\n";
                writer.write(filecontent);

                // Obtain solution values
                for (JobS j : jobobjs) {
                    j.schedule(cp.getStart(j.b0S().b1S().prepVar()),
                            cp.getEnd(j.b0S().b1S().prepVar()),
                            cp.getStart(j.b0S().b1S().layupVar()),
                            cp.getEnd(j.b0S().b1S().layupVar()),
                            cp.getStart(j.b0S().b1S().b2S().autoVar()),
                            cp.getEnd(j.b0S().b1S().b2S().autoVar()),
                            cp.getStart(j.b0S().b1S().demouldVar()),
                            cp.getEnd(j.b0S().b1S().demouldVar()));
                }
                for (ToolBatchS t : b1objs) {
                    t.schedule(cp.getStart(t.prepVar()),
                            cp.getEnd(t.prepVar()),
                            cp.getStart(t.layupVar()),
                            cp.getEnd(t.layupVar()),
                            cp.getStart(t.demouldVar()),
                            cp.getEnd(t.demouldVar()));
                }
                for (AutoBatchS a : b2objs) {
                    a.autoSched(cp.getStart(a.autoVar()),
                            cp.getEnd(a.autoVar()));
                }

                // Print to output
                System.out.println("Objective value: " + cp.getObjValue());
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

            // Make solution object
            SolutionSched soln = new SolutionSched(data.numjob, cp.getObjValue(), elapsedTime, cp.getStatusString());
            soln.addAutoBatchS(b2objs);

            // Write solution to file
            data.writeSched(soln);
            data.writeSum(soln);

            // Close modeler and intermediate file
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
