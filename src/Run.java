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

public class Run {

    public static void callEDDParallelSched(String[] args) throws Exception {
        EDDParallelSched.main(args);
    }

    public static void callBasicMIPPack(String[] args) throws Exception {
        BasicMIPPack.main(args);
    }

    public static void callBasicCPPack(String[] args) throws Exception {
        BasicCPPack.main(args);
    }

    public static void callBasicCPSched(String[] args) throws Exception {
        BasicCPSched.main(args);
    }

    public static void callBasicEDDPack(String[] args) throws Exception {
        BasicEDDPack.main(args);
    }

    public static void callBasicLBBD(String[] args) throws Exception {
        BasicLBBD.main(args);
    }

    public static void callClusterPack(String[] args) throws Exception {
        ClusterPack.main(args);
    }

    public static void callEDDSerialSched(String[] args) throws Exception {
        EDDSerialSched.main(args);
    }

    public static void callGeneticSched(String[] args) throws Exception {
        GeneticSched.main(args);
    }

    public static void callRelaxedCPSched(String[] args) throws Exception {
        RelaxedCPSched.main(args);
    }

    public static void callGenInstances(String[] args) throws Exception {
        GenInstances.generate(args);
    }

    public static void callSolutionCheck(String[] args) throws Exception {
        SolutionCheck.check(args);
    }

    public static void main(String[] args) throws Exception {

        // Testing
        String[] callArgs = new String[]{
                "/Users/tanyatang/Documents/Code/java/scheduling_composites_manufacturing/data/templates/instance_template.xlsx",
                "/Users/tanyatang/Documents/Code/java/scheduling_composites_manufacturing/data/temp/full_instances",
                "30", "500", "1000", "1500", "2000", "2500", "3000", "3500", "4000"
        };
        callGenInstances(callArgs);

    }

}
