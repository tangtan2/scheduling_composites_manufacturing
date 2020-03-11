package models.edd_serial_sched;
import common.model_helpers.*;
import java.util.*;
import java.io.*;

public class EDDSerialSched {

    public static ArrayList<Activity> serial(ArrayList<AutoBatchA> b2objs) {

        // Sort autoclave batches by due date
        ArrayList<AutoBatchA> iterate = new ArrayList<>(b2objs);
        iterate.sort(Comparator.comparing(AutoBatchA::due));

        // Reset activities
        int activityIt = 0;
        for (AutoBatchA a : b2objs) {
            activityIt = a.createActivity(activityIt);
            for (ToolBatchA t : a.b1sA()) {
                activityIt = t.createActivities(activityIt);
                t.setAutoBatchA(a);
                t.prepAct().setBatches(t, a);
                t.layupAct().setBatches(t, a);
                t.demouldAct().setBatches(t, a);
            }
            a.autoAct().setBatches(a);
            a.linkAct();
        }

        // Reset horizons
        for (AutoBatchA a : b2objs) {
            a.autoMachineH().resetHorizon();
            for (ToolBatchA t : a.b1sA()) {
                t.prepMachineH().resetHorizon();
                t.prepLabourH().resetHorizon();
                t.layupMachineH().resetHorizon();
                t.layupLabourH().resetHorizon();
                t.demouldMachineH().resetHorizon();
                t.demouldLabourH().resetHorizon();
                t.bottomToolH().resetHorizon();
            }
        }

        // Initialize heuristic parameters
        ArrayList<Activity> scheduled = new ArrayList<>();
        Queue<Activity> decision = new LinkedList<>();
        ArrayList<Activity> toBeAdded = new ArrayList<>();
        int randomize = 3;

        // While loop to schedule jobs
        while (!(iterate.isEmpty() && toBeAdded.isEmpty() && decision.isEmpty())) {

            // Update decision set
            if (toBeAdded.isEmpty() && decision.isEmpty()) {
                int choose = (int) Math.round(Math.random() * randomize);
                if (iterate.size() > choose) {
                    for (ToolBatchA t : iterate.get(choose).b1sA()) {
                        decision.add(t.prepAct());
                        toBeAdded.add(t.layupAct());
                    }
                    toBeAdded.add(iterate.get(choose).autoAct());
                    for (ToolBatchA t : iterate.get(choose).b1sA()) {
                        toBeAdded.add(t.demouldAct());
                    }
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
                    iterate.remove(0);
                }
            } else {
                ArrayList<Activity> toBeRemoved = new ArrayList<>();
                for (Activity a : toBeAdded) {
                    if (scheduled.containsAll(a.predecessors())) {
                        for (Activity pred : a.predecessors()) {
                            if (pred.end() > a.maxPredEnd()) {
                                a.setMaxPred((int) Math.ceil((double) pred.end() / 5) * 5);
                            }
                        }
                        decision.add(a);
                        toBeRemoved.add(a);
                    }
                }
                toBeAdded.removeAll(toBeRemoved);
            }

            // Choose next activity to schedule
            Activity next = decision.poll();

            // Schedule chosen activity and add to scheduled set
            assert next != null;
            if (next.type() == 2) {
                ArrayList<Horizon> relevanthorizons = new ArrayList<>();
                for (ToolBatchA t : next.b2().b1sA()) {
                    if (!relevanthorizons.contains(t.bottomToolH().horizon())) {
                        relevanthorizons.add(t.bottomToolH().horizon());
                    }
                }
                relevanthorizons.add(next.b2().autoMachineH().horizon());
                int earliest = Math.max(next.maxPredEnd(), relevanthorizons.stream().map(Horizon::earliest).max(Comparator.comparing(Integer::intValue)).orElseThrow());
                for (int i = earliest; i < relevanthorizons.get(0).horizonEnd(); i += 5) {
                    int ind = 0;
                    for (Horizon h : relevanthorizons) {
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
                        for (Horizon h : relevanthorizons) {
                            int numtools = 0;
                            for (ToolBatchA t : next.b2().b1sA()) {
                                if (t.bottomToolH().horizon().equals(h)) {
                                    numtools++;
                                }
                            }
                            if (numtools == 0) {
                                h.schedule(i, i + next.length(), 1);
                            } else {
                                h.schedule(i, i + next.length(), numtools);
                            }
                        }
                        next.schedule(i, i + next.length());
                        break;
                    }
                }
            } else {
                ArrayList<Horizon> relevanthorizons = new ArrayList<>();
                int labourqty = 0;
                relevanthorizons.add(next.b1().bottomToolH().horizon());
                if (next.type() == 0) {
                    relevanthorizons.add(next.b1().prepMachineH().horizon());
                    relevanthorizons.add(next.b1().prepLabourH().horizon());
                    labourqty = next.b1().prepQty();
                } else if (next.type() == 1) {
                    relevanthorizons.add(next.b1().layupMachineH().horizon());
                    relevanthorizons.add(next.b1().layupLabourH().horizon());
                    labourqty = next.b1().layupQty();
                } else if (next.type() == 3) {
                    relevanthorizons.add(next.b1().demouldMachineH().horizon());
                    relevanthorizons.add(next.b1().demouldLabourH().horizon());
                    labourqty = next.b1().demouldQty();
                }
                int earliest = Math.max(next.maxPredEnd(), relevanthorizons.stream().map(Horizon::earliest).max(Comparator.comparing(Integer::intValue)).orElseThrow());
                for (int i = earliest; i < relevanthorizons.get(0).horizonEnd(); i += 5) {
                    int ind = 0;
                    for (Horizon h : relevanthorizons) {
                        if (relevanthorizons.indexOf(h) == relevanthorizons.size() - 1) {
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
                        for (Horizon h : relevanthorizons) {
                            if (relevanthorizons.indexOf(h) == relevanthorizons.size() - 1) {
                                h.schedule(i, i + next.length(), labourqty);
                            } else {
                                h.schedule(i, i + next.length(), 1);
                            }
                        }
                        next.schedule(i, i + next.length());
                        break;
                    }
                }
            }
            scheduled.add(next);

        }

        // Return scheduled batches
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
        for (int i = 0; i < data.numjob; i++) {
            Job newjob = new Job(data.jobname[i], data.jobpartfamily[i], data.autocapperjob[i], i, data.sj[i], data.dj[i], data.jobsteps[i], data.jobsteptimes[i]);
            jobobjs.add(newjob);
        }
        ArrayList<Tool> toptoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numtop; i++) {
            Tool newtool = new Tool(data.topname[i], data.topcap[i], data.topqty[i], data.topmin[i], data.horizon);
            toptoolobjs.add(newtool);
        }
        ArrayList<ToolH> bottomtoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numbottom; i++) {
            ToolH newtool = new ToolH(data.bottomname[i], data.bottomcap[i], data.bottomqty[i], data.bottommin[i], data.horizon, data.bottomsize[i]);
            bottomtoolobjs.add(newtool);
        }
        ArrayList<MachineH> machineobjs = new ArrayList<>();
        for (int i = 0; i < data.nummachine; i++) {
            MachineH newmachine = new MachineH(data.machinename[i], data.machineshift[i], data.shiftstart, data.shiftend, data.horizon);
            machineobjs.add(newmachine);
        }
        ArrayList<LabourH> labourobjs = new ArrayList<>();
        for (int i = 0; i < data.numlabourteam; i++) {
            ArrayList<String> skills = new ArrayList<>();
            for (int j = 0; j < data.numlabourskill; j++) {
                if (data.labourmatrix[i][j] == 1) {
                    skills.add(data.labourskillname[j]);
                }
            }
            LabourH newlabour = new LabourH(data.labourteamname[i], skills, data.labourshift[i], data.shiftstart, data.shiftend, data.horizon);
            labourobjs.add(newlabour);
        }
        labourobjs.add(new LabourH("N/A", null, null, null, null, 0));

        // Create batch objects
        ArrayList<TopBatch> b0objs = new ArrayList<>();
        ArrayList<ToolBatchA> b1objs = new ArrayList<>();
        ArrayList<AutoBatchA> b2objs = new ArrayList<>();
        for (int i = 0; i < data.numb0_new; i++) {
            for (Tool t : toptoolobjs) {
                if (data.b0toptool[i].equals(t.name())) {
                    TopBatch newtop = new TopBatch(t, data.b0indices[i]);
                    for (int j = 0; j < data.numjob; j++) {
                        if (data.jobtob0[j] == data.b0indices[i]) {
                            newtop.addJob(jobobjs.get(data.jobindices[j]));
                            jobobjs.get(data.jobindices[j]).setTopBatch(newtop);
                        }
                    }
                    b0objs.add(newtop);
                }
            }
        }
        b0objs.sort(Comparator.comparing(TopBatch::index));
        for (int i = 0; i < data.numb1_new; i++) {
            for (ToolH t : bottomtoolobjs) {
                if (data.b1bottomtool[i].equals(t.name())) {
                    ToolBatchA newtool = new ToolBatchA(t, data.b1indices[i]);
                    for (int j = 0; j < data.numb0_new; j++) {
                        if (data.b0tob1[j] == data.b1indices[i]) {
                            newtool.addTopBatch(b0objs.get(data.b0indices[j]));
                            b0objs.get(data.b0indices[j]).setToolBatch(newtool);
                        }
                    }
                    b1objs.add(newtool);
                }
            }
        }
        b1objs.sort(Comparator.comparing(ToolBatchA::index));
        for (int i = 0; i < data.numb2_new; i++) {
            AutoBatchA newauto = new AutoBatchA(data.b2cap[i], data.b2indices[i]);
            for (int j = 0; j < data.numb1_new; j++) {
                if (data.b1tob2[j] == data.b2indices[i]) {
                    newauto.addToolBatchA(b1objs.get(data.b1indices[j]));
                }
            }
            b2objs.add(newauto);
        }

        // Set activity parameters
        for (AutoBatchA a : b2objs) {
            for (ToolBatchA t : a.b1sA()) {
                for (int i = 0; i < data.batchtomachine.length; i++) {
                    if (t.jobs().get(0).steps()[0].equals(data.batchtomachine[i][1])) {
                        for (MachineH m : machineobjs) {
                            if (m.name().equals(data.batchtomachine[i][0])) {
                                t.setPrepMachineH(m);
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.batchtomachine.length; i++) {
                    if (t.jobs().get(0).steps()[1].equals(data.batchtomachine[i][1])) {
                        for (MachineH m : machineobjs) {
                            if (m.name().equals(data.batchtomachine[i][0])) {
                                t.setLayupMachineH(m);
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.batchtomachine.length; i++) {
                    if (t.jobs().get(0).steps()[3].equals(data.batchtomachine[i][1])) {
                        for (MachineH m : machineobjs) {
                            if (m.name().equals(data.batchtomachine[i][0])) {
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
                for (int i = 0; i < data.labourtomachine.length; i++) {
                    if (t.prepMachineH().name().equals(data.labourtomachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourtomachine[i][1]) ||
                                    data.labourtomachine[i][1].equals("N/A"))) {
                        for (LabourH l : labourobjs) {
                            if (l.skills().contains(data.labourtomachine[i][2])) {
                                t.setPrepLabourH(l, data.labourqtyrequired[i]);
                                pind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.labourtomachine.length; i++) {
                    if (t.layupMachineH().name().equals(data.labourtomachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourtomachine[i][1]) ||
                                    data.labourtomachine[i][1].equals("N/A"))) {
                        for (LabourH l : labourobjs) {
                            if (l.skills().contains(data.labourtomachine[i][2])) {
                                t.setLayupLabourH(l, data.labourqtyrequired[i]);
                                lind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.labourtomachine.length; i++) {
                    if (t.demouldMachineH().name().equals(data.labourtomachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourtomachine[i][1]) ||
                                    data.labourtomachine[i][1].equals("N/A"))) {
                        for (LabourH l : labourobjs) {
                            if (l.skills().contains(data.labourtomachine[i][2])) {
                                t.setDemouldLabourH(l, data.labourqtyrequired[i]);
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
            for (int i = 0; i < data.batchtomachine.length; i++) {
                if (a.jobs().get(0).steps()[2].equals(data.batchtomachine[i][1])) {
                    for (MachineH m : machineobjs) {
                        if (m.name().equals(data.batchtomachine[i][0])) {
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
        ArrayList<Integer> allObjs = new ArrayList<>();
        SolutionSched best = null;
        for (int i = 0; i < repetitions; i++) {
            ArrayList<Activity> scheduled = serial(b2objs);
            double endtime = System.nanoTime();
            double elapsedtime = (endtime - starttime) / 1_000_000_000;
            int objval = 0;
            for (Activity b2 : scheduled) {
                if (b2.type() == 3) {
                    for (Job j : b2.b1().jobs()) {
                        objval += Math.max(0, b2.end() - j.due());
                    }
                }
            }
            if (i == 0) {
                allObjs.add(objval);
                best = new SolutionSched(data.numjob, objval, elapsedtime, "Feasible");
                best.addActivities(scheduled);
            } else {
                allObjs.sort(Comparator.comparing(Integer::intValue));
                if (objval < allObjs.get(0)) {
                    best = new SolutionSched(data.numjob, objval, elapsedtime, "Feasible");
                    best.addActivities(scheduled);
                }
            }
            writer.write("Heuristic scheduling solution found after " + elapsedtime + " seconds with objective value " + objval + "\n");
        }

        // Write solution to file
        assert best != null;
        data.writeSched(best);
        data.writeSum(best);

        // Close solution file
        writer.close();

    }

}
