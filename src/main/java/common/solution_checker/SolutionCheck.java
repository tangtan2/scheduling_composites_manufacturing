package common.solution_checker;
import common.model_helpers.Data;
import org.apache.commons.math3.util.Pair;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

public class SolutionCheck {

    public static int resourceUsageCheck(ArrayList<int[]> startTimes, ArrayList<int[]> endTimes, Data data, int[] maxQtyPerShift) {
        int mistakes = 0;
        int time = 0;
        int numActive = 0;
        outer: while (true) {
            int weekTime = 0;
            for (int j = 0; j < data.numShift; j++) {
                while (weekTime < data.shiftEnd[j]) {
                    int currentTime = time;
                    if (startTimes.stream().anyMatch(x -> x[0] < currentTime + 5) || endTimes.stream().anyMatch(x -> x[0] < currentTime + 5)) {
                        List<int[]> tempStarts = startTimes.stream().filter(x -> x[0] < currentTime + 5).collect(Collectors.toList());
                        List<int[]> tempEnds = endTimes.stream().filter(x -> x[0] < currentTime + 5).collect(Collectors.toList());
                        for (int[] it : tempStarts) {
                            numActive += it[1];
                        }
                        for (int[] it : tempEnds) {
                            numActive -= it[1];
                        }
                        startTimes.removeAll(tempStarts);
                        endTimes.removeAll(tempEnds);
                        if (numActive > maxQtyPerShift[j]) {
                            mistakes++;
                        }
                        if (startTimes.isEmpty()) {
                            break outer;
                        }
                    }
                    weekTime += 5;
                    time += 5;
                }
            }
        }
        return mistakes;
    }

    public static void check(String[] args) throws Exception {

        // Get settings from json file
        String filepath = args[0];
        String sumfile = args[1];
        String interfile = args[2];
        int pi = Integer.parseInt(args[3]);

        // Make data object and import raw data
        Data data = new Data(filepath, sumfile, pi);
        data.readInstanceParams();
        data.readPackParams();
        data.readSchedParams();

        // Put results into two hash maps indexed by job/batch number
        HashMap<Integer, ArrayList<Integer>> jobIntResults = new HashMap<>();
        HashMap<Integer, ArrayList<String>> jobStringResults = new HashMap<>();
        for (int i = 0; i < data.numJob; i++) {
            jobIntResults.put(data.jobIndices[i], new ArrayList<>(Arrays.asList(
                    data.jobToB0[i],
                    data.jobPrepS[i],
                    data.jobPrepE[i],
                    data.jobLayupS[i],
                    data.jobLayupE[i],
                    data.jobAutoS[i],
                    data.jobAutoE[i],
                    data.jobDemouldS[i],
                    data.jobDemouldE[i],
                    data.jobTardiness[i])
            ));
            jobStringResults.put(data.jobIndices[i], new ArrayList<>(Collections.singletonList(data.jobToTopTool[i])));
            for (int j = 0; j < data.numJob; j++) {
                if (j == data.jobIndices[i]) {
                    jobIntResults.get(data.jobIndices[i]).addAll(new ArrayList<>(Arrays.asList(
                            Integer.parseInt(data.jobName[j]),
                            data.jobDue[j],
                            data.jobSize[j],
                            data.jobAutoCap[j],
                            data.jobStepTimes[j][0],
                            data.jobStepTimes[j][1],
                            data.jobStepTimes[j][2],
                            data.jobStepTimes[j][3]
                    )));
                    jobStringResults.get(data.jobIndices[i]).addAll(new ArrayList<>(Arrays.asList(
                            data.jobPartFamily[j],
                            data.jobSteps[j][0],
                            data.jobSteps[j][1],
                            data.jobSteps[j][2],
                            data.jobSteps[j][3])
                    ));
                }
            }
        }
        HashMap<Integer, ArrayList<Integer>> b0IntResults = new HashMap<>();
        HashMap<Integer, ArrayList<String>> b0StringResults = new HashMap<>();
        for (int i = 0; i < data.numB0_new; i++) {
            b0IntResults.put(data.b0Indices[i], new ArrayList<>(Collections.singletonList(data.b0ToB1[i])));
            b0StringResults.put(data.b0Indices[i], new ArrayList<>(Collections.singletonList(data.b0TopTool[i])));
        }
        HashMap<Integer, ArrayList<Integer>> b1IntResults = new HashMap<>();
        HashMap<Integer, ArrayList<String>> b1StringResults = new HashMap<>();
        for (int i = 0; i < data.numB1_new; i++) {
            b1IntResults.put(data.b1Indices[i], new ArrayList<>(Arrays.asList(
                    data.b1ToB2[i],
                    data.b1Vol[i],
                    data.b1PrepS[i],
                    data.b1PrepE[i],
                    data.b1PrepQty[i],
                    data.b1LayupS[i],
                    data.b1LayupE[i],
                    data.b1LayupQty[i],
                    data.b1DemouldS[i],
                    data.b1DemouldE[i],
                    data.b1DemouldQty[i])
            ));
            b1StringResults.put(data.b1Indices[i], new ArrayList<>(Arrays.asList(
                    data.b1BottomTool[i],
                    data.b1PrepLabour[i],
                    data.b1PrepMachine[i],
                    data.b1LayupLabour[i],
                    data.b1LayupMachine[i],
                    data.b1DemouldLabour[i],
                    data.b1DemouldMachine[i])
            ));
        }
        HashMap<Integer, ArrayList<Integer>> b2IntResults = new HashMap<>();
        HashMap<Integer, ArrayList<String>> b2StringResults = new HashMap<>();
        for (int i = 0; i < data.numB2_new; i++) {
            b2IntResults.put(data.b2Indices[i], new ArrayList<>(Arrays.asList(
                    data.b2Cap[i],
                    data.b2SumOfToolVol[i],
                    data.b2AutoS[i],
                    data.b2AutoE[i])
            ));
            b2StringResults.put(data.b2Indices[i], new ArrayList<>(Collections.singletonList(data.b2AutoMachine[i])));
        }

        // Create hash maps of instance parameters
        HashMap<String, ArrayList<Integer>> topTools = new HashMap<>();
        for (int i = 0; i < data.numTopTool; i++) {
            topTools.put(data.topToolName[i], new ArrayList<>(Arrays.asList(
                    data.topToolCap[i],
                    data.topToolQty[i])
            ));
        }
        HashMap<String, ArrayList<Integer>> bottomTools = new HashMap<>();
        for (int i = 0; i < data.numBottomTool; i++) {
            bottomTools.put(data.bottomToolName[i], new ArrayList<>(Arrays.asList(
                    data.bottomToolCap[i],
                    data.bottomToolQty[i],
                    data.bottomToolSize[i])
            ));
        }
        HashMap<Integer, ArrayList<Pair<String, String>>> jobToTools = new HashMap<>();
        for (int i = 0; i < data.numJobToTool; i++) {
            if (!jobToTools.containsKey(Integer.parseInt(data.jobToTool[i][0]))) {
                jobToTools.put(Integer.parseInt(data.jobToTool[i][0]), new ArrayList<>(Collections.singletonList(
                        new Pair<>(data.jobToTool[i][1], data.jobToTool[i][2])
                )));
            } else {
                jobToTools.get(Integer.parseInt(data.jobToTool[i][0])).add(
                        new Pair<>(data.jobToTool[i][1], data.jobToTool[i][2])
                );
            }
        }
        HashMap<String, String> batchToMachines = new HashMap<>();
        for (int i = 0; i < data.numStepToMachine; i++) {
            batchToMachines.put(data.stepToMachine[i][1], data.stepToMachine[i][0]);
        }
        HashMap<String, ArrayList<Integer>> machineShifts = new HashMap<>();
        for (int i = 0; i < data.numMachine; i++) {
            machineShifts.put(data.machineName[i], new ArrayList<>());
            for (int j = 0; j < data.numShift; j++) {
                machineShifts.get(data.machineName[i]).add(data.machineShift[i][j]);
            }
        }
        HashMap<String, String> labourSkills = new HashMap<>();
        for (int i = 0; i < data.numLabour; i++) {
            for (int j = 0; j < data.numLabourSkill; j++) {
                if (data.labourMatrix[i][j] == 1) {
                    labourSkills.put(data.labourName[i], data.labourSkillName[j]);
                    break;
                }
            }
        }
        HashMap<String, ArrayList<Integer>> labourShifts = new HashMap<>();
        for (int i = 0; i < data.numLabour; i++) {
            labourShifts.put(data.labourName[i], new ArrayList<>());
            for (int j = 0; j < data.numShift; j++) {
                labourShifts.get(data.labourName[i]).add(data.labourShift[i][j]);
            }
        }

        // Create file to write solution checker summary to
        FileWriter writer = new FileWriter(interfile, true);
        String initialtext = "Solution check for problem Instance: " + pi + "\n";
        writer.write(initialtext);

        // Initialize result parameters
        String status = "Feasible";
        int mistakes = 0;

        // Begin checking for mistakes
        // All jobs are assigned to a correct top tool and bottom tool combination in b0 and b1
        for (int i = 0; i < data.numJob; i++) {
            int ind = 1;
            String left = b0StringResults.get(jobIntResults.get(i).get(0)).get(0);
            String right = b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(0);
            for (Pair<String, String> pair : jobToTools.get(jobIntResults.get(i).get(10))) {
                if (left.equals(pair.getFirst()) && right.equals(pair.getSecond())) {
                    ind = 0;
                    break;
                }
            }
            mistakes += ind;
        }

        // All jobs in b1 are mapped to the same prep/layup/demould machines
        for (int i = 0; i < data.numB1_new; i++) {
            int currentB1 = i;
            List<Integer> relevantJobs = jobIntResults.entrySet().stream().filter(x -> b0IntResults.get(x.getValue().get(0)).get(0) == currentB1).map(Map.Entry::getKey).collect(Collectors.toList());
            List<String> prepActivities = jobStringResults.entrySet().stream().filter(x -> relevantJobs.contains(x.getKey())).map(x -> x.getValue().get(2)).collect(Collectors.toList());
            List<String> layupActivities = jobStringResults.entrySet().stream().filter(x -> relevantJobs.contains(x.getKey())).map(x -> x.getValue().get(3)).collect(Collectors.toList());
            List<String> demouldActivities = jobStringResults.entrySet().stream().filter(x -> relevantJobs.contains(x.getKey())).map(x -> x.getValue().get(5)).collect(Collectors.toList());
            for (String s : prepActivities) {
                if (!batchToMachines.get(s).equals(batchToMachines.get(prepActivities.get(0)))) {
                    mistakes++;
                    break;
                }
            }
            for (String s : layupActivities) {
                if (!batchToMachines.get(s).equals(batchToMachines.get(layupActivities.get(0)))) {
                    mistakes++;
                    break;
                }
            }
            for (String s : demouldActivities) {
                if (!batchToMachines.get(s).equals(batchToMachines.get(demouldActivities.get(0)))) {
                    mistakes++;
                    break;
                }
            }
        }

        // All jobs in b2 are mapped to the same auto machines
        for (int i = 0; i < data.numB2_new; i++) {
            int currentB2 = i;
            List<Integer> relevantJobs = jobIntResults.entrySet().stream().filter(x -> b1IntResults.get(b0IntResults.get(x.getValue().get(0)).get(0)).get(0) == currentB2).map(Map.Entry::getKey).collect(Collectors.toList());
            List<String> autoActivities = jobStringResults.entrySet().stream().filter(x -> relevantJobs.contains(x.getKey())).map(x -> x.getValue().get(4)).collect(Collectors.toList());
            for (String s : autoActivities) {
                if (!batchToMachines.get(s).equals(batchToMachines.get(autoActivities.get(0)))) {
                    mistakes++;
                    break;
                }
            }
        }

        // All jobs have the correct tardiness
        for (int i = 0; i < data.numJob; i++) {
            if (Math.max(0, jobIntResults.get(i).get(8) - jobIntResults.get(i).get(11)) != jobIntResults.get(i).get(9)) {
                mistakes++;
            }
        }

        // Each b1 has less than the maximum number of spots filled
        for (int i = 0; i < data.numB1_new; i++) {
            int numSpots = 0;
            int countB0 = 0;
            for (int j = 0; j < data.numB0_new; j++) {
                if (b0IntResults.get(j).get(0) == i) {
                    numSpots = topTools.get(b0StringResults.get(j).get(0)).get(0);
                    countB0++;
                }
            }
            numSpots *= bottomTools.get(b1StringResults.get(i).get(0)).get(0);
            if (countB0 > numSpots) {
                mistakes++;
            }
        }

        // Each b2 has less than the capacity filled
        for (int i = 0; i < data.numB2_new; i++) {
            int sumVol = 0;
            for (int j = 0; j < data.numB1_new; j++) {
                if (b1IntResults.get(j).get(0) == i) {
                    sumVol += b1IntResults.get(j).get(1);
                }
            }
            if (sumVol != b2IntResults.get(i).get(1)) {
                mistakes++;
            }
            if (sumVol > b2IntResults.get(i).get(0)) {
                mistakes++;
            }
        }

        // Each b1 is assigned to the correct prep/layup/demould labours with the correct quantities
        ArrayList<Integer> completedBatches = new ArrayList<>();
        for (int i = 0; i < data.numJob; i++) {
            if (!completedBatches.contains(b0IntResults.get(jobIntResults.get(i).get(0)).get(0))) {
                for (int j = 0; j < data.numLabourSkillToMachine; j++) {
                    if (data.labourToMachine[j][0].equals(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(2)) &&
                            data.labourToMachine[j][1].equals(jobStringResults.get(i).get(0))) {
                        if (!labourSkills.get(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(1)).equals(data.labourToMachine[j][2]) ||
                                b1IntResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(4) != data.labourQtyRequired[j]) {
                            mistakes++;
                        }
                    }
                    if (data.labourToMachine[j][0].equals(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(4)) &&
                            data.labourToMachine[j][1].equals(jobStringResults.get(i).get(0))) {
                        if (!labourSkills.get(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(3)).equals(data.labourToMachine[j][2]) ||
                                b1IntResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(4) != data.labourQtyRequired[j]) {
                            mistakes++;
                        }
                    }
                    if (data.labourToMachine[j][0].equals(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(6)) &&
                            data.labourToMachine[j][1].equals(jobStringResults.get(i).get(0))) {
                        if (!labourSkills.get(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(5)).equals(data.labourToMachine[j][2]) ||
                                b1IntResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(4) != data.labourQtyRequired[j]) {
                            mistakes++;
                        }
                    }
                }
                completedBatches.add(b0IntResults.get(jobIntResults.get(i).get(0)).get(0));
            }
        }

        // Each b1 is assigned to the correct prep/layup/demould machines
        completedBatches = new ArrayList<>();
        for (int i = 0; i < data.numJob; i++) {
            if (!completedBatches.contains(b0IntResults.get(jobIntResults.get(i).get(0)).get(0))) {
                if (!batchToMachines.get(jobStringResults.get(i).get(2)).equals(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(2))) {
                    mistakes++;
                }
                if (!batchToMachines.get(jobStringResults.get(i).get(3)).equals(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(4))) {
                    mistakes++;
                }
                if (!batchToMachines.get(jobStringResults.get(i).get(5)).equals(b1StringResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(6))) {
                    mistakes++;
                }
                completedBatches.add(b0IntResults.get(jobIntResults.get(i).get(0)).get(0));
            }
        }

        // Each b2 is assigned to the correct auto machine
        completedBatches = new ArrayList<>();
        for (int i = 0; i < data.numJob; i++) {
            if (!completedBatches.contains(b1IntResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(0))) {
                if (!batchToMachines.get(jobStringResults.get(i).get(4)).equals(b2StringResults.get(b1IntResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(0)).get(0))) {
                    mistakes++;
                }
                completedBatches.add(b1IntResults.get(b0IntResults.get(jobIntResults.get(i).get(0)).get(0)).get(0));
            }
        }

        // Each bottom tool does not exceed the quantity in any period
        for (int i = 0; i < data.numBottomTool; i++) {
            if (b1StringResults.values().stream().map(x -> x.get(0)).collect(Collectors.toList()).contains(data.bottomToolName[i])) {
                String toolName = data.bottomToolName[i];
                List<Integer> relevantB1 = b1StringResults.entrySet().stream().filter(x -> x.getValue().get(0).equals(toolName)).map(Map.Entry::getKey).collect(Collectors.toList());
                List<Integer> relevantStartTimes = b1IntResults.entrySet().stream().filter(x -> relevantB1.contains(x.getKey())).map(Map.Entry::getValue).map(x -> x.get(2)).collect(Collectors.toList());
                List<Integer> relevantEndTimes = b1IntResults.entrySet().stream().filter(x -> relevantB1.contains(x.getKey())).map(Map.Entry::getValue).map(x -> x.get(9)).collect(Collectors.toList());
                relevantStartTimes.sort(Comparator.comparing(Integer::intValue));
                relevantEndTimes.sort(Comparator.comparing(Integer::intValue));
                relevantStartTimes.remove(0);
                int numActive = 1;
                while (!relevantEndTimes.isEmpty() && !relevantStartTimes.isEmpty()) {
                    if (relevantEndTimes.get(0) < relevantStartTimes.get(0)) {
                        numActive--;
                        relevantEndTimes.remove(0);
                    } else if (relevantEndTimes.get(0) > relevantStartTimes.get(0)) {
                        numActive++;
                        relevantStartTimes.remove(0);
                    } else {
                        relevantEndTimes.remove(0);
                        relevantStartTimes.remove(0);
                    }
                    if (numActive > data.bottomToolQty[i]) {
                        mistakes++;
                    }
                }
            }
        }

        // Each labour team does not exceed the quantity in any period
        for (int i = 0; i < data.numLabour; i++) {
            String labourName = data.labourName[i];
            ArrayList<int[]> startTimes = new ArrayList<>();
            ArrayList<int[]> endTimes = new ArrayList<>();
            if (b1StringResults.values().stream().map(x -> x.get(1)).collect(Collectors.toList()).contains(data.labourName[i])) {
                List<Integer> relevantB1 = b1StringResults.entrySet().stream().filter(x -> x.getValue().get(1).equals(labourName)).map(Map.Entry::getKey).collect(Collectors.toList());
                for (Integer it : relevantB1) {
                    startTimes.add(new int[]{b1IntResults.get(it).get(2), b1IntResults.get(it).get(4)});
                    endTimes.add(new int[]{b1IntResults.get(it).get(3), b1IntResults.get(it).get(4)});
                }
            }
            if (b1StringResults.values().stream().map(x -> x.get(3)).collect(Collectors.toList()).contains(data.labourName[i])) {
                List<Integer> relevantB1 = b1StringResults.entrySet().stream().filter(x -> x.getValue().get(3).equals(labourName)).map(Map.Entry::getKey).collect(Collectors.toList());
                for (Integer it : relevantB1) {
                    startTimes.add(new int[]{b1IntResults.get(it).get(5), b1IntResults.get(it).get(7)});
                    endTimes.add(new int[]{b1IntResults.get(it).get(6), b1IntResults.get(it).get(7)});
                }
            }
            if (b1StringResults.values().stream().map(x -> x.get(5)).collect(Collectors.toList()).contains(data.labourName[i])) {
                List<Integer> relevantB1 = b1StringResults.entrySet().stream().filter(x -> x.getValue().get(5).equals(labourName)).map(Map.Entry::getKey).collect(Collectors.toList());
                for (Integer it : relevantB1) {
                    startTimes.add(new int[]{b1IntResults.get(it).get(8), b1IntResults.get(it).get(10)});
                    endTimes.add(new int[]{b1IntResults.get(it).get(9), b1IntResults.get(it).get(10)});
                }
            }
            if (!startTimes.isEmpty()) {
                startTimes.sort(Comparator.comparing(x -> x[0]));
                endTimes.sort(Comparator.comparing(x -> x[0]));
                mistakes += resourceUsageCheck(startTimes, endTimes, data, data.labourShift[i]);
            }
        }

        // Each machine does not exceed the quantity in any period
        for (int i = 0; i < data.numMachine; i++) {
            if (b1StringResults.values().stream().map(x -> x.get(2)).collect(Collectors.toList()).contains(data.machineName[i])) {
                String machineName = data.machineName[i];
                List<Integer> relevantB1 = b1StringResults.entrySet().stream().filter(x -> x.getValue().get(2).equals(machineName)).map(Map.Entry::getKey).collect(Collectors.toList());
                ArrayList<int[]> startTimes = new ArrayList<>();
                ArrayList<int[]> endTimes = new ArrayList<>();
                for (Integer it : relevantB1) {
                    startTimes.add(new int[]{b1IntResults.get(it).get(2), 1});
                    endTimes.add(new int[]{b1IntResults.get(it).get(3), 1});
                }
                startTimes.sort(Comparator.comparing(x -> x[0]));
                endTimes.sort(Comparator.comparing(x -> x[0]));
                mistakes += resourceUsageCheck(startTimes, endTimes, data, data.machineShift[i]);
            }
            if (b1StringResults.values().stream().map(x -> x.get(4)).collect(Collectors.toList()).contains(data.machineName[i])) {
                String machineName = data.machineName[i];
                List<Integer> relevantB1 = b1StringResults.entrySet().stream().filter(x -> x.getValue().get(4).equals(machineName)).map(Map.Entry::getKey).collect(Collectors.toList());
                ArrayList<int[]> startTimes = new ArrayList<>();
                ArrayList<int[]> endTimes = new ArrayList<>();
                for (Integer it : relevantB1) {
                    startTimes.add(new int[]{b1IntResults.get(it).get(5), 1});
                    endTimes.add(new int[]{b1IntResults.get(it).get(6), 1});
                }
                startTimes.sort(Comparator.comparing(x -> x[0]));
                endTimes.sort(Comparator.comparing(x -> x[0]));
                mistakes += resourceUsageCheck(startTimes, endTimes, data, data.machineShift[i]);
            }
            if (b1StringResults.values().stream().map(x -> x.get(6)).collect(Collectors.toList()).contains(data.machineName[i])) {
                String machineName = data.machineName[i];
                List<Integer> relevantB1 = b1StringResults.entrySet().stream().filter(x -> x.getValue().get(6).equals(machineName)).map(Map.Entry::getKey).collect(Collectors.toList());
                ArrayList<int[]> startTimes = new ArrayList<>();
                ArrayList<int[]> endTimes = new ArrayList<>();
                for (Integer it : relevantB1) {
                    startTimes.add(new int[]{b1IntResults.get(it).get(8), 1});
                    endTimes.add(new int[]{b1IntResults.get(it).get(9), 1});
                }
                startTimes.sort(Comparator.comparing(x -> x[0]));
                endTimes.sort(Comparator.comparing(x -> x[0]));
                mistakes += resourceUsageCheck(startTimes, endTimes, data, data.machineShift[i]);
            }
            if (b2StringResults.values().stream().map(x -> x.get(0)).collect(Collectors.toList()).contains(data.machineName[i])) {
                String machineName = data.machineName[i];
                List<Integer> relevantB2 = b2StringResults.entrySet().stream().filter(x -> x.getValue().get(0).equals(machineName)).map(Map.Entry::getKey).collect(Collectors.toList());
                ArrayList<int[]> startTimes = new ArrayList<>();
                ArrayList<int[]> endTimes = new ArrayList<>();
                for (Integer it : relevantB2) {
                    startTimes.add(new int[]{b2IntResults.get(it).get(2), 1});
                    endTimes.add(new int[]{b2IntResults.get(it).get(3), 1});
                }
                startTimes.sort(Comparator.comparing(x -> x[0]));
                endTimes.sort(Comparator.comparing(x -> x[0]));
                mistakes += resourceUsageCheck(startTimes, endTimes, data, data.machineShift[i]);
            }
        }

        // Write solution check results to file
        if (mistakes > 0) {
            status = "Not Feasible";
        }
        data.writeSolnCheck(status, mistakes);

    }

}
