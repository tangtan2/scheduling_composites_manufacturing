package models.edd_sched;
import common.model_helpers.*;
import java.util.*;
import java.io.*;

public class EDDSched {

    public static ArrayList<Activity> schedule(ArrayList<AutoBatchA> b2objs) {

        // Copy autoclave batch list
        ArrayList<AutoBatchA> iterate = new ArrayList<>(b2objs);

        // Reset activities
        int activityIt = 0;
        for (AutoBatchA a : b2objs) {
            activityIt = a.createActivity(activityIt);
            for (ToolBatchA t : a.b1sA()) {
                activityIt = t.createActivities(activityIt);
                t.linkActivities(a);
            }
            a.autoAct().setBatch(a);
            a.linkActivities();
        }

        // Reset horizons
        ArrayList<Horizon> resetHorizons = new ArrayList<>();
        for (AutoBatchA a : b2objs) {
            if (!resetHorizons.contains(a.autoMachineH().horizon())) {
                a.autoMachineH().resetHorizon();
                resetHorizons.add(a.autoMachineH().horizon());
            }
            for (ToolBatchA t : a.b1sA()) {
                if (!resetHorizons.contains(t.prepMachineH().horizon())) {
                    t.prepMachineH().resetHorizon();
                    resetHorizons.add(t.prepMachineH().horizon());
                }
                if (!resetHorizons.contains(t.prepLabourH().horizon())) {
                    t.prepLabourH().resetHorizon();
                    resetHorizons.add(t.prepLabourH().horizon());
                }
                if (!resetHorizons.contains(t.layupMachineH().horizon())) {
                    t.layupMachineH().resetHorizon();
                    resetHorizons.add(t.layupMachineH().horizon());
                }
                if (!resetHorizons.contains(t.layupLabourH().horizon())) {
                    t.layupLabourH().resetHorizon();
                    resetHorizons.add(t.layupLabourH().horizon());
                }
                if (!resetHorizons.contains(t.demouldMachineH().horizon())) {
                    t.demouldMachineH().resetHorizon();
                    resetHorizons.add(t.demouldMachineH().horizon());
                }
                if (!resetHorizons.contains(t.demouldLabourH().horizon())) {
                    t.demouldLabourH().resetHorizon();
                    resetHorizons.add(t.demouldLabourH().horizon());
                }
                if (!resetHorizons.contains(t.bottomToolH().horizon())) {
                    t.bottomToolH().resetHorizon();
                    resetHorizons.add(t.bottomToolH().horizon());
                }
            }
        }
        System.out.println("Horizons reset!");

        // Initialize heuristic parameters
        ArrayList<Activity> scheduled = new ArrayList<>();
        ArrayList<Activity> decision = new ArrayList<>();
        ArrayList<Activity> toBeAdded = new ArrayList<>();
        int randomize = 5;

        // While loop to schedule activities
        while (!iterate.isEmpty()) {

            // Add autoclave batch activities to decision and to be added sets
            int choose = (int) Math.round(Math.random() * randomize);
            AutoBatchA currentAuto;
            if (iterate.size() > choose) {
                for (ToolBatchA t : iterate.get(choose).b1sA()) {
                    decision.add(t.prepAct());
                    toBeAdded.add(t.layupAct());
                }
                toBeAdded.add(iterate.get(choose).autoAct());
                for (ToolBatchA t : iterate.get(choose).b1sA()) {
                    toBeAdded.add(t.demouldAct());
                }
                currentAuto = iterate.get(choose);
                iterate.remove(choose);
            } else {
                for (ToolBatchA t : iterate.get(0).b1sA()) {
                    decision.add(t.prepAct());
                    toBeAdded.add(t.layupAct());
                }
                toBeAdded.add(iterate.get(0).autoAct());
                for (ToolBatchA t : iterate.get(0).b1sA()) {
                    toBeAdded.add(t.demouldAct());
                }
                currentAuto = iterate.get(0);
                iterate.remove(0);
            }

            // While loops to schedule all activities
            while (!decision.isEmpty()) {

                // Choose next activity
                Activity next;
                choose = (int) Math.round(Math.random() * randomize);
                if (decision.size() > choose) {
                    next = decision.get(choose);
                    decision.remove(choose);
                } else {
                    next = decision.get(0);
                    decision.remove(0);
                }

                // Schedule next activity and add to active set
                assert next != null;
                if (next.type() == 2) {
                    ArrayList<Horizon> relevantHorizons = new ArrayList<>();
                    for (ToolBatchA t : next.b2().b1sA()) {
                        if (!relevantHorizons.contains(t.bottomToolH().horizon())) {
                            relevantHorizons.add(t.bottomToolH().horizon());
                        }
                    }
                    relevantHorizons.add(next.b2().autoMachineH().horizon());
                    int earliest = Math.max(next.maxPredEnd(), relevantHorizons.stream().map(Horizon::earliest).max(Comparator.comparing(Integer::intValue)).orElseThrow());
                    for (int i = earliest; i < relevantHorizons.get(0).horizonEnd(); i += 5) {
                        int ind = 0;
                        for (Horizon h : relevantHorizons) {
                            int numtools = 0;
                            for (ToolBatchA t : next.b2().b1sA()) {
                                if (t.bottomToolH().horizon().equals(h)) {
                                    numtools++;
                                }
                            }
                            if (numtools == 0) {
                                if (!h.check(i, i + next.length(), 1)) {
                                    ind = 1;
                                    break;
                                }
                            } else {
                                if (!h.check(i, i + next.length(), numtools)) {
                                    ind = 1;
                                    break;
                                }
                            }
                        }
                        if (ind == 0) {
                            for (Horizon h : relevantHorizons) {
                                int numtools = 0;
                                for (ToolBatchA t : next.b2().b1sA()) {
                                    if (t.bottomToolH().horizon().equals(h)) {
                                        numtools++;
                                    }
                                }
                                if (numtools == 0) {
                                    h.schedule(i, i + next.length(), 1);
                                } else {
                                    h.updateHorizonStart((int) Math.ceil((double) (i + next.length()) / 5) * 5 - 5, 0);
                                }
                            }
                            next.schedule(i, i + next.length());
                            break;
                        }
                    }
                } else {
                    ArrayList<Horizon> relevantHorizons = new ArrayList<>();
                    int labourqty = 0;
                    relevantHorizons.add(next.b1().bottomToolH().horizon());
                    if (next.type() == 0) {
                        relevantHorizons.add(next.b1().prepMachineH().horizon());
                        relevantHorizons.add(next.b1().prepLabourH().horizon());
                        labourqty = next.b1().prepQty();
                    } else if (next.type() == 1) {
                        relevantHorizons.add(next.b1().layupMachineH().horizon());
                        relevantHorizons.add(next.b1().layupLabourH().horizon());
                        labourqty = next.b1().layupQty();
                    } else if (next.type() == 3) {
                        relevantHorizons.add(next.b1().demouldMachineH().horizon());
                        relevantHorizons.add(next.b1().demouldLabourH().horizon());
                        labourqty = next.b1().demouldQty();
                    }
                    int earliest = Math.max(next.maxPredEnd(), relevantHorizons.stream().map(Horizon::earliest).max(Comparator.comparing(Integer::intValue)).orElseThrow());
                    for (int i = earliest; i < relevantHorizons.get(0).horizonEnd(); i += 5) {
                        int ind = 0;
                        for (Horizon h : relevantHorizons) {
                            if (relevantHorizons.indexOf(h) == relevantHorizons.size() - 1) {
                                if (!h.check(i, i + next.length(), labourqty)) {
                                    ind = 1;
                                    break;
                                }
                            } else {
                                if (!h.check(i, i + next.length(), 1)) {
                                    ind = 1;
                                    break;
                                }
                            }
                        }
                        if (ind == 0) {
                            if (next.type() == 0) {
                                for (Horizon h : relevantHorizons) {
                                    if (relevantHorizons.indexOf(h) == relevantHorizons.size() - 1) {
                                        h.schedule(i, i + next.length(), labourqty);
                                    } else if (relevantHorizons.indexOf(h) == 0) {
                                        h.schedule(i, i + next.length(), 1);
                                    }
                                }
                            } else if (next.type() == 1) {
                                relevantHorizons.get(0).schedule((int) Math.ceil((double) next.b1().prepAct().end() / 5) * 5, i + next.length(), 1);
                                relevantHorizons.get(1).schedule(i, i + next.length(), 1);
                                relevantHorizons.get(2).schedule(i, i + next.length(), labourqty);
                            } else if (next.type() == 3) {
                                relevantHorizons.get(0).updateHorizonStart((int) Math.ceil((double) (i + next.length()) / 5) * 5 - 5, 0);
                                relevantHorizons.get(1).schedule(i, i + next.length(), 1);
                                relevantHorizons.get(2).schedule(i, i + next.length(), labourqty);
                            }
                            next.schedule(i, i + next.length());
                            break;
                        }
                    }
                }
                scheduled.add(next);

                // Move available jobs from to be added set to decision set
                ArrayList<Activity> toBeRemoved = new ArrayList<>();
                for (Activity a : toBeAdded) {
                    if (scheduled.containsAll(a.predecessors())) {
                        for (Activity pred : a.predecessors()) {
                            if (pred.end() > a.maxPredEnd()) {
                                a.setMaxPredecessorEnd((int) Math.ceil((double) pred.end() / 5) * 5);
                            }
                        }
                        decision.add(a);
                        toBeRemoved.add(a);
                    }
                }
                toBeAdded.removeAll(toBeRemoved);

            }

            // Update tool horizons
            System.out.println("Autoclave batch " + currentAuto.index() + " is scheduled...");

        }

        // Return scheduled batches
        System.out.println("Scheduling complete!");
        return scheduled;

    }

    public static void main(String[] args) throws Exception {

        // Get settings from json file
        String filepath = args[0];
        String sumfile = args[1];
        String interfile = args[2];
        int pi = Integer.parseInt(args[3]);
        int repetitions = Integer.parseInt(args[4]);

        // Make data object and import raw data
        Data data = new Data(filepath, sumfile, pi);
        data.readInstanceParams();
        data.readPackParams();

        // Create instance parameter objects
        ArrayList<Job> jobobjs = new ArrayList<>();
        for (int i = 0; i < data.numJob; i++) {
            Job newjob = new Job(data.jobName[i], data.jobPartFamily[i], data.jobAutoCap[i], i, data.jobSize[i], data.jobDue[i], data.jobSteps[i], data.jobStepTimes[i]);
            jobobjs.add(newjob);
        }
        ArrayList<Tool> toptoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numTopTool; i++) {
            Tool newtool = new Tool(data.topToolName[i], data.topToolCap[i], data.topToolQty[i], data.topToolMin[i], data.horizon);
            toptoolobjs.add(newtool);
        }
        ArrayList<ToolH> bottomtoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numBottomTool; i++) {
            ToolH newtool = new ToolH(data.bottomToolName[i], data.bottomToolCap[i], data.bottomToolQty[i], data.bottomToolMin[i], data.horizon, data.bottomToolSize[i]);
            bottomtoolobjs.add(newtool);
        }
        ArrayList<MachineH> machineobjs = new ArrayList<>();
        for (int i = 0; i < data.numMachine; i++) {
            MachineH newmachine = new MachineH(data.machineName[i], data.machineShift[i], data.shiftStart, data.shiftEnd, data.horizon);
            machineobjs.add(newmachine);
        }
        ArrayList<LabourH> labourobjs = new ArrayList<>();
        for (int i = 0; i < data.numLabour; i++) {
            ArrayList<String> skills = new ArrayList<>();
            for (int j = 0; j < data.numLabourSkill; j++) {
                if (data.labourMatrix[i][j] == 1) {
                    skills.add(data.labourSkillName[j]);
                }
            }
            LabourH newlabour = new LabourH(data.labourName[i], skills, data.labourShift[i], data.shiftStart, data.shiftEnd, data.horizon);
            labourobjs.add(newlabour);
        }
        labourobjs.add(new LabourH("N/A", null, null, null, null, 0));

        // Create batch objects
        ArrayList<TopBatch> b0objs = new ArrayList<>();
        ArrayList<ToolBatchA> b1objs = new ArrayList<>();
        ArrayList<AutoBatchA> b2objs = new ArrayList<>();
        for (int i = 0; i < data.numB0_new; i++) {
            for (Tool t : toptoolobjs) {
                if (data.b0TopTool[i].equals(t.name())) {
                    TopBatch newtop = new TopBatch(t, data.b0Indices[i]);
                    for (int j = 0; j < data.numJob; j++) {
                        if (data.jobToB0[j] == data.b0Indices[i]) {
                            newtop.addJob(jobobjs.get(data.jobIndices[j]));
                            jobobjs.get(data.jobIndices[j]).setTopBatch(newtop);
                        }
                    }
                    b0objs.add(newtop);
                }
            }
        }
        b0objs.sort(Comparator.comparing(TopBatch::index));
        for (int i = 0; i < data.numB1_new; i++) {
            for (ToolH t : bottomtoolobjs) {
                if (data.b1BottomTool[i].equals(t.name())) {
                    ToolBatchA newtool = new ToolBatchA(t, data.b1Indices[i]);
                    for (int j = 0; j < data.numB0_new; j++) {
                        if (data.b0ToB1[j] == data.b1Indices[i]) {
                            newtool.addTopBatch(b0objs.get(data.b0Indices[j]));
                            b0objs.get(data.b0Indices[j]).setToolBatch(newtool);
                        }
                    }
                    b1objs.add(newtool);
                }
            }
        }
        b1objs.sort(Comparator.comparing(ToolBatchA::index));
        for (int i = 0; i < data.numB2_new; i++) {
            AutoBatchA newauto = new AutoBatchA(data.b2Cap[i], data.b2Indices[i]);
            for (int j = 0; j < data.numB1_new; j++) {
                if (data.b1ToB2[j] == data.b2Indices[i]) {
                    newauto.addToolBatchA(b1objs.get(data.b1Indices[j]));
                }
            }
            b2objs.add(newauto);
        }

        // Set activity parameters
        for (AutoBatchA a : b2objs) {
            for (ToolBatchA t : a.b1sA()) {
                for (int i = 0; i < data.stepToMachine.length; i++) {
                    if (t.jobs().get(0).steps()[0].equals(data.stepToMachine[i][1])) {
                        for (MachineH m : machineobjs) {
                            if (m.name().equals(data.stepToMachine[i][0])) {
                                t.setPrepMachineH(m);
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.stepToMachine.length; i++) {
                    if (t.jobs().get(0).steps()[1].equals(data.stepToMachine[i][1])) {
                        for (MachineH m : machineobjs) {
                            if (m.name().equals(data.stepToMachine[i][0])) {
                                t.setLayupMachineH(m);
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.stepToMachine.length; i++) {
                    if (t.jobs().get(0).steps()[3].equals(data.stepToMachine[i][1])) {
                        for (MachineH m : machineobjs) {
                            if (m.name().equals(data.stepToMachine[i][0])) {
                                t.setDemouldMachineH(m);
                                break;
                            }
                        }
                        break;
                    }
                }
                int pind = 0;
                int lind = 0;
                int dind = 0;
                for (int i = 0; i < data.labourToMachine.length; i++) {
                    if (t.prepMachineH().name().equals(data.labourToMachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourToMachine[i][1]) ||
                                    data.labourToMachine[i][1].equals("N/A"))) {
                        for (LabourH l : labourobjs) {
                            if (l.skills().contains(data.labourToMachine[i][2])) {
                                t.setPrepLabourH(l, data.labourQtyRequired[i]);
                                pind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.labourToMachine.length; i++) {
                    if (t.layupMachineH().name().equals(data.labourToMachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourToMachine[i][1]) ||
                                    data.labourToMachine[i][1].equals("N/A"))) {
                        for (LabourH l : labourobjs) {
                            if (l.skills().contains(data.labourToMachine[i][2])) {
                                t.setLayupLabourH(l, data.labourQtyRequired[i]);
                                lind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.labourToMachine.length; i++) {
                    if (t.demouldMachineH().name().equals(data.labourToMachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourToMachine[i][1]) ||
                                    data.labourToMachine[i][1].equals("N/A"))) {
                        for (LabourH l : labourobjs) {
                            if (l.skills().contains(data.labourToMachine[i][2])) {
                                t.setDemouldLabourH(l, data.labourQtyRequired[i]);
                                dind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                if (pind == 0) {
                    t.setPrepLabourH(labourobjs.get(labourobjs.size() - 1), 0);
                }
                if (lind == 0) {
                    t.setLayupLabourH(labourobjs.get(labourobjs.size() - 1), 0);
                }
                if (dind == 0) {
                    t.setDemouldLabourH(labourobjs.get(labourobjs.size() - 1), 0);
                }
            }
            for (int i = 0; i < data.stepToMachine.length; i++) {
                if (a.jobs().get(0).steps()[2].equals(data.stepToMachine[i][1])) {
                    for (MachineH m : machineobjs) {
                        if (m.name().equals(data.stepToMachine[i][0])) {
                            a.setAutoMachineH(m);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        // Create file to write solutions to
        FileWriter writer = new FileWriter(interfile, true);
        String initialtext = "Problem Instance: " + pi + "\n";
        writer.write(initialtext);

        // Run heuristic
        double starttime = System.nanoTime();
        SolutionSched best = null;
        b2objs.sort(Comparator.comparing(AutoBatchA::due));
        for (int i = 0; i < repetitions; i++) {
            System.out.println("Starting scheduling heuristic...repetition: " + i);
            ArrayList<Activity> scheduled = schedule(b2objs);
            double endtime = System.nanoTime();
            double elapsedtime = (endtime - starttime) / 1_000_000_000;
            int objval = 0;
            for (Activity a : scheduled) {
                if (a.type() == 3) {
                    for (Job j : a.b1().jobs()) {
                        objval += Math.max(0, a.end() - j.due());
                    }
                }
            }
            if (i == 0) {
                System.out.println(objval);
                best = new SolutionSched(data.numJob, objval, elapsedtime, "Feasible");
                best.addActivities(scheduled);
            } else {
                if (objval < best.objVal()) {
                    System.out.println(objval);
                    best = new SolutionSched(data.numJob, objval, elapsedtime, "Feasible");
                    best.addActivities(scheduled);
                }
            }
            writer.write("EDD scheduling solution found after " + elapsedtime + " seconds with objective value " + objval + "\n");
        }

        // Write solution to file
        assert best != null;
        best.replaceTime((System.nanoTime() - starttime) / 1_000_000_000);
        data.writeSched(best);
        data.writeSum(best);

        // Close solution file
        writer.close();

    }

}