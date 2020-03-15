package bin;
import common.instance_generation.GenInstances;
import common.solution_checker.SolutionCheck;
import models.edd_parallel_sched.EDDParallelSched;
import models.basic_mip_pack.BasicMIPPack;
import models.basic_cp_pack.BasicCPPack;
import models.basic_cp_sched.BasicCPSched;
import models.basic_edd_pack.BasicEDDPack;
import models.basic_lbbd.BasicLBBD;
import models.cluster_pack.ClusterPack;
import models.edd_serial_sched.EDDSerialSched;
import models.genetic_sched.GeneticSched;
import models.relaxed_cp_sched.RelaxedCPSched;
import java.nio.file.*;
import java.util.*;

public class Run {

    public static void callEDDParallelSched(String[] args) throws Exception {
        EDDParallelSched.main(args);
    }

    public static void callBasicMIPPack(String[] args) {
        BasicMIPPack.main(args);
    }

    public static void callBasicCPPack(String[] args) {
        BasicCPPack.main(args);
    }

    public static void callBasicCPSched(String[] args) {
        BasicCPSched.main(args);
    }

    public static void callBasicEDDPack(String[] args) throws Exception {
        BasicEDDPack.main(args);
    }

    public static void callBasicLBBD(String[] args) {
        BasicLBBD.main(args);
    }

    public static void callClusterPack(String[] args) {
        ClusterPack.main(args);
    }

    public static void callEDDSerialSched(String[] args) throws Exception {
        EDDSerialSched.main(args);
    }

    public static void callGeneticSched(String[] args) throws Exception {
        GeneticSched.main(args);
    }

    public static void callRelaxedCPSched(String[] args) {
        RelaxedCPSched.main(args);
    }

    public static void callGenInstances(String[] args) throws Exception {
        GenInstances.generate(args);
    }

    public static void callSolutionCheck(String[] args) throws Exception {
        SolutionCheck.check(args);
    }

    public static void main(String[] args) throws Exception {

        // Read in arguments from experiment file
        List<String> data = Files.readAllLines(Paths.get(args[0]));

        // Loop through each line and call according to line contents
        for (String line : data) {

            // Split line into arguments
            String[] allArgs = line.split(",");
            int type = Integer.parseInt(allArgs[0]);
            String[] callArgs = Arrays.copyOfRange(allArgs, 1, allArgs.length);

            // Write to output
            System.out.println("Running model type " + type + " on problem instance " + callArgs[3] + " with file name " + callArgs[0]);

            // Call correct model
            switch (type) {

                case 100: // Generate new instances
                    callGenInstances(callArgs);

                case 0: // RSP + LBBD
                    callRelaxedCPSched(callArgs);
                    callBasicLBBD(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 1: // RSP + CP pack + CP sched
                    callRelaxedCPSched(callArgs);
                    callBasicCPPack(callArgs);
                    callBasicCPSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 2: // RSP + MIP pack + CP sched
                    callRelaxedCPSched(callArgs);
                    callBasicMIPPack(callArgs);
                    callBasicCPSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 3: // RSP + EDD pack + CP sched
                    callRelaxedCPSched(callArgs);
                    callBasicEDDPack(callArgs);
                    callBasicCPSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 4: // RSP + chap2 pack + chap2 sched
                    callRelaxedCPSched(callArgs);
                    // TBD
                    // TBD
                    callSolutionCheck(callArgs);
                    break;

                case 5: // RSP + chap2 pack + EDD serial sched
                    callRelaxedCPSched(callArgs);
                    // TBD
                    callEDDSerialSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 6: // RSP + chap2 pack + EDD parallel sched
                    callRelaxedCPSched(callArgs);
                    // TBD
                    callEDDParallelSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 7: // RSP + chap2 pack + genetic sched
                    callRelaxedCPSched(callArgs);
                    // TBD
                    callGeneticSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 8: // RSP + cluster pack + chap2 sched
                    callRelaxedCPSched(callArgs);
                    callClusterPack(callArgs);
                    // TBD
                    callSolutionCheck(callArgs);
                    break;

                case 9: // RSP + cluster pack + EDD serial sched
                    callRelaxedCPSched(callArgs);
                    callClusterPack(callArgs);
                    callEDDSerialSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 10: // RSP + cluster pack + EDD parallel sched
                    callRelaxedCPSched(callArgs);
                    callClusterPack(callArgs);
                    callEDDParallelSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 11: // RSP + cluster pack + genetic sched
                    callRelaxedCPSched(callArgs);
                    callClusterPack(callArgs);
                    callGeneticSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

            }

        }

    }

}
