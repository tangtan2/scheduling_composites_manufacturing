package models.basic_edd_pack;
import common.model_helpers.*;
import java.util.*;
import java.io.*;

public class BasicEDDPack {

    // Static function to run heuristic
    public static ArrayList<AutoBatch> run(ArrayList<Job> jobobjs) {

        // Create lists to hold batch objects
        ArrayList<ToolBatch> b1objs = new ArrayList<>();
        ArrayList<AutoBatch> b2objs = new ArrayList<>();
        int randomize = 3;

        // Sort jobs in order of due date then order of rsp
        jobobjs.sort(Comparator.comparing(Job::rspOrder));
        ArrayList<Job> iterate = new ArrayList<>();
        int due = 7 * 24 * 60;
        while (iterate.size() < jobobjs.size()) {
            for (Job j : jobobjs) {
                if (j.due() == due) {
                    iterate.add(j);
                }
            }
            due += 7 * 24 * 60;
        }

        // Pack jobs into tool batches
        while (!iterate.isEmpty()) {

            // Choose next job
            int choose = (int) Math.round(Math.random() * randomize);
            Job currentJob;
            ArrayList<Job> associatedJobs = new ArrayList<>();
            if (iterate.size() > choose) {
                currentJob = iterate.get(choose);
            } else {
                currentJob = iterate.get(0);
            }
            int sizeComp = currentJob.mappedCombos().get(0).size();
            int qtyComp = currentJob.mappedCombos().get(0).qty();
            ToolCombo best = currentJob.mappedCombos().get(0);
            for (ToolCombo c : currentJob.mappedCombos()) {
                if (c.size() < sizeComp) {
                    sizeComp = c.size();
                    qtyComp = c.qty();
                    best = c;
                } else if (c.size() == sizeComp) {
                    if (c.qty() > qtyComp) {
                        qtyComp = c.qty();
                        best = c;
                    }
                }
            }

            // Batch into tool batch
            ToolBatch newb1 = new ToolBatch(best.bottom());
            int maxjob = best.top().capacity() * best.bottom().capacity();
            for (Job j : iterate) {
                if (j.mappedCombos().contains(best)) {
                    associatedJobs.add(j);
                }
                if (associatedJobs.size() == maxjob) {
                    break;
                }
            }
            iterate.removeAll(associatedJobs);
            while (!associatedJobs.isEmpty()) {
                TopBatch newtop = new TopBatch(best.top());
                newtop.setToolBatch(newb1);
                if (associatedJobs.size() >= best.top().capacity()) {
                    newtop.addJob(associatedJobs.subList(0, best.top().capacity() - 1));
                } else {
                    newtop.addJob(associatedJobs);
                }
                newb1.addTopBatch(newtop);
                associatedJobs.removeAll(newtop.jobs());
            }
            b1objs.add(newb1);

        }

        // Pack tool batches into autoclave batches
        ArrayList<ToolBatch> iterate2 = new ArrayList<>(b1objs);
        while (!iterate2.isEmpty()) {

            // Choose next tool battch
            int choose = (int) Math.round(Math.random() * randomize);
            ToolBatch currentTool;
            if (iterate2.size() > choose) {
                currentTool = iterate2.get(choose);
            } else {
                currentTool = iterate2.get(0);
            }

            // Batch into autoclave batch
            AutoBatch newauto = new AutoBatch(currentTool.jobs().get(0).autoCap());
            newauto.addToolBatch(currentTool);
            currentTool.setAutoBatch(newauto);
            for (ToolBatch t : iterate2) {
                if (newauto.sumToolSize() + t.size() <= newauto.capacity()) {
                    if (t.jobs().get(0).autoCap() == currentTool.jobs().get(0).autoCap() &&
                            !t.equals(currentTool)) {
                        int numbottom = 1;
                        for (ToolBatch t2 : newauto.b1s()) {
                            if (t2.bottomTool().equals(t.bottomTool())) {
                                numbottom++;
                            }
                        }
                        if (numbottom <= t.bottomTool().qty()) {
                            newauto.addToolBatch(t);
                            t.setAutoBatch(newauto);
                        }
                    }
                }
            }
            iterate2.removeAll(newauto.b1s());
            b2objs.add(newauto);

        }

        // Return packed batched
        return b2objs;

    }

    public static void main(String[] args) throws Exception {

        // Get settings from json file
        // TBD: Parse JSON information
        String filepath = "";
        String sumfile = "";
        String interfile = "";
        int pi = 0;
        int repetitions = 10;

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

        // Create file to write iterative solutions to
        FileWriter writer = new FileWriter(interfile, true);
        String initialtext = "Problem Instance: " + pi + "\n";
        writer.write(initialtext);

        // Run heuristic
        double start = System.nanoTime();
        ArrayList<SolutionPack> solutionList = new ArrayList<>();
        for (int i = 0; i < repetitions; i++) {
            ArrayList<AutoBatch> packed = run(jobobjs);
            double end = System.nanoTime();
            double elapsedTime = (end - start) / 1_000_000_000;
            int objval = 0;
            for (AutoBatch a : packed) {
                objval += (a.capacity() - a.sumToolSize());
            }
            SolutionPack soln = new SolutionPack(data.numjob, objval, elapsedTime, "Feasible");
            soln.addAutoBatch(packed);
            solutionList.add(soln);
            writer.write("Heuristic packing solution found after " + elapsedTime + " seconds with objective value " + objval + "\n");
        }

        // Choose best solution
        solutionList.sort(Comparator.comparing(SolutionPack::objVal));
        SolutionPack best = solutionList.get(0);

        // Write solution to file
        data.writePack(best);
        data.writeSum(best);

        // Close solution file
        writer.close();

    }

}
