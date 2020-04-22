package common.instance_generation;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

public class GenInstances {

    public static void generate(String[] args) throws Exception {

        // Get instance generation parameters
        String templatePath = args[0];
        String instancePath = args[1];
        int reps = Integer.parseInt(args[2]);
        String date = args[3];
        ArrayList<Integer> numjobs = new ArrayList<>();
        for (int i = 4; i < args.length; i++) {
            numjobs.add(Integer.parseInt(args[i]));
        }

        // Make overarching folder
        String newFolder = instancePath + "/instances_" + date;
        boolean folderSuccess0 = new File(newFolder).mkdir();

        // Iterate through different instance classes
        for (Integer numjob : numjobs) {

            // Make new directory to hold instances
            String currentFolder = newFolder + "/jobs_" + numjob + "/";
            boolean folderSuccess1 = new File(currentFolder).mkdir();
            if (folderSuccess1 && folderSuccess0) {

                // Create reps number of instances
                for (int i = 0; i < reps; i++) {

                    // Copy template file to new directory
                    String currentFile = currentFolder + "/instance_" + i + ".xlsx";
                    Files.copy(Paths.get(templatePath), Paths.get(currentFile));

                    // Open file and sample new jobs
                    XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(currentFile));
                    XSSFSheet sheet = workbook.getSheet("single_param");
                    int totjobs = (int) sheet.getRow(2).getCell(1).getNumericCellValue();
                    int numsamples = (int) sheet.getRow(19).getCell(1).getNumericCellValue();
                    sheet = workbook.getSheet("sample");
                    ArrayList<Integer> jobs = new ArrayList<>();
                    for (int j = 0; j < numjob; j++) {
                        jobs.add((int) sheet.getRow(
                                (int) Math.ceil(Math.random() * numsamples)
                        ).getCell(0).getNumericCellValue());
                    }
                    sheet = workbook.getSheet("jobs");
                    int[] autocaps = new int[numjob];
                    int[] sizes = new int[numjob];
                    String[] partFams = new String[numjob];
                    int[] dues = new int[numjob];
                    int it = 0;
                    for (Integer job : jobs) {
                        for (int j = 0; j < totjobs; j++) {
                            if (job == (sheet.getRow(j + 1).getCell(0).getNumericCellValue())) {
                                autocaps[it] = (int) sheet.getRow(j + 1).getCell(11).getNumericCellValue();
                                sizes[it] = (int) sheet.getRow(j + 1).getCell(13).getNumericCellValue();
                                partFams[it] = sheet.getRow(j + 1).getCell(12).getStringCellValue();
                                dues[it++] = (int) Math.ceil(Math.random() * 4);
                                break;
                            }
                        }
                    }

                    // Add sampled jobs to instance
                    sheet = workbook.getSheet("order_jobs");
                    sheet.getRow(0).createCell(6).setCellValue("auto_end");
                    sheet.getRow(0).createCell(7).setCellValue("order");
                    sheet.getRow(0).createCell(8).setCellValue("prep_start");
                    sheet.getRow(0).createCell(9).setCellValue("prep_end");
                    sheet.getRow(0).createCell(10).setCellValue("layup_start");
                    sheet.getRow(0).createCell(11).setCellValue("layup_end");
                    sheet.getRow(0).createCell(12).setCellValue("auto_start");
                    sheet.getRow(0).createCell(13).setCellValue("demould_start");
                    sheet.getRow(0).createCell(14).setCellValue("demould_end");
                    sheet.getRow(0).createCell(15).setCellValue("tardiness");
                    for (int j = 0; j < numjob; j++) {
                        sheet.createRow(j + 1).createCell(0).setCellValue(j);
                        sheet.getRow(j + 1).createCell(1).setCellValue(jobs.get(j));
                        sheet.getRow(j + 1).createCell(2).setCellValue(dues[j]);
                        sheet.getRow(j + 1).createCell(3).setCellValue(sizes[j]);
                        sheet.getRow(j + 1).createCell(4).setCellValue(autocaps[j]);
                        sheet.getRow(j + 1).createCell(5).setCellValue(partFams[j]);
                    }

                    // Create results page and add headings
                    sheet = workbook.createSheet("results");
                    sheet.createRow(0).createCell(3).setCellValue("job");
                    sheet.getRow(0).createCell(4).setCellValue("job_to_b0");
                    sheet.getRow(0).createCell(5).setCellValue("job_top_tool");
                    sheet.getRow(0).createCell(6).setCellValue("job_prep_start");
                    sheet.getRow(0).createCell(7).setCellValue("job_prep_end");
                    sheet.getRow(0).createCell(8).setCellValue("job_layup_start");
                    sheet.getRow(0).createCell(9).setCellValue("job_layup_end");
                    sheet.getRow(0).createCell(10).setCellValue("job_auto_start");
                    sheet.getRow(0).createCell(11).setCellValue("job_auto_end");
                    sheet.getRow(0).createCell(12).setCellValue("job_demould_start");
                    sheet.getRow(0).createCell(13).setCellValue("job_demould_end");
                    sheet.getRow(0).createCell(14).setCellValue("job_tardiness");
                    sheet.getRow(0).createCell(16).setCellValue("b0");
                    sheet.getRow(0).createCell(17).setCellValue("b0_to_b1");
                    sheet.getRow(0).createCell(18).setCellValue("b0_top_tool");
                    sheet.getRow(0).createCell(20).setCellValue("b1");
                    sheet.getRow(0).createCell(21).setCellValue("b1_to_b2");
                    sheet.getRow(0).createCell(22).setCellValue("b1_bottom_tool");
                    sheet.getRow(0).createCell(23).setCellValue("b1_size");
                    sheet.getRow(0).createCell(24).setCellValue("b1_prep_start");
                    sheet.getRow(0).createCell(25).setCellValue("b1_prep_end");
                    sheet.getRow(0).createCell(26).setCellValue("b1_prep_labour");
                    sheet.getRow(0).createCell(27).setCellValue("b1_prep_labour_qty");
                    sheet.getRow(0).createCell(28).setCellValue("b1_prep_machine");
                    sheet.getRow(0).createCell(29).setCellValue("b1_layup_start");
                    sheet.getRow(0).createCell(30).setCellValue("b1_layup_end");
                    sheet.getRow(0).createCell(31).setCellValue("b1_layup_labour");
                    sheet.getRow(0).createCell(32).setCellValue("b1_layup_labour_qty");
                    sheet.getRow(0).createCell(33).setCellValue("b1_layup_machine");
                    sheet.getRow(0).createCell(34).setCellValue("b1_demould_start");
                    sheet.getRow(0).createCell(35).setCellValue("b1_demould_end");
                    sheet.getRow(0).createCell(36).setCellValue("b1_demould_labour");
                    sheet.getRow(0).createCell(37).setCellValue("b1_demould_labour_qty");
                    sheet.getRow(0).createCell(38).setCellValue("b1_demould_machine");
                    sheet.getRow(0).createCell(40).setCellValue("b2");
                    sheet.getRow(0).createCell(41).setCellValue("b2_capacity");
                    sheet.getRow(0).createCell(42).setCellValue("b2_tools_size");
                    sheet.getRow(0).createCell(43).setCellValue("b2_auto_start");
                    sheet.getRow(0).createCell(44).setCellValue("b2_auto_end");
                    sheet.getRow(0).createCell(45).setCellValue("b2_auto_machine");

                    // Create quality over time page and add headings
                    sheet = workbook.createSheet("quality_over_time");
                    sheet.createRow(0).createCell(0).setCellValue("instance");
                    sheet.getRow(0).createCell(1).setCellValue("quality");
                    sheet.getRow(0).createCell(2).setCellValue("time");

                    // Add missing params to single_param page
                    ArrayList<Integer> autocount = new ArrayList<>();
                    for (Integer auto : autocaps) {
                        if (!autocount.contains(auto)) {
                            autocount.add(auto);
                        }
                    }
                    sheet = workbook.getSheet("single_param");
                    sheet.getRow(1).createCell(1).setCellValue(numjob);
                    sheet.getRow(3).createCell(1).setCellValue(numjob);
                    sheet.getRow(4).createCell(1).setCellValue(numjob);
                    sheet.getRow(5).createCell(1).setCellValue(numjob);
                    sheet.getRow(16).createCell(1).setCellValue(autocount.size());
                    sheet.getRow(17).createCell(1).setCellValue(150 * numjob);

                    // Write output and close workbook
                    workbook.write(new FileOutputStream(currentFile));
                    workbook.close();

                }

                // Create summary file/pages and add headings
                String summaryFile = currentFolder + "/summary.xlsx";
                XSSFWorkbook summary = new XSSFWorkbook();
                XSSFSheet sheet = summary.createSheet("rsp");
                sheet.createRow(0).createCell(0).setCellValue("instance");
                sheet.getRow(0).createCell(1).setCellValue("numjobs");
                sheet.getRow(0).createCell(2).setCellValue("status");
                sheet.getRow(0).createCell(3).setCellValue("tardiness");
                sheet.getRow(0).createCell(4).setCellValue("elapsed_time");
                sheet = summary.createSheet("current_exp_pack");
                sheet.createRow(0).createCell(0).setCellValue("instance");
                sheet.getRow(0).createCell(1).setCellValue("numjobs");
                sheet.getRow(0).createCell(2).setCellValue("status");
                sheet.getRow(0).createCell(3).setCellValue("numbins");
                sheet.getRow(0).createCell(4).setCellValue("elapsed_time");
                sheet.getRow(0).createCell(5).setCellValue("infeas_bins");
                sheet = summary.createSheet("current_exp_sched");
                sheet.createRow(0).createCell(0).setCellValue("instance");
                sheet.getRow(0).createCell(1).setCellValue("numjobs");
                sheet.getRow(0).createCell(2).setCellValue("status");
                sheet.getRow(0).createCell(3).setCellValue("tardiness");
                sheet.getRow(0).createCell(4).setCellValue("elapsed_time");
                sheet = summary.createSheet("current_exp");
                sheet.createRow(0).createCell(0).setCellValue("instance");
                sheet.getRow(0).createCell(1).setCellValue("status");
                sheet.getRow(0).createCell(2).setCellValue("numbins");
                sheet.getRow(0).createCell(3).setCellValue("tardiness");
                sheet.getRow(0).createCell(4).setCellValue("elapsed_time_to_best");
                sheet.getRow(0).createCell(5).setCellValue("iterations");
                sheet.getRow(0).createCell(6).setCellValue("over_hour");
                sheet.getRow(0).createCell(7).setCellValue("elapsed_time");
                sheet.getRow(0).createCell(8).setCellValue("non_optimal");
                sheet.getRow(0).createCell(9).setCellValue("infeas_bins");
                sheet = summary.createSheet("solution_check");
                sheet.createRow(0).createCell(0).setCellValue("instance");
                sheet.getRow(0).createCell(1).setCellValue("status");
                sheet.getRow(0).createCell(2).setCellValue("mistakes");
                summary.write(new FileOutputStream(summaryFile));
                summary.close();

            } else {

                // Directory could not be created
                throw new Exception("Directory not created for " + numjob + " jobs");

            }

        }

    }

}
