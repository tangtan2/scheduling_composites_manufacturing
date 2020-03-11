package models.genetic_sched;
import common.model_helpers.*;
import org.apache.commons.math3.util.Pair;
import java.util.*;
import java.io.*;

public class GeneticSched {

    public static int[][] serial(ArrayList<Activity> activityList) {

        // While loop to schedule activities
        int[][] schedTimes = new int[activityList.size()][5];
        for (Activity current : activityList) {

            // Calculate maximum end time of predecessors
            current.setMaxPred(0);
            for (Activity pred : current.predecessors()) {
                if (schedTimes[activityList.indexOf(pred)][3] > current.maxPredEnd()) {
                    current.setMaxPred((int) Math.ceil((double) schedTimes[activityList.indexOf(pred)][3] / 5) * 5);
                }
            }

            // Schedule
            if (current.type() == 2) {

                // Collect all relevant horizons to current activity
                ArrayList<Horizon> relevanthorizons = new ArrayList<>();
                for (ToolBatchA t : current.b2().b1sA()) {
                    relevanthorizons.add(t.bottomToolH().horizon());
                }
                relevanthorizons.add(current.b2().autoMachineH().horizon());

                // Start at earliest possible time and iterate through horizon periods
                int earliest = Math.max(current.maxPredEnd(), relevanthorizons.stream().map(Horizon::earliest).max(Comparator.comparing(Integer::intValue)).orElseThrow());
                for (int i = earliest; i < relevanthorizons.get(0).horizonEnd(); i += 5) {

                    // Check if horizons are open for duration of activity
                    int ind = 0;
                    for (Horizon h : relevanthorizons) {
                        int numtools = 0;
                        for (ToolBatchA t : current.b2().b1sA()) {
                            if (t.bottomToolH().horizon().equals(h)) {
                                numtools++;
                            }
                        }
                        if (numtools == 0) {
                            if (!h.check(i, i + current.length(), 1)) {
                                ind = 1;
                                break;
                            }
                        } else {
                            if (!h.check(i, i + current.length(), numtools)) {
                                ind = 1;
                                break;
                            }
                        }
                    }

                    // If all horizons are open, then schedule and exit loop
                    if (ind == 0) {
                        for (Horizon h : relevanthorizons) {
                            int numtools = 0;
                            for (ToolBatchA t : current.b2().b1sA()) {
                                if (t.bottomToolH().horizon().equals(h)) {
                                    numtools++;
                                }
                            }
                            if (numtools == 0) {
                                h.schedule(i, i + current.length(), 1);
                            } else {
                                h.schedule(i, i + current.length(), numtools);
                            }
                        }
                        schedTimes[activityList.indexOf(current)] = new int[]{current.index(), current.b2().capacity(), i, i + current.length(), current.due()};
                        break;
                    }

                }

            } else {

                // Collect all relevant horizons to current activity
                ArrayList<Horizon> relevanthorizons = new ArrayList<>();
                int labourqty = 0;
                relevanthorizons.add(current.b1().bottomToolH().horizon());
                if (current.type() == 0) {
                    relevanthorizons.add(current.b1().prepMachineH().horizon());
                    relevanthorizons.add(current.b1().prepLabourH().horizon());
                    labourqty = current.b1().prepQty();
                } else if (current.type() == 1) {
                    relevanthorizons.add(current.b1().layupMachineH().horizon());
                    relevanthorizons.add(current.b1().layupLabourH().horizon());
                    labourqty = current.b1().layupQty();
                } else if (current.type() == 3) {
                    relevanthorizons.add(current.b1().demouldMachineH().horizon());
                    relevanthorizons.add(current.b1().demouldLabourH().horizon());
                    labourqty = current.b1().demouldQty();
                }

                // Start at maximum end of predecessors and iterate through horizon periods
                int earliest = Math.max(current.maxPredEnd(), relevanthorizons.stream().map(Horizon::earliest).max(Comparator.comparing(Integer::intValue)).orElseThrow());
                for (int i = earliest; i < relevanthorizons.get(0).horizonEnd(); i += 5) {

                    // Check if horizons are open for duration of activity
                    int ind = 0;
                    for (Horizon h : relevanthorizons) {
                        if (relevanthorizons.indexOf(h) == relevanthorizons.size() - 1) {
                            if (!h.check(i, i + current.length(), labourqty)) {
                                ind = 1;
                                break;
                            }
                        } else {
                            if (!h.check(i, i + current.length(), 1)) {
                                ind = 1;
                                break;
                            }
                        }
                    }

                    // If all horizons are open, then schedule and exit loop
                    if (ind == 0) {
                        for (Horizon h : relevanthorizons) {
                            if (relevanthorizons.indexOf(h) == relevanthorizons.size() - 1) {
                                h.schedule(i, i + current.length(), labourqty);
                            } else {
                                h.schedule(i, i + current.length(), 1);
                            }
                        }
                        schedTimes[activityList.indexOf(current)] = new int[]{current.index(), current.type(), i, i + current.length(), current.due()};
                        break;
                    }

                }

            }

        }

        // Return scheduled activities
        return schedTimes;

    }

    public static void main(String[] args) throws Exception {

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

        // Create activities
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

        // Start clock
        double start = System.nanoTime();

        // Initialize raw activity list
        ArrayList<Activity> rawActivityList = new ArrayList<>();
        for (AutoBatchA a : b2objs) {
            for (ToolBatchA t : a.b1sA()) {
                rawActivityList.add(t.prepAct());
                rawActivityList.add(t.layupAct());
                rawActivityList.add(t.demouldAct());
            }
            rawActivityList.add(a.autoAct());
        }

        // Initialize parameters
        int numEligible = (int) Math.round(0.4 * data.numb1_new);
        int numInPop = 20;
        int numGenerations = 2;
        double mutation = 0.05;

        // Initialize population
        ArrayList<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < numInPop; i++) {

            // Create new activity list
            ArrayList<Activity> newActivityList = new ArrayList<>();
            ArrayList<Activity> eligibleSet = new ArrayList<>();
            for (Activity a : rawActivityList) {
                if (a.predecessors().isEmpty()) {
                    eligibleSet.add(a);
                }
            }
            while (!eligibleSet.isEmpty()) {
                ArrayList<Activity> randomEligible = new ArrayList<>();
                for (int j = 0; j < numEligible; j++) {
                    randomEligible.add(eligibleSet.get((int) Math.floor(Math.random() * eligibleSet.size())));
                }
                randomEligible.sort(Comparator.comparing(Activity::due));
                Activity next = randomEligible.get(0);
                newActivityList.add(next);
                eligibleSet.remove(next);
                for (Activity a : rawActivityList) {
                    if (!newActivityList.contains(a) && !eligibleSet.contains(a) && newActivityList.containsAll(a.predecessors())) {
                        eligibleSet.add(a);
                    }
                }
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

            // Create schedule and find fitness
            int[][] newSched = serial(newActivityList);
            int newFitness = 0;
            for (int[] ints : newSched) {
                if (ints[1] == 3) {
                    newFitness += Math.max(ints[3] - ints[4], 0);
                }
            }
            individuals.add(new Individual(newActivityList, newSched, newFitness));

        }

        // Genetic algorithm search
        int gen = 0;
        outer: while (gen < numGenerations) {

            // If there is a solution with 0 tardiness, exit loop and take as best
            for (Individual i : individuals) {
                if (i.fitness() == 0) {
                    break outer;
                }
            }

            /*
            crossover/mutation parameters
            mutation probability = 0.05
            two-point crossover
            40 individuals in population before reproduction, 80 individuals after reproduction
            25 generations
            ranking selection -> keep 40 best individuals, discard rest
            -> randomly select pairs from 40 best individuals
            -> each pair produces a son and daughter that joins the population
            */

            // Sort previous generation by fitness
            individuals.sort(Comparator.comparing(Individual::fitness));

            // Add best individuals to new population
            ArrayList<Individual> newIndividuals = new ArrayList<>(individuals.subList(0, numInPop));

            // Divide into random pairs
            ArrayList<Pair<ArrayList<Activity>, ArrayList<Activity>>> parents = new ArrayList<>();
            ArrayList<Integer> parentIndices = new ArrayList<>();
            for (int i = 0; i < numInPop; i++) {
                parentIndices.add(i);
            }
            Collections.shuffle(parentIndices);
            for (int i = 0; i < numInPop; i += 2) {
                parents.add(new Pair<>(newIndividuals.get(parentIndices.get(i)).activityList(), newIndividuals.get(parentIndices.get(i + 1)).activityList()));
            }

            // Reproduce
            for (Pair<ArrayList<Activity>, ArrayList<Activity>> currentParents : parents) {

                // Crossover
                ArrayList<Activity> mother = currentParents.getFirst();
                ArrayList<Activity> father = currentParents.getSecond();
                ArrayList<Activity> daughter = new ArrayList<>();
                ArrayList<Activity> son = new ArrayList<>();
                int numActivities = mother.size();
                int lb = (int) (0.05 * numActivities);
                int ub = (int) (0.95 * numActivities);
                Pair<Integer, Integer> crossover = new Pair<>((int) Math.round(Math.random() * (ub - lb)) + lb, (int) Math.round(Math.random() * (ub - lb)) + lb);
                if (crossover.getFirst() < crossover.getSecond()) {
                    daughter.addAll(mother.subList(0, crossover.getFirst()));
                    for (Activity a : father.subList(crossover.getFirst(), crossover.getSecond())) {
                        for (Activity pred : a.predecessors()) {
                            if (!daughter.contains(pred)) {
                                daughter.add(pred);
                            }
                        }
                        if (!daughter.contains(a)) {
                            daughter.add(a);
                        }
                    }
                    for (Activity a : mother.subList(crossover.getSecond(), mother.size())) {
                        for (Activity pred : a.predecessors()) {
                            if (!daughter.contains(pred)) {
                                daughter.add(pred);
                            }
                        }
                        if (!daughter.contains(a)) {
                            daughter.add(a);
                        }
                    }
                    for (Activity a : mother) {
                        for (Activity pred : a.predecessors()) {
                            if (!daughter.contains(pred)) {
                                daughter.add(pred);
                            }
                        }
                        if (!daughter.contains(a)) {
                            daughter.add(a);
                        }
                    }
                    son.addAll(father.subList(0, crossover.getFirst()));
                    for (Activity a : mother.subList(crossover.getFirst(), crossover.getSecond())) {
                        for (Activity pred : a.predecessors()) {
                            if (!son.contains(pred)) {
                                son.add(pred);
                            }
                        }
                        if (!son.contains(a)) {
                            son.add(a);
                        }
                    }
                    for (Activity a : father.subList(crossover.getSecond(), mother.size())) {
                        for (Activity pred : a.predecessors()) {
                            if (!son.contains(pred)) {
                                son.add(pred);
                            }
                        }
                        if (!son.contains(a)) {
                            son.add(a);
                        }
                    }
                    for (Activity a : father) {
                        for (Activity pred : a.predecessors()) {
                            if (!son.contains(pred)) {
                                son.add(pred);
                            }
                        }
                        if (!son.contains(a)) {
                            son.add(a);
                        }
                    }
                } else {
                    daughter.addAll(mother.subList(0, crossover.getSecond()));
                    for (Activity a : father.subList(crossover.getSecond(), crossover.getFirst())) {
                        for (Activity pred : a.predecessors()) {
                            if (!daughter.contains(pred)) {
                                daughter.add(pred);
                            }
                        }
                        if (!daughter.contains(a)) {
                            daughter.add(a);
                        }
                    }
                    for (Activity a : mother.subList(crossover.getFirst(), mother.size())) {
                        for (Activity pred : a.predecessors()) {
                            if (!daughter.contains(pred)) {
                                daughter.add(pred);
                            }
                        }
                        if (!daughter.contains(a)) {
                            daughter.add(a);
                        }
                    }
                    for (Activity a : mother) {
                        for (Activity pred : a.predecessors()) {
                            if (!daughter.contains(pred)) {
                                daughter.add(pred);
                            }
                        }
                        if (!daughter.contains(a)) {
                            daughter.add(a);
                        }
                    }
                    son.addAll(father.subList(0, crossover.getSecond()));
                    for (Activity a : mother.subList(crossover.getSecond(), crossover.getFirst())) {
                        for (Activity pred : a.predecessors()) {
                            if (!son.contains(pred)) {
                                son.add(pred);
                            }
                        }
                        if (!son.contains(a)) {
                            son.add(a);
                        }
                    }
                    for (Activity a : father.subList(crossover.getFirst(), mother.size())) {
                        for (Activity pred : a.predecessors()) {
                            if (!son.contains(pred)) {
                                son.add(pred);
                            }
                        }
                        if (!son.contains(a)) {
                            son.add(a);
                        }
                    }
                    for (Activity a : father) {
                        for (Activity pred : a.predecessors()) {
                            if (!son.contains(pred)) {
                                son.add(pred);
                            }
                        }
                        if (!son.contains(a)) {
                            son.add(a);
                        }
                    }
                }
                assert daughter.size() == mother.size();
                assert son.size() == father.size();

                // Mutation
                for (ArrayList<Activity> child : List.of(daughter, son)) {
                    if (Math.random() < mutation) {
                        int index1 = (int) Math.floor(Math.random() * numActivities);
                        int index2 = (int) Math.floor(Math.random() * numActivities);
                        if (index1 > index2) {
                            int check = 0;
                            for (Activity a : child.get(index1).predecessors()) {
                                if (!child.subList(0, index2).contains(a)) {
                                    check = 1;
                                    break;
                                }
                            }
                            if (check == 0) {
                                Activity swap1 = child.get(index1);
                                Activity swap2 = child.get(index2);
                                child.remove(swap1);
                                child.remove(swap2);
                                child.add(index1, swap2);
                                child.add(index2, swap1);
                            }
                        } else if (index2 > index1) {
                            int check = 0;
                            for (Activity a : child.get(index2).predecessors()) {
                                if (!child.subList(0, index1).contains(a)) {
                                    check = 1;
                                    break;
                                }
                            }
                            if (check == 0) {
                                Activity swap1 = child.get(index1);
                                Activity swap2 = child.get(index2);
                                child.remove(swap1);
                                child.remove(swap2);
                                child.add(index1, swap2);
                                child.add(index2, swap1);
                            }
                        }
                    }
                }

                // Find schedule and fitness of daughter and son and add to new individuals list
                for (ArrayList<Activity> child : List.of(daughter, son)) {

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

                    // Create schedule and find fitness
                    int[][] newSched = serial(child);
                    int newFitness = 0;
                    for (int[] ints : newSched) {
                        if (ints[1] == 3) {
                            newFitness += Math.max(ints[3] - ints[4], 0);
                        }
                    }
                    newIndividuals.add(new Individual(child, newSched, newFitness));

                }

            }

            // Move to next generation
            individuals = newIndividuals;
            gen++;

        }

        // Get best scheduled activity list and map to corresponding jobs/batches
        individuals.sort(Comparator.comparing(Individual::fitness));
        ArrayList<Activity> bestIndividual = individuals.get(0).activityList();
        int[][] bestSchedule = individuals.get(0).schedule();
        bestIndividual.sort(Comparator.comparing(Activity::index));
        for (int i = 0; i < bestIndividual.size(); i++) {
            bestIndividual.get(bestSchedule[i][0]).schedule(bestSchedule[i][2], bestSchedule[i][3]);
        }

        // Make solution object
        double elapsedTime = (System.nanoTime() - start) / 1_000_000_000;
        int objVal = 0;
        for (Activity b2 : bestIndividual) {
            if (b2.type() == 3) {
                for (Job j : b2.b1().jobs()) {
                    objVal += Math.max(0, b2.end() - j.due());
                }
            }
        }
        SolutionSched soln = new SolutionSched(data.numjob, objVal, elapsedTime, "Feasible");
        soln.addActivities(bestIndividual);

        // Write solution to file
        data.writeSched(soln);
        data.writeSum(soln);

        // Close modeler and intermediate file
        writer.close();

    }

}