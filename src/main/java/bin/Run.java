package bin;
import common.instance_generation.GenInstances;
import common.solution_checker.SolutionCheck;
import models.basic_mip_pack.BasicMIPPack;
import models.basic_cp_pack.BasicCPPack;
import models.basic_cp_sched.BasicCPSched;
import models.basic_edd_pack.BasicEDDPack;
import models.basic_lbbd.BasicLBBD;
import models.cluster_pack.ClusterPack;
import models.genetic_sched.GeneticSched;
import models.relaxed_cp_sched.RelaxedCPSched;
import models.edd_sched.EDDSched;
import java.nio.file.*;
import java.util.*;

public class Run {

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

    public static void callEDDSched(String[] args) throws Exception {
        EDDSched.main(args);
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
            if (type != 100) {
                System.out.println("Running model type " + type + " on problem instance " + callArgs[3] + " with file name " + callArgs[0] + "...");
            } else {
                System.out.println("Generating " + callArgs[2] + " instances for date " + callArgs[3] + "...");
            }

            // Call correct model
            switch (type) {

                case 100: // Generate new instances
                    callGenInstances(callArgs);
                    break;

                case 200: // RSP
                    callRelaxedCPSched(callArgs);
                    break;

                case 0: // LBBD
                    callBasicLBBD(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 1: // CP pack + CP sched
                    callBasicCPPack(callArgs);
                    callBasicCPSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 2: // MIP pack + CP sched
                    callBasicMIPPack(callArgs);
                    callBasicCPSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 3: // EDD pack + CP sched
                    callBasicEDDPack(callArgs);
                    callBasicCPSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 4: // EDD pack + EDD sched
                    callBasicEDDPack(callArgs);
                    callEDDSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 5: // EDD pack + genetic sched
                    callBasicEDDPack(callArgs);
                    callGeneticSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 6: // cluster pack + CP sched
                    callClusterPack(callArgs);
                    callBasicCPSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 7: // cluster pack + EDD sched
                    callClusterPack(callArgs);
                    callEDDSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

                case 8: // cluster pack + genetic sched
                    callClusterPack(callArgs);
                    callGeneticSched(callArgs);
                    callSolutionCheck(callArgs);
                    break;

            }

            // Show complete
            System.out.println("Success!");

        }

    }

}
