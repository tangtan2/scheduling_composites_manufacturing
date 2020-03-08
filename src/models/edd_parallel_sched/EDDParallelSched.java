package models.edd_parallel_sched;
import common.model_helpers.*;
import java.util.*;
import java.io.*;
import org.apache.commons.lang3.SerializationUtils;

public class EDDParallelSched {

    public static ArrayList<AutoBatchA> parallel(ArrayList<AutoBatchA> b2objs) {

        // Sort autoclave batches by due date
        ArrayList<AutoBatchA> iterate = new ArrayList<>(b2objs);
        iterate.sort(Comparator.comparing(AutoBatchA::due));

        // Reset horizons
        for (AutoBatchA a : b2objs) {
            a.autoMachine().resetHorizon();
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
        ArrayList<Activity> completed = new ArrayList<>();
        ArrayList<Activity> active = new ArrayList<>();
        Queue<Activity> decision = new LinkedList<>();
        Queue<Activity> toBeAdded = new LinkedList<>();
        int randomize = 3;

        // While loop to schedule jobs
        while (!(iterate.isEmpty() && toBeAdded.isEmpty())) {

            // Update schedule time and move jobs from active set to completed set
            if (active.size() > 0) {
                active.sort(Comparator.comparing(Activity::end));
                int scheduletime = active.get(0).end();
                for (Activity a : active) {
                    if (a.end() <= scheduletime) {
                        completed.add(a);
                        active.remove(a);
                    }
                }
            }

            // Update decision set
            if (toBeAdded.isEmpty() && iterate.size() > 0) {
                int choose = (int) Math.round(Math.random() * randomize);
                if (iterate.size() >= choose - 1) {
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
                for (Activity a : toBeAdded) {
                    ArrayList<Activity> union = new ArrayList<>();
                    union.addAll(completed);
                    union.addAll(active);
                    if (union.containsAll(a.predecessors())) {
                        for (Activity pred : a.predecessors()) {
                            if (pred.end() > a.maxPredEnd()) {
                                a.setMaxPred((int) Math.ceil((double) pred.end() / 5) * 5);
                            }
                        }
                        decision.add(a);
                        toBeAdded.remove(a);
                    }
                }
            }

            // While loop to schedule all jobs in decision set
            while (!decision.isEmpty()) {

                // Choose next activity
                Activity next = decision.poll();

                // Schedule next activity and add to active set
                assert next != null;
                if (next.b1() == null) {
                    ArrayList<Horizon> relevanthorizons = new ArrayList<>();
                    for (ToolBatchA t : next.b2().b1sA()) {
                        relevanthorizons.add(t.bottomToolH().horizon());
                    }
                    relevanthorizons.add(next.b2().autoMachine().horizon());
                    for (int i = next.maxPredEnd(); i < relevanthorizons.get(0).horizonEnd(); i += 5) {
                        int ind = 0;
                        for (Horizon h : relevanthorizons) {
                            if (!h.check(i, i + next.length(), 1)) {
                                ind = 1;
                                break;
                            }
                        }
                        if (ind == 0) {
                            for (Horizon h : relevanthorizons) {
                                h.schedule(i, i + next.length(), 1);
                            }
                            next.b2().autoSched(i, i + next.length());
                            break;
                        }
                    }
                } else if (next.b2() == null) {
                    ArrayList<Horizon> relevanthorizons = new ArrayList<>();
                    int labourqty = 0;
                    relevanthorizons.add(next.b1().bottomToolH().horizon());
                    if (next.name().contains("Prep")) {
                        relevanthorizons.add(next.b1().prepMachineH().horizon());
                        relevanthorizons.add(next.b1().prepLabourH().horizon());
                        labourqty = next.b1().prepQty();
                    } else if (next.name().contains("Lay")) {
                        relevanthorizons.add(next.b1().layupMachineH().horizon());
                        relevanthorizons.add(next.b1().layupLabourH().horizon());
                        labourqty = next.b1().layupQty();
                    } else if (next.name().contains("Demould")) {
                        relevanthorizons.add(next.b1().demouldMachineH().horizon());
                        relevanthorizons.add(next.b1().demouldLabourH().horizon());
                        labourqty = next.b1().demouldQty();
                    }
                    for (int i = next.maxPredEnd(); i < relevanthorizons.get(0).horizonEnd(); i += 5) {
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
                            if (next.name().contains("Prep")) {
                                next.b1().prepSched(i, i + next.length());
                            } else if (next.name().contains("Lay")) {
                                next.b1().layupSched(i, i + next.length());
                            } else if (next.name().contains("Demould")) {
                                next.b1().demouldSched(i, i + next.length());
                            }
                            break;
                        }
                    }
                }
                active.add(next);

            }

        }

        // Schedule jobs
        for (AutoBatchA a : b2objs) {
            for (ToolBatchA t : a.b1sA()) {
                t.jobs().sort(Comparator.comparing(Job::due));
                int prepstart = t.prepStart();
                int layupstart = t.layupStart();
                int autostart = a.autoStart();
                int autoend = a.autoEnd();
                int demouldstart = t.demouldStart();
                for (Job j : t.jobs()) {
                    j.schedule(prepstart, prepstart + j.stepTimes()[0],
                            layupstart, layupstart + j.stepTimes()[1],
                            autostart, autoend,
                            demouldstart, demouldstart + j.stepTimes()[3]);
                    prepstart += j.stepTimes()[0];
                    layupstart += j.stepTimes()[1];
                    demouldstart += j.stepTimes()[3];
                }
            }
        }

        // Return scheduled batches
        return b2objs;

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
                    TopBatch newtop = new TopBatch(t);
                    for (int j = 0; j < data.numjob; j++) {
                        if (data.jobtob0[j] == i) {
                            newtop.addJob(jobobjs.get(j));
                            jobobjs.get(j).setTopBatch(newtop);
                        }
                    }
                    b0objs.add(newtop);
                }
            }
        }
        for (int i = 0; i < data.numb1_new; i++) {
            for (ToolH t : bottomtoolobjs) {
                if (data.b1bottomtool[i].equals(t.name())) {
                    ToolBatchA newtool = new ToolBatchA(t);
                    for (int j = 0; j < data.numb0_new; j++) {
                        if (data.b0tob1[j] == i) {
                            newtool.addTopBatch(b0objs.get(j));
                            b0objs.get(j).setToolBatch(newtool);
                        }
                    }
                    b1objs.add(newtool);
                }
            }
        }
        for (int i = 0; i < data.numb2_new; i++) {
            AutoBatchA newauto = new AutoBatchA(data.b2cap[i]);
            for (int j = 0; j < data.numb1_new; j++) {
                if (data.b1tob2[j] == i) {
                    newauto.addToolBatchA(b1objs.get(j));
                    b1objs.get(j).setAutoBatchA(newauto);
                }
            }
            b2objs.add(newauto);
        }

        // Create activity objects
        for (ToolBatchA t : b1objs) {
            t.createActivities();
        }
        for (AutoBatchA a : b2objs) {
            a.createActivity();
        }

        // Set activity parameters
        for (AutoBatchA a : b2objs) {
            for (ToolBatchA t : a.b1sA()) {
                for (int i = 0; i < data.batchtomachine.length; i++) {
                    if (t.jobs().get(0).steps()[0].equals(data.batchtomachine[i][1])) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.batchtomachine[i][0])) {
                                t.setPrepMachine(m);
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.batchtomachine.length; i++) {
                    if (t.jobs().get(0).steps()[1].equals(data.batchtomachine[i][1])) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.batchtomachine[i][0])) {
                                t.setLayupMachine(m);
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.batchtomachine.length; i++) {
                    if (t.jobs().get(0).steps()[3].equals(data.batchtomachine[i][1])) {
                        for (Machine m : machineobjs) {
                            if (m.name().equals(data.batchtomachine[i][0])) {
                                t.setDemouldMachine(m);
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
                    if (t.prepMachine().name().equals(data.labourtomachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourtomachine[i][1]) ||
                                    data.labourtomachine[i][1].equals("N/A"))) {
                        for (Labour l : labourobjs) {
                            if (l.name().equals(data.labourtomachine[i][2])) {
                                t.setPrepLabour(l, data.labourqtyrequired[i]);
                                pind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.labourtomachine.length; i++) {
                    if (t.layupMachine().name().equals(data.labourtomachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourtomachine[i][1]) ||
                                    data.labourtomachine[i][1].equals("N/A"))) {
                        for (Labour l : labourobjs) {
                            if (l.name().equals(data.labourtomachine[i][2])) {
                                t.setLayupLabour(l, data.labourqtyrequired[i]);
                                lind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                for (int i = 0; i < data.labourtomachine.length; i++) {
                    if (t.demouldMachine().name().equals(data.labourtomachine[i][0]) &&
                            (t.jobs().get(0).partFamily().equals(data.labourtomachine[i][1]) ||
                                    data.labourtomachine[i][1].equals("N/A"))) {
                        for (Labour l : labourobjs) {
                            if (l.name().equals(data.labourtomachine[i][2])) {
                                t.setDemouldLabour(l, data.labourqtyrequired[i]);
                                dind = 1;
                                break;
                            }
                        }
                        break;
                    }
                }
                if (pind == 0) {
                    t.setPrepLabour(labourobjs.get(labourobjs.size() - 1), 0);
                }
                if (lind == 0) {
                    t.setLayupLabour(labourobjs.get(labourobjs.size() - 1), 0);
                }
                if (dind == 0) {
                    t.setDemouldLabour(labourobjs.get(labourobjs.size() - 1), 0);
                }
            }
            for (int i = 0; i < data.batchtomachine.length; i++) {
                if (a.jobs().get(0).steps()[2].equals(data.batchtomachine[i][1])) {
                    for (Machine m : machineobjs) {
                        if (m.name().equals(data.batchtomachine[i][0])) {
                            a.setAutoMachine(m);
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
            ArrayList<AutoBatchA> scheduled = parallel(SerializationUtils.clone(b2objs));
            double endtime = System.nanoTime();
            double elapsedtime = (endtime - starttime) / 1_000_000_000;
            int objval = 0;
            for (AutoBatchA b2 : scheduled) {
                objval += b2.capacity() - b2.sumToolSize();
                for (Job j : b2.jobs()) {
                    objval += j.tardiness();
                }
            }
            allObjs.sort(Comparator.comparing(Integer::intValue));
            if (objval < allObjs.get(0)) {
                best =  new SolutionSched(data.numjob, objval, elapsedtime, "Feasible");
                best.addAutoBatch(new ArrayList<>(b2objs));
            }
            writer.write("Heuristic scheduling solution found after " + elapsedtime + " seconds with objective value " + objval + "\n");
        }

        // Write solution to file
        data.writeSched(best);
        data.writeSum(best);

        // Close solution file
        writer.close();

    }

}
