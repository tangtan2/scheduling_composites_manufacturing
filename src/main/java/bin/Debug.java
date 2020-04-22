package bin;
import common.instance_generation.GenInstances;
import common.solution_checker.SolutionCheck;
import models.basic_mip_pack.BasicMIPPack;
import models.basic_cp_pack.BasicCPPack;
import models.basic_cp_sched.BasicCPSched;
import models.basic_edd_pack.BasicEDDPack;
import models.basic_lbbd.BasicLBBD;
import models.cluster_pack.ClusterPack;
import models.edd_sched.EDDSched;
import models.genetic_sched.GeneticSched;
import models.relaxed_cp_sched.RelaxedCPSched;

public class Debug {

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

//        // Testing generate instances
//        String[] genArgs = new String[]{
//                "/Users/tanyatang/Documents/Code/java/scheduling_composites_manufacturing/data/templates/instance_template.xlsx",
//                "/Users/tanyatang/Downloads",
//                "30", "042220", "4000"
//        };
//        callGenInstances(genArgs);
//
//        // Testing relaxed scheduling
//        for (int i = 0; i < 30; i++) {
//            String[] callArgs = new String[]{
//                    "/Users/tanyatang/Downloads/test/jobs_2000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_2000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_2000/intermediates.txt", Integer.toString(i)
//            };
//            callRelaxedCPSched(callArgs);
//        }
//
//        // Testing CP packing
//        for (int i = 0; i < 15; i++) {
//            String[] callArgs = new String[]{
//                    "/Users/tanyatang/Downloads/test/jobs_1000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/intermediates.txt", Integer.toString(i)
//            };
//            callBasicCPPack(callArgs);
//        }
//
//        // Testing MIP packing
//        for (int i = 15; i < 30; i++) {
//            String[] callArgs = new String[]{
//                    "/Users/tanyatang/Downloads/test/jobs_1000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/intermediates.txt", Integer.toString(i)
//            };
//            callBasicMIPPack(callArgs);
//        }
//
//        // Testing EDD packing
//        for (int i = 0; i < 10; i++) {
//            String[] callArgs = new String[]{
//                    "/Users/tanyatang/Downloads/test/jobs_2000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_2000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_2000/intermediates.txt", Integer.toString(i), "0"
//            };
//            callBasicEDDPack(callArgs);
//        }
//
//        // Testing cluster packing
//        for (int i = 0; i < 30; i++) {
//            String[] callArgs = new String[]{
//                    "/Users/tanyatang/Downloads/test/jobs_3000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_3000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_3000/intermediates.txt", Integer.toString(i)
//            };
//            callClusterPack(callArgs);
//        }
//
//        // Testing LBBD
//        for (int i = 1; i < 5; i++) {
//            String[] callArgs = new String[] {
//                    "/Users/tanyatang/Downloads/test/jobs_1000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/intermediates.txt", Integer.toString(i)
//            };
//            callBasicLBBD(callArgs);
//        }
//
//        // Testing CP scheduling
//        for (int i = 0; i < 10; i++) {
//            String[] callArgs = new String[] {
//                    "/Users/tanyatang/Downloads/test/jobs_3000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_3000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_3000/intermediates.txt", Integer.toString(i)
//            };
//            callBasicCPSched(callArgs);
//        }
//
//        // Testing EDD scheduling
//        for (int i = 0; i < 1; i++) {
//            String[] callArgs = new String[] {
//                    "/Users/tanyatang/Downloads/jobs_4000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/jobs_4000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/jobs_4000/intermediates.txt", Integer.toString(i), "10"
//            };
//            callEDDSched(callArgs);
//        }
//
//        // Testing genetic scheduling
//        for (int i = 0; i < 30; i++) {
//            String[] callArgs = new String[] {
//                    "/Users/tanyatang/Downloads/test/jobs_2000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_2000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_2000/intermediates.txt", Integer.toString(i)
//            };
//            callGeneticSched(callArgs);
//        }
//
//        // Testing solution checker
//        for (int i = 1; i < 5; i++) {
//            String[] callArgs = new String[] {
//                    "/Users/tanyatang/Downloads/test/jobs_1000/instance_" + i + ".xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/summary.xlsx",
//                    "/Users/tanyatang/Downloads/test/jobs_1000/intermediates.txt", Integer.toString(i)
//            };
//            callSolutionCheck(callArgs);
//        }

    }

}
