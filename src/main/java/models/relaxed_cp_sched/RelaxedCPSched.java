package models.relaxed_cp_sched;
import common.model_helpers.*;
import java.util.*;
import java.io.*;
import ilog.cp.*;
import ilog.concert.*;

public class RelaxedCPSched {

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
        ArrayList<JobRSP> jobobjs = new ArrayList<>();
        for (int i = 0; i < data.numJob; i++) {
            JobRSP newjob = new JobRSP(data.jobName[i], data.jobPartFamily[i], i, data.jobSize[i], data.jobDue[i], data.jobSteps[i], data.jobStepTimes[i]);
            jobobjs.add(newjob);
        }
        ArrayList<Tool> bottomobjs = new ArrayList<>();
        for (int i = 0; i < data.numBottomTool; i++) {
            Tool newtool = new Tool(data.bottomToolName[i], data.bottomToolCap[i], data.bottomToolQty[i], data.bottomToolSize[i], data.bottomToolMin[i]);
            bottomobjs.add(newtool);
        }
        ArrayList<Machine> machineobjs = new ArrayList<>();
        for (int i = 0; i < data.numMachine; i++) {
            Machine newmachine = new Machine(data.machineName[i], data.machineShift[i], data.shiftStart, data.shiftEnd, data.machineCap[i]);
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

        try {

            // Create file to write iterative solutions to
            FileWriter writer = new FileWriter(interfile, true);
            String initialtext = "Problem Instance: " + pi + "\n";
            writer.write(initialtext);

            // Create RSP modeler
            IloCP cp = new IloCP();

            // Define decision variables
            // prep_j, layup_j, auto_j, demould_j, l_j
            for (JobRSP j : jobobjs) {
                j.setVars(cp.intervalVar(j.stepTimes()[0]),
                        cp.intervalVar(j.stepTimes()[1]),
                        cp.intervalVar(j.stepTimes()[2]),
                        cp.intervalVar(j.stepTimes()[3]),
                        cp.intVar(0, data.horizon));
                j.prepVar().setEndMax(data.horizon);
                j.layupVar().setEndMax(data.horizon);
                j.autoVar().setEndMax(data.horizon);
                j.demouldVar().setEndMax(data.horizon);
            }

            // CE
            for (Tool b : bottomobjs) {
                b.setCumul(cp.cumulFunctionExpr());
            }
            for (Machine m : machineobjs) {
                m.setCumul(cp.cumulFunctionExpr());
            }
            for (Labour l : labourobjs) {
                l.setCumul(cp.cumulFunctionExpr());
            }

            // Create constraints
            // Job activity ordering
            for (JobRSP j : jobobjs) {
                IloIntervalSequenceVar totjob = cp.intervalSequenceVar(
                        new IloIntervalVar[]{j.prepVar(),
                        j.layupVar(),
                        j.autoVar(),
                        j.demouldVar()});
                cp.add(cp.first(totjob, j.prepVar()));
                cp.add(cp.last(totjob, j.demouldVar()));
                cp.add(cp.before(totjob, j.layupVar(), j.autoVar()));
                cp.add(cp.startBeforeEnd(j.layupVar(), j.autoVar(), -data.restrictedWaitTime));
                cp.add(cp.noOverlap(totjob));
            }

            // Resource utilization for bottom tools
            for (JobRSP j : jobobjs) {
                ArrayList<String> possibletoolnames = new ArrayList<>();
                ArrayList<Tool> possibletools = new ArrayList<>();
                for (int i = 0; i < data.numJobToTool; i++) {
                    if (data.jobToTool[i][0].equals(j.name())) {
                        possibletoolnames.add(data.jobToTool[i][2]);
                    }
                }
                for (Tool b : bottomobjs) {
                    if (possibletoolnames.contains(b.name())) {
                        possibletools.add(b);
                    }
                }
                possibletools.sort(Comparator.comparing(Tool::qty).reversed());
                possibletools.sort(Comparator.comparing(Tool::size));
                Tool best = possibletools.get(0);
                best.cumul().add(cp.pulse(j.prepVar(), 1));
                best.cumul().add(cp.pulse(j.layupVar(), 1));
                best.cumul().add(cp.pulse(j.autoVar(), 1));
                best.cumul().add(cp.pulse(j.demouldVar(), 1));
            }

            // Bottom tool availability
            for (Tool b : bottomobjs) {
                cp.add(cp.alwaysIn(b.cumul(), 0, data.horizon, 0, b.qty()));
            }

            // Resource utilization for prep machines
            for (JobRSP j : jobobjs) {
                for (int i = 0; i < data.numStepToMachine; i++) {
                    if (data.stepToMachine[i][1].equals(j.steps()[0])) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.stepToMachine[i][0])) {
                                m.cumul().add(cp.pulse(j.prepVar(), 1));
                                j.setPrepMachine(m);
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // Resource utilization for layup machines
            for (JobRSP j : jobobjs) {
                for (int i = 0; i < data.numStepToMachine; i++) {
                    if (data.stepToMachine[i][1].equals(j.steps()[1])) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.stepToMachine[i][0])) {
                                m.cumul().add(cp.pulse(j.layupVar(), 1));
                                j.setLayupMachine(m);
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // Resource utilization for auto machines
            for (JobRSP j : jobobjs) {
                for (int i = 0; i < data.numStepToMachine; i++) {
                    if (data.stepToMachine[i][1].equals(j.steps()[2])) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.stepToMachine[i][0])) {
                                m.cumul().add(cp.pulse(j.autoVar(), j.size()));
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // Resource utilization for demould machines
            for (JobRSP j : jobobjs) {
                for (int i = 0; i < data.numStepToMachine; i++) {
                    if (data.stepToMachine[i][1].equals(j.steps()[3])) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.stepToMachine[i][0])) {
                                m.cumul().add(cp.pulse(j.demouldVar(), 1));
                                j.setDemouldMachine(m);
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // Machine availability
            for (Machine m : machineobjs) {
                if (m.name().contains("Auto")) {
                    int time = 0;
                    while (time < data.horizon) {
                        for (int j = 0; j < data.numShift; j++) {
                            cp.add(cp.alwaysIn(m.cumul(), time + data.shiftStart[j], time + data.shiftEnd[j], 0, m.capacity()));
                        }
                        time += 7 * 24 * 60;
                    }
                } else {
                    int time = 0;
                    while (time < data.horizon) {
                        for (int j = 0; j < data.numShift; j++) {
                            cp.add(cp.alwaysIn(m.cumul(), time + data.shiftStart[j], time + data.shiftEnd[j], 0, m.qtyPerShift()[j]));
                        }
                        time += 7 * 24 * 60;
                    }
                }
            }

            // Resource utilization for prep labours
            for (JobRSP j : jobobjs) {
                for (int i = 0; i < data.numLabourSkillToMachine; i++) {
                    if (data.labourToMachine[i][0].equals(j.prepMachine().name()) &&
                            (j.partFamily().equals(data.labourToMachine[i][1]) ||
                                    data.labourToMachine[i][1].equals("N/A"))) {
                        String skill = data.labourToMachine[i][2];
                        for (Labour l : labourobjs) {
                            if (l.skills().contains(skill)) {
                                l.cumul().add(cp.pulse(j.prepVar(), data.labourQtyRequired[i]));
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // Resource utilization for layup labours
            for (JobRSP j : jobobjs) {
                for (int i = 0; i < data.numLabourSkillToMachine; i++) {
                    if (data.labourToMachine[i][0].equals(j.layupMachine().name()) &&
                            (j.partFamily().equals(data.labourToMachine[i][1]) ||
                                    data.labourToMachine[i][1].equals("N/A"))) {
                        String skill = data.labourToMachine[i][2];
                        for (Labour l : labourobjs) {
                            if (l.skills().contains(skill)) {
                                l.cumul().add(cp.pulse(j.layupVar(), data.labourQtyRequired[i]));
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // Resource utilization for demould labours
            for (JobRSP j : jobobjs) {
                for (int i = 0; i < data.numLabourSkillToMachine; i++) {
                    if (data.labourToMachine[i][0].equals(j.demouldMachine().name()) &&
                            (j.partFamily().equals(data.labourToMachine[i][1]) ||
                                    data.labourToMachine[i][1].equals("N/A"))) {
                        String skill = data.labourToMachine[i][2];
                        for (Labour l : labourobjs) {
                            if (l.skills().contains(skill)) {
                                l.cumul().add(cp.pulse(j.demouldVar(), data.labourQtyRequired[i]));
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // Labour availability
            for (Labour i : labourobjs) {
                int time = 0;
                while (time < data.horizon) {
                    for (int j = 0; j < data.numShift; j++) {
                        cp.add(cp.alwaysIn(i.cumul(), time + data.shiftStart[j], time + data.shiftEnd[j], 0, i.qtyPerShift()[j]));
                    }
                    time += 7 * 24 * 60;
                }
            }

            // Job tardiness
            for (JobRSP j : jobobjs) {
                cp.addGe(j.tardyVar(), cp.diff(cp.endOf(j.demouldVar()), j.due()));
                cp.addGe(j.tardyVar(), 0);
            }

            // Objective function
            IloIntExpr objfunc2 = cp.intExpr();
            for (JobRSP j : jobobjs) {
                objfunc2 = cp.sum(objfunc2, j.tardyVar());
            }
            cp.addMinimize(objfunc2);

            // RSP modeler parameters
            cp.setParameter(IloCP.IntParam.Workers, 1);
            cp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Terse);
            cp.setParameter(IloCP.DoubleParam.TimeLimit, 60);

            // Solve RSP
            double elapsedTime = 0;
            if (cp.solve()) {

                // Get solution time and write summary to intermediate file
                elapsedTime = cp.getInfo(IloCP.DoubleInfo.SolveTime);
                String filecontent = "Schedule found after time " + Math.round(elapsedTime) + " with objective value of " + cp.getObjValue() + "\n";
                writer.write(filecontent);

                // Obtain solution values
                for (JobRSP j : jobobjs) {
                    j.schedule(cp.getStart(j.prepVar()),
                            cp.getEnd(j.prepVar()),
                            cp.getStart(j.layupVar()),
                            cp.getEnd(j.layupVar()),
                            cp.getStart(j.autoVar()),
                            cp.getEnd(j.autoVar()),
                            cp.getStart(j.demouldVar()),
                            cp.getEnd(j.demouldVar()));
                }

                // Print to output
                System.out.println("Objective value: " + cp.getObjValue());
                for (JobRSP j : jobobjs) {
                    System.out.println("Job " + j.index() + " due at " + j.due() + ":");
                    System.out.println("Prep starts at " + j.prepStart() + " and ends at " + j.prepEnd());
                    System.out.println("Layup starts at " + j.layupStart() + " and ends at " + j.layupEnd());
                    System.out.println("Curing starts at " + j.autoStart() + " and ends at " + j.autoEnd());
                    System.out.println("Demould starts at " + j.demouldStart() + " and ends at " + j.demouldEnd());
                    System.out.println("Tardiness of " + j.tardiness());
                    System.out.println();
                }

            }

            // Make solution object
            SolutionRSP soln = new SolutionRSP(data.numJob, cp.getObjValue(), elapsedTime, cp.getStatusString());
            soln.addJobs(jobobjs);

            // Write solution to file
            data.writeRSP(soln);
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
