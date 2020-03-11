package common.solution_checker;
import common.model_helpers.Data;
import java.io.FileWriter;

public class SolutionCheck {

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

        // Create file to write solution checker summary to
        FileWriter writer = new FileWriter(interfile, true);
        String initialtext = "Solution check for problem Instance: " + pi + "\n";
        writer.write(initialtext);

        // Initialize result parameters
        String status = "Feasible";
        int mistakes = 0;

        // Begin checking for mistakes

        // Write solution check results to file
        data.writeSolnCheck(status, mistakes);

    }

}
