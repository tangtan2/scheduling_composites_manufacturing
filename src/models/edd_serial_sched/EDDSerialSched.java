package models.edd_serial_sched;
import common.model_helpers.*;
import java.util.*;
import java.io.*;
import org.apache.commons.lang3.SerializationUtils;

public class EDDSerialSched {

    public static ArrayList<AutoBatchA> serial(ArrayList<AutoBatchA> b2objs) {

        // Sort autoclave batches by due date
        ArrayList<AutoBatchA> iterate = new ArrayList<>(b2objs);
        iterate.sort(Comparator.comparing(AutoBatchA::due));

        // Return scheduled batches
        return b2objs;

    }

    public static ArrayList<AutoBatchA> parallel(ArrayList<AutoBatchA> b2objs) {

        // Sort autoclave batches by due date
        ArrayList<AutoBatchA> iterate = new ArrayList<>(b2objs);
        iterate.sort(Comparator.comparing(AutoBatchA::due));

        // Return scheduled batches
        return b2objs;

    }

    public static void main(String[] args) throws Exception {

        // Get settings from json file
        // TBD: Parse JSON information
        String filepath = "";
        String sumfile = "";
        String interfile = "";
        int pi = 0;
        int heuristic = Integer.parseInt(args[0]);
        int repetitions = 10;

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
        ArrayList<ToolH> toptoolobjs = new ArrayList<>();
        for (int i = 0; i < data.numtop; i++) {
            ToolH newtool = new ToolH(data.topname[i], data.topcap[i], data.topqty[i], data.topmin[i], data.horizon);
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
            for (Tool t : bottomtoolobjs) {
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

        // Run chosen heuristic
        double starttime = System.nanoTime();
        ArrayList<Integer> allObjs = new ArrayList<>();
        SolutionSched best = null;
        if (heuristic == 1) {
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
        } else if (heuristic == 2) {
            for (int i = 0; i < repetitions; i++) {
                ArrayList<AutoBatchA> scheduled = serial(SerializationUtils.clone(b2objs));
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
        }

        // Write solution to file
        data.writeSched(best);
        data.writeSum(best);

        // Close solution file
        writer.close();

    }

}
