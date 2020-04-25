package models.genetic_sched;
import common.model_helpers.*;
import models.edd_sched.EDDSched;
import org.apache.commons.math3.util.Pair;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class GeneticSched {

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

        // Create activities
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

        // Start clock
        double start = System.nanoTime();

        // Initialize parameters
        int numEligible = (int) Math.round(0.4 * data.numB1_new);
        int numInPop = 20;
        int numGenerations = 25;
        double mutation = 0.3;

        // Initialize population
        ArrayList<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < numInPop; i++) {

            // Create new activity list
            ArrayList<AutoBatchA> iterate = new ArrayList<>(b2objs);
            ArrayList<AutoBatchA> newAutoBatchList = new ArrayList<>();
            while (newAutoBatchList.size() < data.numB2_new) {
                ArrayList<AutoBatchA> eligible = new ArrayList<>();
                for (int j = 0; j < numEligible; j++) {
                    eligible.add(iterate.get((int) Math.floor(Math.random() * iterate.size())));
                }
                AutoBatchA chosen = eligible.get((int) Math.floor(Math.random() * eligible.size()));
                newAutoBatchList.add(chosen);
                iterate.remove(chosen);
            }

            // Create schedule and find fitness
            ArrayList<Activity> scheduled = EDDSched.schedule(newAutoBatchList);
            System.out.println("Initial individual " + i + " scheduled");
            int newFitness = 0;
            for (Activity a : scheduled) {
                for (Job j : a.b1().jobs()) {
                    newFitness += Math.max(0, a.end() - j.due());
                }
            }
            individuals.add(new Individual(newAutoBatchList, scheduled, newFitness));

        }

        // Create arrays to hold best fitness over generations
        ArrayList<Double> fitnessOverTime = new ArrayList<>();
        ArrayList<Double> times = new ArrayList<>();

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
            fitnessOverTime.add((double) individuals.get(0).fitness());
            times.add((System.nanoTime() - start) / 1_000_000_000);

            // Add best individuals to new population
            ArrayList<Individual> newIndividuals = new ArrayList<>(individuals.subList(0, numInPop));

            // Divide into random pairs
            ArrayList<Pair<ArrayList<AutoBatchA>, ArrayList<AutoBatchA>>> parents = new ArrayList<>();
            Collections.shuffle(newIndividuals);
            for (int i = 0; i < numInPop; i += 2) {
                parents.add(new Pair<>(newIndividuals.get(i).autoList(), newIndividuals.get(i + 1).autoList()));
            }

            // Reproduce
            for (Pair<ArrayList<AutoBatchA>, ArrayList<AutoBatchA>> currentParents : parents) {

                // Crossover
                ArrayList<AutoBatchA> mother = currentParents.getFirst();
                ArrayList<AutoBatchA> father = currentParents.getSecond();
                ArrayList<AutoBatchA> daughter = new ArrayList<>();
                ArrayList<AutoBatchA> son = new ArrayList<>();
                int numActivities = mother.size();
                int lb = (int) (0.05 * numActivities);
                int ub = (int) (0.95 * numActivities);
                Pair<Integer, Integer> crossover = new Pair<>((int) Math.round(Math.random() * (ub - lb)) + lb, (int) Math.round(Math.random() * (ub - lb)) + lb);
                if (crossover.getFirst() < crossover.getSecond()) {
                    daughter.addAll(mother.subList(0, crossover.getFirst()));
                    daughter.addAll(father.subList(crossover.getFirst(), crossover.getSecond()).stream().filter(x -> !daughter.contains(x)).collect(Collectors.toList()));
                    daughter.addAll(mother.stream().filter(x -> !daughter.contains(x)).collect(Collectors.toList()));
                    son.addAll(father.subList(0, crossover.getFirst()));
                    son.addAll(mother.subList(crossover.getFirst(), crossover.getSecond()).stream().filter(x -> !son.contains(x)).collect(Collectors.toList()));
                    son.addAll(father.stream().filter(x -> !son.contains(x)).collect(Collectors.toList()));
                } else {
                    daughter.addAll(mother.subList(0, crossover.getSecond()));
                    daughter.addAll(father.subList(crossover.getSecond(), crossover.getFirst()).stream().filter(x -> !daughter.contains(x)).collect(Collectors.toList()));
                    daughter.addAll(mother.stream().filter(x -> !daughter.contains(x)).collect(Collectors.toList()));
                    son.addAll(father.subList(0, crossover.getSecond()));
                    son.addAll(mother.subList(crossover.getSecond(), crossover.getFirst()).stream().filter(x -> !son.contains(x)).collect(Collectors.toList()));
                    son.addAll(father.stream().filter(x -> !son.contains(x)).collect(Collectors.toList()));
                }
                assert daughter.size() == mother.size();
                assert son.size() == father.size();

                // Mutation
                for (ArrayList<AutoBatchA> child : List.of(daughter, son)) {
                    if (Math.random() < mutation) {
                        int index1 = (int) Math.floor(Math.random() * (numActivities - 6)) + 3;
                        int index2 = (int) Math.floor(Math.random() * (numActivities - 6)) + 3;
                        System.out.println("numActivities: " + numActivities + ", index1: " + index1 + ", index2: " + index2);
                        if (index1 > index2) {
                            AutoBatchA swap1 = child.get(index1);
                            AutoBatchA swap2 = child.get(index2);
                            child.remove(swap1);
                            child.remove(swap2);
                            child.add(index1, swap2);
                            child.add(index2, swap1);
                        } else if (index2 > index1) {
                            AutoBatchA swap1 = child.get(index1);
                            AutoBatchA swap2 = child.get(index2);
                            child.remove(swap1);
                            child.remove(swap2);
                            child.add(index1, swap2);
                            child.add(index2, swap1);
                        }
                    }
                }

                // Find schedule and fitness of daughter and son and add to new individuals list
                for (ArrayList<AutoBatchA> child : List.of(daughter, son)) {

                    // Create schedule and find fitness
                    ArrayList<Activity> scheduled = EDDSched.schedule(child);
                    System.out.println("Generation " + gen + " child scheduled");
                    int newFitness = 0;
                    for (Activity a : scheduled) {
                        for (Job j : a.b1().jobs()) {
                            newFitness += Math.max(0, a.end() - j.due());
                        }
                    }
                    newIndividuals.add(new Individual(child, scheduled, newFitness));

                }

            }

            // Move to next generation
            individuals = newIndividuals;
            writer.write("Generation " + gen + " is complete\n");
            gen++;

        }

        // Add last fitness over time
        individuals.sort(Comparator.comparing(Individual::fitness));
        fitnessOverTime.add((double) individuals.get(0).fitness());
        times.add((System.nanoTime() - start) / 1_000_000_000);

        // Make solution object
        individuals.sort(Comparator.comparing(Individual::fitness));
        ArrayList<Activity> bestIndividual = individuals.get(0).activityList();
        double elapsedTime = (System.nanoTime() - start) / 1_000_000_000;
        int objVal = individuals.get(0).fitness();
        writer.write("Solution found after " + elapsedTime + " seconds with objective of " + objVal + " \n");
        SolutionSched soln = new SolutionSched(data.numJob, objVal, elapsedTime, "Feasible");
        soln.addActivities(bestIndividual);

        // Write solution to file
        data.writeSched(soln);
        data.writeSum(soln);
        data.writeQual(fitnessOverTime, times);

        // Close modeler and intermediate file
        writer.close();

    }

}
