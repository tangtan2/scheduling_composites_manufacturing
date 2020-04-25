package common.model_helpers;
import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class Data {

    // Filepaths and problem instance
    private final String filePath;
    private final String summaryFilePath;
    private final int problemInstance;

    // Instance parameters
    public int numJob;
    public int numTotalJobs;
    public int numB0;
    public int numB1;
    public int numB2;
    public int numBottomTool;
    public int numTopTool;
    public int numTopToBottom;
    public int numJobToTool;
    public int numMachine;
    public int numStepToMachine;
    public int numShift;
    public int numLabour;
    public int numLabourSkill;
    public int numLabourSkillToMachine;
    public int numDistinctAuto;
    public int horizon;
    public int restrictedWaitTime;
    public String[] jobName;
    public int[] jobDue;
    public int[] jobSize;
    public int[] jobAutoCap;
    public String[] jobPartFamily;
    public int[] jobRSPOrder;
    public String[][] jobSteps;
    public int[][] jobStepTimes;
    public String[] bottomToolName;
    public int[] bottomToolMin;
    public int[] bottomToolCap;
    public int[] bottomToolQty;
    public int[] bottomToolSize;
    public String[] topToolName;
    public int[] topToolMin;
    public int[] topToolCap;
    public int[] topToolQty;
    public String[][] topToBottom;
    public String[][] jobToTool;
    public String[] machineName;
    public int[] machineQty;
    public int[] machineCap;
    public String[][] stepToMachine;
    public int[] stepTimeByBatchType;
    public String[] shiftName;
    public int[] shiftStart;
    public int[] shiftEnd;
    public int[][] machineShift;
    public String[] labourName;
    public String[] labourSkillName;
    public int[][] labourMatrix;
    public String[][] labourToMachine;
    public int[] labourQtyRequired;
    public int[][] labourShift;

    // Packing solution parameters
    public int numB0_new;
    public int numB1_new;
    public int numB2_new;
    public int[] jobIndices;
    public int[] jobToB0;
    public String[] jobToTopTool;
    public int[] b0Indices;
    public String[] b0TopTool;
    public int[] b0ToB1;
    public int[] b1Indices;
    public int[] b1ToB2;
    public String[] b1BottomTool;
    public int[] b1Vol;
    public int[] b2Indices;
    public int[] b2Cap;
    public int[] b2SumOfToolVol;

    // Scheduling solution parameters
    public int[] jobPrepS;
    public int[] jobPrepE;
    public int[] jobLayupS;
    public int[] jobLayupE;
    public int[] jobAutoS;
    public int[] jobAutoE;
    public int[] jobDemouldS;
    public int[] jobDemouldE;
    public int[] jobTardiness;
    public int[] b1PrepS;
    public int[] b1PrepE;
    public String[] b1PrepLabour;
    public int[] b1PrepQty;
    public String[] b1PrepMachine;
    public int[] b1LayupS;
    public int[] b1LayupE;
    public String[] b1LayupLabour;
    public int[] b1LayupQty;
    public String[] b1LayupMachine;
    public int[] b1DemouldS;
    public int[] b1DemouldE;
    public String[] b1DemouldLabour;
    public int[] b1DemouldQty;
    public String[] b1DemouldMachine;
    public int[] b2AutoS;
    public int[] b2AutoE;
    public String[] b2AutoMachine;

    // Constructor
    public Data(String path, String sum, int pi) {

        this.filePath = path;
        this.summaryFilePath = sum;
        this.problemInstance = pi;

    }

    // Get instance parameters
    public void readInstanceParams() {

        try {

            Workbook workbook = new XSSFWorkbook(this.filePath);
            Sheet sheet = workbook.getSheet("single_param");
            this.numJob = (int) sheet.getRow(1).getCell(1).getNumericCellValue();
            this.numTotalJobs = (int) sheet.getRow(2).getCell(1).getNumericCellValue();
            this.numB0 = (int) sheet.getRow(3).getCell(1).getNumericCellValue();
            this.numB1 = (int) sheet.getRow(4).getCell(1).getNumericCellValue();
            this.numB2 = (int) sheet.getRow(5).getCell(1).getNumericCellValue();
            this.numBottomTool = (int) sheet.getRow(6).getCell(1).getNumericCellValue();
            this.numTopTool = (int) sheet.getRow(7).getCell(1).getNumericCellValue();
            this.numTopToBottom = (int) sheet.getRow(8).getCell(1).getNumericCellValue();
            this.numJobToTool = (int) sheet.getRow(9).getCell(1).getNumericCellValue();
            this.numMachine = (int) sheet.getRow(10).getCell(1).getNumericCellValue();
            this.numStepToMachine = (int) sheet.getRow(11).getCell(1).getNumericCellValue();
            this.numShift = (int) sheet.getRow(12).getCell(1).getNumericCellValue();
            this.numLabour = (int) sheet.getRow(13).getCell(1).getNumericCellValue();
            this.numLabourSkill = (int) sheet.getRow(14).getCell(1).getNumericCellValue();
            this.numLabourSkillToMachine = (int) sheet.getRow(15).getCell(1).getNumericCellValue();
            this.numDistinctAuto = (int) sheet.getRow(16).getCell(1).getNumericCellValue();
            this.horizon = (int) sheet.getRow(17).getCell(1).getNumericCellValue();
            this.restrictedWaitTime = (int) sheet.getRow(18).getCell(1).getNumericCellValue();
            sheet = workbook.getSheet("order_jobs");
            this.jobName = new String[this.numJob];
            this.jobDue = new int[this.numJob];
            this.jobSize = new int[this.numJob];
            this.jobAutoCap = new int[this.numJob];
            this.jobPartFamily = new String[this.numJob];
            this.jobRSPOrder = new int[this.numJob];
            for (int i = 0; i < this.numJob; i++) {
                this.jobName[i] = Integer.toString((int) sheet.getRow(i + 1).getCell(1).getNumericCellValue());
                this.jobDue[i] = (int) sheet.getRow(i + 1).getCell(2).getNumericCellValue() * 7 * 24 * 60;
                this.jobSize[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                this.jobAutoCap[i] = (int) sheet.getRow(i + 1).getCell(4).getNumericCellValue();
                this.jobPartFamily[i] = sheet.getRow(i + 1).getCell(5).getStringCellValue();
                try {
                    this.jobRSPOrder[i] = (int) sheet.getRow(i + 1).getCell(6).getNumericCellValue();
                } catch (Exception ignored) {}
            }
            sheet = workbook.getSheet("jobs");
            this.jobSteps = new String[this.numJob][4];
            this.jobStepTimes = new int[this.numJob][4];
            for (int i = 0; i < this.numJob; i++) {
                for (int j = 0; j < this.numTotalJobs; j++) {
                    if (Integer.toString((int) sheet.getRow(j + 1).getCell(0).getNumericCellValue()).equals(this.jobName[i])) {
                        for (int k = 0; k < 4; k++) {
                            this.jobSteps[i][k] = sheet.getRow(j + 1).getCell(3 + 2 * k).getStringCellValue();
                            if (sheet.getRow(j + 1).getCell(3 + 2 * k).getStringCellValue().contains("Autoclave")) {
                                this.jobStepTimes[i][k] = 0;
                            } else {
                                this.jobStepTimes[i][k] = (int) sheet.getRow(j + 1).getCell(4 + 2 * k).getNumericCellValue();
                            }
                        }
                        break;
                    }
                }
            }
            sheet = workbook.getSheet("bottom_tools");
            this.bottomToolName = new String[this.numBottomTool];
            this.bottomToolCap = new int[this.numBottomTool];
            this.bottomToolQty = new int[this.numBottomTool];
            this.bottomToolSize = new int[this.numBottomTool];
            this.bottomToolMin = new int[this.numBottomTool];
            for (int i = 0; i < this.numBottomTool; i++) {
                this.bottomToolName[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.bottomToolCap[i] = (int) sheet.getRow(i + 1).getCell(2).getNumericCellValue();
                this.bottomToolQty[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                this.bottomToolSize[i] = (int) sheet.getRow(i + 1).getCell(4).getNumericCellValue();
                if (sheet.getRow(i + 1).getCell(1).getStringCellValue().equals("Yes")) {
                    this.bottomToolMin[i] = this.bottomToolCap[i];
                } else {
                    this.bottomToolMin[i] = 0;
                }
            }
            sheet = workbook.getSheet("top_tools");
            this.topToolName = new String[this.numTopTool];
            this.topToolCap = new int[this.numTopTool];
            this.topToolQty = new int[this.numTopTool];
            this.topToolMin = new int[this.numTopTool];
            for (int i = 0; i < this.numTopTool; i++) {
                this.topToolName[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.topToolCap[i] = (int) sheet.getRow(i + 1).getCell(2).getNumericCellValue();
                this.topToolQty[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                if (sheet.getRow(i + 1).getCell(1).getStringCellValue().equals("Yes")) {
                    this.topToolMin[i] = this.topToolCap[i];
                } else {
                    this.topToolMin[i] = 0;
                }
            }
            sheet = workbook.getSheet("top_to_bottom");
            this.topToBottom = new String[this.numTopToBottom][2];
            for (int i = 0; i < this.numTopToBottom; i++) {
                this.topToBottom[i][0] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.topToBottom[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
            }
            sheet = workbook.getSheet("job_to_tool");
            this.jobToTool = new String[this.numJobToTool][3];
            for (int i = 0; i < this.numJobToTool; i++) {
                this.jobToTool[i][0] = Integer.toString((int) sheet.getRow(i + 1).getCell(0).getNumericCellValue());
                this.jobToTool[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
                this.jobToTool[i][2] = sheet.getRow(i + 1).getCell(2).getStringCellValue();
            }
            sheet = workbook.getSheet("machines");
            this.machineName = new String[this.numMachine];
            this.machineQty = new int[this.numMachine];
            this.machineCap = new int[this.numMachine];
            for (int i = 0; i < this.numMachine; i++) {
                this.machineName[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.machineQty[i] = (int) sheet.getRow(i + 1).getCell(1).getNumericCellValue();
                this.machineCap[i] = (int) sheet.getRow(i + 1).getCell(5).getNumericCellValue();
            }
            sheet = workbook.getSheet("batch_to_machine");
            this.stepToMachine = new String[this.numStepToMachine][2];
            this.stepTimeByBatchType = new int[this.numStepToMachine];
            for (int i = 0; i < this.numStepToMachine; i++) {
                this.stepToMachine[i][0] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.stepToMachine[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
                this.stepTimeByBatchType[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
            }
            for (int i = 0; i < this.numJob; i++) {
                for (int j = 0; j < this.numStepToMachine; j++) {
                    if (this.jobSteps[i][2].equals(this.stepToMachine[j][1])) {
                        jobStepTimes[i][2] = this.stepTimeByBatchType[j];
                        break;
                    }
                }
            }
            sheet = workbook.getSheet("machine_shifts");
            this.shiftName = new String[this.numShift];
            this.shiftStart = new int[this.numShift];
            this.shiftEnd = new int[this.numShift];
            this.machineShift = new int[this.numMachine][this.numShift];
            int shift = 1;
            int day = 0;
            for (int i = 0; i < this.numShift; i++) {
                String name = sheet.getRow(0).getCell(i + 1).getStringCellValue() + "-" + shift;
                this.shiftName[i] = name;
                this.shiftStart[i] = (int) Math.round((sheet.getRow(1).getCell(i + 1).getNumericCellValue() + day) * 24 * 60);
                shift++;
                if (shift > 7) {
                    shift = 1;
                    day++;
                }
                this.shiftEnd[i] = (int) Math.round((sheet.getRow(2).getCell(i + 1).getNumericCellValue() + day) * 24 * 60);
            }
            for (int i = 0; i < this.numShift; i++) {
                for (int j = 0; j < this.numMachine; j++) {
                    if (sheet.getRow(3 + j).getCell(1 + i).getStringCellValue().equals("ON")) {
                        this.machineShift[j][i] = this.machineQty[j];
                    } else {
                        this.machineShift[j][i] = 0;
                    }
                }
            }
            sheet = workbook.getSheet("labour_skills");
            this.labourName = new String[this.numLabour];
            this.labourSkillName = new String[this.numLabourSkill];
            this.labourMatrix = new int[this.numLabour][this.numLabourSkill];
            for (int i = 0; i < this.numLabour; i++) {
                this.labourName[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
            }
            for (int i = 0; i < this.numLabourSkill; i++) {
                this.labourSkillName[i] = sheet.getRow(0).getCell(i + 1).getStringCellValue();
            }
            for (int i = 0; i < this.numLabour; i++) {
                for (int j = 0; j < this.numLabourSkill; j++) {
                    this.labourMatrix[i][j] = (int) sheet.getRow(i + 1).getCell(j + 1).getNumericCellValue();
                }
            }
            sheet = workbook.getSheet("labour_to_machine");
            this.labourToMachine = new String[this.numLabourSkillToMachine][3];
            this.labourQtyRequired = new int[this.numLabourSkillToMachine];
            for (int i = 0; i < this.numLabourSkillToMachine; i++) {
                this.labourToMachine[i][0] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.labourToMachine[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
                this.labourToMachine[i][2] = sheet.getRow(i + 1).getCell(2).getStringCellValue();
                this.labourQtyRequired[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
            }
            sheet = workbook.getSheet("labour_shifts");
            this.labourShift = new int[this.numLabour][this.numShift];
            for (int i = 0; i < this.numShift; i++) {
                for (int j = 0; j < this.numLabour; j++) {
                    this.labourShift[j][i] = (int) sheet.getRow(3 + j).getCell(1 + i).getNumericCellValue();
                }
            }
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Get packing solution parameters
    public void readPackParams() {

        try {

            Workbook workbook = new XSSFWorkbook(this.filePath);
            Sheet sheet = workbook.getSheet("single_param");
            this.numB0_new = (int) sheet.getRow(3).getCell(1).getNumericCellValue();
            this.numB1_new = (int) sheet.getRow(4).getCell(1).getNumericCellValue();
            this.numB2_new = (int) sheet.getRow(5).getCell(1).getNumericCellValue();
            sheet = workbook.getSheet("results");
            this.jobIndices = new int[this.numJob];
            this.jobToB0 = new int[this.numJob];
            this.jobToTopTool = new String[this.numJob];
            this.b0Indices = new int[this.numB0_new];
            this.b0TopTool = new String[this.numB0_new];
            this.b0ToB1 = new int[this.numB0_new];
            this.b1Indices = new int[this.numB1_new];
            this.b1ToB2 = new int[this.numB1_new];
            this.b1BottomTool = new String[this.numB1_new];
            this.b1Vol = new int[this.numB1_new];
            this.b2Indices = new int[this.numB2_new];
            this.b2Cap = new int[this.numB2_new];
            this.b2SumOfToolVol = new int[this.numB2_new];
            for (int i = 0; i < this.numJob; i++) {
                this.jobIndices[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                this.jobToB0[i] = (int) sheet.getRow(i + 1).getCell(4).getNumericCellValue();
                this.jobToTopTool[i] = sheet.getRow(i + 1).getCell(5).getStringCellValue();
            }
            for (int i = 0; i < this.numB0_new; i++) {
                this.b0Indices[i] = (int) sheet.getRow(i + 1).getCell(16).getNumericCellValue();
                this.b0ToB1[i] = (int) sheet.getRow(i + 1).getCell(17).getNumericCellValue();
                this.b0TopTool[i] = sheet.getRow(i + 1).getCell(18).getStringCellValue();
            }
            for (int i = 0; i < this.numB1_new; i++) {
                this.b1Indices[i] = (int) sheet.getRow(i + 1).getCell(20).getNumericCellValue();
                this.b1ToB2[i] = (int) sheet.getRow(i + 1).getCell(21).getNumericCellValue();
                this.b1BottomTool[i] = sheet.getRow(i + 1).getCell(22).getStringCellValue();
                this.b1Vol[i] = (int) sheet.getRow(i + 1).getCell(23).getNumericCellValue();
            }
            for (int i = 0; i < this.numB2_new; i++) {
                this.b2Indices[i] = (int) sheet.getRow(i + 1).getCell(40).getNumericCellValue();
                this.b2Cap[i] = (int) sheet.getRow(i + 1).getCell(41).getNumericCellValue();
                this.b2SumOfToolVol[i] = (int) sheet.getRow(i + 1).getCell(42).getNumericCellValue();
            }
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Get packing solution parameters
    public void readSchedParams() {

        try {

            Workbook workbook = new XSSFWorkbook(this.filePath);
            Sheet sheet = workbook.getSheet("results");
            this.jobPrepS = new int[this.numJob];
            this.jobPrepE = new int[this.numJob];
            this.jobLayupS = new int[this.numJob];
            this.jobLayupE = new int[this.numJob];
            this.jobAutoS = new int[this.numJob];
            this.jobAutoE = new int[this.numJob];
            this.jobDemouldS = new int[this.numJob];
            this.jobDemouldE = new int[this.numJob];
            this.jobTardiness = new int[this.numJob];
            this.b1PrepS = new int[this.numB1_new];
            this.b1PrepE = new int[this.numB1_new];
            this.b1PrepLabour = new String[this.numB1_new];
            this.b1PrepQty = new int[this.numB1_new];
            this.b1PrepMachine = new String[this.numB1_new];
            this.b1LayupS = new int[this.numB1_new];
            this.b1LayupE = new int[this.numB1_new];
            this.b1LayupLabour = new String[this.numB1_new];
            this.b1LayupQty = new int[this.numB1_new];
            this.b1LayupMachine = new String[this.numB1_new];
            this.b1DemouldS = new int[this.numB1_new];
            this.b1DemouldE = new int[this.numB1_new];
            this.b1DemouldLabour = new String[this.numB1_new];
            this.b1DemouldQty = new int[this.numB1_new];
            this.b1DemouldMachine = new String[this.numB1_new];
            this.b2AutoS = new int[this.numB2_new];
            this.b2AutoE = new int[this.numB2_new];
            this.b2AutoMachine = new String[this.numB2_new];
            for (int i = 0; i < this.numJob; i++) {
                this.jobPrepS[i] = (int) sheet.getRow(i + 1).getCell(6).getNumericCellValue();
                this.jobPrepE[i] = (int) sheet.getRow(i + 1).getCell(7).getNumericCellValue();
                this.jobLayupS[i] = (int) sheet.getRow(i + 1).getCell(8).getNumericCellValue();
                this.jobLayupE[i] = (int) sheet.getRow(i + 1).getCell(9).getNumericCellValue();
                this.jobAutoS[i] = (int) sheet.getRow(i + 1).getCell(10).getNumericCellValue();
                this.jobAutoE[i] = (int) sheet.getRow(i + 1).getCell(11).getNumericCellValue();
                this.jobDemouldS[i] = (int) sheet.getRow(i + 1).getCell(12).getNumericCellValue();
                this.jobDemouldE[i] = (int) sheet.getRow(i + 1).getCell(13).getNumericCellValue();
                this.jobTardiness[i] = (int) sheet.getRow(i + 1).getCell(14).getNumericCellValue();
            }
            for (int i = 0; i < this.numB1_new; i++) {
                this.b1PrepS[i] = (int) sheet.getRow(i + 1).getCell(24).getNumericCellValue();
                this.b1PrepE[i] = (int) sheet.getRow(i + 1).getCell(25).getNumericCellValue();
                this.b1PrepLabour[i] = sheet.getRow(i + 1).getCell(26).getStringCellValue();
                this.b1PrepQty[i] = (int) sheet.getRow(i + 1).getCell(27).getNumericCellValue();
                this.b1PrepMachine[i] = sheet.getRow(i + 1).getCell(28).getStringCellValue();
                this.b1LayupS[i] = (int) sheet.getRow(i + 1).getCell(29).getNumericCellValue();
                this.b1LayupE[i] = (int) sheet.getRow(i + 1).getCell(30).getNumericCellValue();
                this.b1LayupLabour[i] = sheet.getRow(i + 1).getCell(31).getStringCellValue();
                this.b1LayupQty[i] = (int) sheet.getRow(i + 1).getCell(32).getNumericCellValue();
                this.b1LayupMachine[i] = sheet.getRow(i + 1).getCell(33).getStringCellValue();
                this.b1DemouldS[i] = (int) sheet.getRow(i + 1).getCell(34).getNumericCellValue();
                this.b1DemouldE[i] = (int) sheet.getRow(i + 1).getCell(35).getNumericCellValue();
                this.b1DemouldLabour[i] = sheet.getRow(i + 1).getCell(36).getStringCellValue();
                this.b1DemouldQty[i] = (int) sheet.getRow(i + 1).getCell(37).getNumericCellValue();
                this.b1DemouldMachine[i] =sheet.getRow(i + 1).getCell(38).getStringCellValue();
            }
            for (int i = 0; i < this.numB2_new; i++) {
                this.b2AutoS[i] = (int) sheet.getRow(i + 1).getCell(43).getNumericCellValue();
                this.b2AutoE[i] = (int) sheet.getRow(i + 1).getCell(44).getNumericCellValue();
                this.b2AutoMachine[i] = sheet.getRow(i + 1).getCell(45).getStringCellValue();
            }
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write RSP solution
    public void writeRSP(SolutionRSP soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filePath));
            XSSFSheet sheet = workbook.getSheet("order_jobs");
            TreeMap<Integer, ArrayList<JobRSP>> map = new TreeMap<>();
            for (JobRSP j : soln.RSPjobs()) {
                if (map.containsKey(j.autoEnd())) {
                    map.get(j.autoEnd()).add(j);
                } else {
                    map.put(j.autoEnd(), new ArrayList<>(Collections.singletonList(j)));
                }
            }
            int it = 0;
            for (Map.Entry<Integer, ArrayList<JobRSP>> entry : map.entrySet()) {
                for (JobRSP j : entry.getValue()) {
                    sheet.getRow(j.index() + 1).createCell(6).setCellValue(entry.getKey());
                    sheet.getRow(j.index() + 1).createCell(7).setCellValue(it + 1);
                    sheet.getRow(j.index() + 1).createCell(8).setCellValue(j.prepStart());
                    sheet.getRow(j.index() + 1).createCell(9).setCellValue(j.prepEnd());
                    sheet.getRow(j.index() + 1).createCell(10).setCellValue(j.layupStart());
                    sheet.getRow(j.index() + 1).createCell(11).setCellValue(j.layupEnd());
                    sheet.getRow(j.index() + 1).createCell(12).setCellValue(j.autoStart());
                    sheet.getRow(j.index() + 1).createCell(13).setCellValue(j.demouldStart());
                    sheet.getRow(j.index() + 1).createCell(14).setCellValue(j.demouldEnd());
                    sheet.getRow(j.index() + 1).createCell(15).setCellValue(j.tardiness());
                    it++;
                }
            }
            workbook.write(new FileOutputStream(this.filePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write packing solution
    public void writePack(SolutionPack soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filePath));
            XSSFSheet sheet = workbook.getSheet("single_param");
            sheet.getRow(3).getCell(1).setCellValue(soln.b0s().size());
            sheet.getRow(4).getCell(1).setCellValue(soln.b1s().size());
            sheet.getRow(5).getCell(1).setCellValue(soln.b2s().size());
            sheet = workbook.getSheet("results");
            for (int i = 0; i < soln.numJob(); i++) {
                sheet.createRow(i + 1).createCell(3).setCellValue(soln.jobs().get(i).index());
                sheet.getRow(i + 1).createCell(4).setCellValue(soln.jobs().get(i).b0().index());
                sheet.getRow(i + 1).createCell(5).setCellValue(soln.jobs().get(i).b0().topTool().name());
            }
            for (int i = 0; i < soln.b0s().size(); i++) {
                sheet.getRow(i + 1).createCell(16).setCellValue(soln.b0s().get(i).index());
                sheet.getRow(i + 1).createCell(17).setCellValue(soln.b0s().get(i).b1().index());
                sheet.getRow(i + 1).createCell(18).setCellValue(soln.b0s().get(i).topTool().name());
            }
            for (int i = 0; i < soln.b1s().size(); i++) {
                sheet.getRow(i + 1).createCell(20).setCellValue(soln.b1s().get(i).index());
                sheet.getRow(i + 1).createCell(21).setCellValue(soln.b1s().get(i).b2().index());
                sheet.getRow(i + 1).createCell(22).setCellValue(soln.b1s().get(i).bottomTool().name());
                sheet.getRow(i + 1).createCell(23).setCellValue(soln.b1s().get(i).size());
            }
            for (int i = 0; i < soln.b2s().size(); i++) {
                sheet.getRow(i + 1).createCell(40).setCellValue(soln.b2s().get(i).index());
                sheet.getRow(i + 1).createCell(41).setCellValue(soln.b2s().get(i).capacity());
                sheet.getRow(i + 1).createCell(42).setCellValue(soln.b2s().get(i).sumToolSize());
                sheet.getRow(i + 1).createCell(46).setCellValue(soln.b2s().get(i).RSPdist());
            }
            workbook.write(new FileOutputStream(this.filePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write scheduling solution
    public void writeSched(SolutionSched soln) {

        if (soln.b2sS().size() > 0) {

            try {

                XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filePath));
                XSSFSheet sheet = workbook.getSheet("results");
                for (AutoBatchS a : soln.b2sS()) {
                    for (JobS j : a.jobsS()) {
                        for (int i = 0; i < this.numJob; i++) {
                            if (this.jobIndices[i] == j.index()) {
                                sheet.getRow(i + 1).createCell(6).setCellValue(j.prepStart());
                                sheet.getRow(i + 1).createCell(7).setCellValue(j.prepEnd());
                                sheet.getRow(i + 1).createCell(8).setCellValue(j.layupStart());
                                sheet.getRow(i + 1).createCell(9).setCellValue(j.layupEnd());
                                sheet.getRow(i + 1).createCell(10).setCellValue(j.autoStart());
                                sheet.getRow(i + 1).createCell(11).setCellValue(j.autoEnd());
                                sheet.getRow(i + 1).createCell(12).setCellValue(j.demouldStart());
                                sheet.getRow(i + 1).createCell(13).setCellValue(j.demouldEnd());
                                sheet.getRow(i + 1).createCell(14).setCellValue(j.tardiness());
                                break;
                            }
                        }
                    }
                    for (ToolBatchS t : a.b1sS()) {
                        for (int i = 0; i < this.numB1_new; i++) {
                            if (this.b1Indices[i] == t.index()) {
                                sheet.getRow(i + 1).createCell(24).setCellValue(t.prepStart());
                                sheet.getRow(i + 1).createCell(25).setCellValue(t.prepEnd());
                                sheet.getRow(i + 1).createCell(26).setCellValue(t.prepLabour().name());
                                sheet.getRow(i + 1).createCell(27).setCellValue(t.prepQty());
                                sheet.getRow(i + 1).createCell(28).setCellValue(t.prepMachine().name());
                                sheet.getRow(i + 1).createCell(29).setCellValue(t.layupStart());
                                sheet.getRow(i + 1).createCell(30).setCellValue(t.layupEnd());
                                sheet.getRow(i + 1).createCell(31).setCellValue(t.layupLabour().name());
                                sheet.getRow(i + 1).createCell(32).setCellValue(t.layupQty());
                                sheet.getRow(i + 1).createCell(33).setCellValue(t.layupMachine().name());
                                sheet.getRow(i + 1).createCell(34).setCellValue(t.demouldStart());
                                sheet.getRow(i + 1).createCell(35).setCellValue(t.demouldEnd());
                                sheet.getRow(i + 1).createCell(36).setCellValue(t.demouldLabour().name());
                                sheet.getRow(i + 1).createCell(37).setCellValue(t.demouldQty());
                                sheet.getRow(i + 1).createCell(38).setCellValue(t.demouldMachine().name());
                                break;
                            }
                        }
                    }
                    for (int i = 0; i < this.numB2_new; i++) {
                        if (this.b2Indices[i] == a.index()) {
                            sheet.getRow(i + 1).createCell(43).setCellValue(a.autoStart());
                            sheet.getRow(i + 1).createCell(44).setCellValue(a.autoEnd());
                            sheet.getRow(i + 1).createCell(45).setCellValue(a.autoMachine().name());
                            break;
                        }
                    }
                }
                sheet.getRow(0).createCell(0).setCellValue("obj_val");
                sheet.getRow(0).createCell(1).setCellValue(soln.objVal());
                workbook.write(new FileOutputStream(this.filePath));
                workbook.close();

            } catch (Exception e) {

                e.printStackTrace();

            }

        } else if (soln.acts().size() > 0) {

            try {

                XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filePath));
                XSSFSheet sheet = workbook.getSheet("results");
                for (Activity a : soln.acts()) {
                    if (a.type() == 0) {
                        for (Job j : a.b1().jobs()) {
                            for (int i = 0; i < this.numJob; i++) {
                                if (this.jobIndices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(6).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(7).setCellValue(a.end());
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < this.numB1_new; i++) {
                            if (this.b1Indices[i] == a.b1().index()) {
                                sheet.getRow(i + 1).createCell(24).setCellValue(a.start());
                                sheet.getRow(i + 1).createCell(25).setCellValue(a.end());
                                sheet.getRow(i + 1).createCell(26).setCellValue(a.b1().prepLabourH().name());
                                sheet.getRow(i + 1).createCell(27).setCellValue(a.b1().prepQty());
                                sheet.getRow(i + 1).createCell(28).setCellValue(a.b1().prepMachineH().name());
                                break;
                            }
                        }
                    } else if (a.type() == 1) {
                        for (Job j : a.b1().jobs()) {
                            for (int i = 0; i < this.numJob; i++) {
                                if (this.jobIndices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(8).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(9).setCellValue(a.end());
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < this.numB1_new; i++) {
                            if (this.b1Indices[i] == a.b1().index()) {
                                sheet.getRow(i + 1).createCell(29).setCellValue(a.start());
                                sheet.getRow(i + 1).createCell(30).setCellValue(a.end());
                                sheet.getRow(i + 1).createCell(31).setCellValue(a.b1().layupLabourH().name());
                                sheet.getRow(i + 1).createCell(32).setCellValue(a.b1().layupQty());
                                sheet.getRow(i + 1).createCell(33).setCellValue(a.b1().layupMachineH().name());
                                break;
                            }
                        }
                    } else if (a.type() == 2) {
                        for (Job j : a.b2().jobs()) {
                            for (int i = 0; i < this.numJob; i++) {
                                if (this.jobIndices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(10).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(11).setCellValue(a.end());
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < numB2_new; i++) {
                            if (this.b2Indices[i] == a.b2().index()) {
                                sheet.getRow(i + 1).createCell(43).setCellValue(a.start());
                                sheet.getRow(i + 1).createCell(44).setCellValue(a.end());
                                sheet.getRow(i + 1).createCell(45).setCellValue(a.b2().autoMachineH().name());
                                break;
                            }
                        }
                    } else if (a.type() == 3) {
                        for (Job j : a.b1().jobs()) {
                            for (int i = 0; i < this.numJob; i++) {
                                if (this.jobIndices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(12).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(13).setCellValue(a.end());
                                    sheet.getRow(i + 1).createCell(14).setCellValue(Math.max(0, a.end() - j.due()));
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < numB1_new; i++) {
                            if (this.b1Indices[i] == a.b1().index()) {
                                sheet.getRow(i + 1).createCell(34).setCellValue(a.start());
                                sheet.getRow(i + 1).createCell(35).setCellValue(a.end());
                                sheet.getRow(i + 1).createCell(36).setCellValue(a.b1().demouldLabourH().name());
                                sheet.getRow(i + 1).createCell(37).setCellValue(a.b1().demouldQty());
                                sheet.getRow(i + 1).createCell(38).setCellValue(a.b1().demouldMachineH().name());
                                break;
                            }
                        }
                    }
                }
                sheet.getRow(0).createCell(0).setCellValue("obj_val");
                sheet.getRow(0).createCell(1).setCellValue(soln.objVal());
                workbook.write(new FileOutputStream(this.filePath));
                workbook.close();

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }

    // Write LBBD solution
    public void writeLBBD(Solution soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filePath));
            XSSFSheet sheet = workbook.getSheet("single_param");
            sheet.getRow(3).getCell(1).setCellValue(soln.b0sS().size());
            sheet.getRow(4).getCell(1).setCellValue(soln.b1sS().size());
            sheet.getRow(5).getCell(1).setCellValue(soln.b2sS().size());
            sheet = workbook.getSheet("results");
            for (int i = 0; i < soln.numJob(); i++) {
                sheet.createRow(i + 1).createCell(3).setCellValue(soln.jobsS().get(i).index());
                sheet.getRow(i + 1).createCell(4).setCellValue(soln.jobsS().get(i).b0S().index());
                sheet.getRow(i + 1).createCell(5).setCellValue(soln.jobsS().get(i).b0S().topTool().name());
                sheet.getRow(i + 1).createCell(6).setCellValue(soln.jobsS().get(i).prepStart());
                sheet.getRow(i + 1).createCell(7).setCellValue(soln.jobsS().get(i).prepEnd());
                sheet.getRow(i + 1).createCell(8).setCellValue(soln.jobsS().get(i).layupStart());
                sheet.getRow(i + 1).createCell(9).setCellValue(soln.jobsS().get(i).layupEnd());
                sheet.getRow(i + 1).createCell(10).setCellValue(soln.jobsS().get(i).autoStart());
                sheet.getRow(i + 1).createCell(11).setCellValue(soln.jobsS().get(i).autoEnd());
                sheet.getRow(i + 1).createCell(12).setCellValue(soln.jobsS().get(i).demouldStart());
                sheet.getRow(i + 1).createCell(13).setCellValue(soln.jobsS().get(i).demouldEnd());
                sheet.getRow(i + 1).createCell(14).setCellValue(soln.jobsS().get(i).tardiness());
            }
            for (int i = 0; i < soln.b0sS().size(); i++) {
                sheet.getRow(i + 1).createCell(16).setCellValue(soln.b0sS().get(i).index());
                sheet.getRow(i + 1).createCell(17).setCellValue(soln.b0sS().get(i).b1S().index());
                sheet.getRow(i + 1).createCell(18).setCellValue(soln.b0sS().get(i).topTool().name());
            }
            for (int i = 0; i < soln.b1sS().size(); i++) {
                sheet.getRow(i + 1).createCell(20).setCellValue(soln.b1sS().get(i).index());
                sheet.getRow(i + 1).createCell(21).setCellValue(soln.b1sS().get(i).b2S().index());
                sheet.getRow(i + 1).createCell(22).setCellValue(soln.b1sS().get(i).bottomTool().name());
                sheet.getRow(i + 1).createCell(23).setCellValue(soln.b1sS().get(i).size());
                sheet.getRow(i + 1).createCell(24).setCellValue(soln.b1sS().get(i).prepStart());
                sheet.getRow(i + 1).createCell(25).setCellValue(soln.b1sS().get(i).prepEnd());
                sheet.getRow(i + 1).createCell(26).setCellValue(soln.b1sS().get(i).prepLabour().name());
                sheet.getRow(i + 1).createCell(27).setCellValue(soln.b1sS().get(i).prepQty());
                sheet.getRow(i + 1).createCell(28).setCellValue(soln.b1sS().get(i).prepMachine().name());
                sheet.getRow(i + 1).createCell(29).setCellValue(soln.b1sS().get(i).layupStart());
                sheet.getRow(i + 1).createCell(30).setCellValue(soln.b1sS().get(i).layupEnd());
                sheet.getRow(i + 1).createCell(31).setCellValue(soln.b1sS().get(i).layupLabour().name());
                sheet.getRow(i + 1).createCell(32).setCellValue(soln.b1sS().get(i).layupQty());
                sheet.getRow(i + 1).createCell(33).setCellValue(soln.b1sS().get(i).layupMachine().name());
                sheet.getRow(i + 1).createCell(34).setCellValue(soln.b1sS().get(i).demouldStart());
                sheet.getRow(i + 1).createCell(35).setCellValue(soln.b1sS().get(i).demouldEnd());
                sheet.getRow(i + 1).createCell(36).setCellValue(soln.b1sS().get(i).demouldLabour().name());
                sheet.getRow(i + 1).createCell(37).setCellValue(soln.b1sS().get(i).demouldQty());
                sheet.getRow(i + 1).createCell(38).setCellValue(soln.b1sS().get(i).demouldMachine().name());
            }
            for (int i = 0; i < soln.b2sS().size(); i++) {
                sheet.getRow(i + 1).createCell(40).setCellValue(soln.b2sS().get(i).index());
                sheet.getRow(i + 1).createCell(41).setCellValue(soln.b2sS().get(i).capacity());
                sheet.getRow(i + 1).createCell(42).setCellValue(soln.b2sS().get(i).sumToolSize());
                sheet.getRow(i + 1).createCell(43).setCellValue(soln.b2sS().get(i).autoStart());
                sheet.getRow(i + 1).createCell(44).setCellValue(soln.b2sS().get(i).autoEnd());
                sheet.getRow(i + 1).createCell(45).setCellValue(soln.b2sS().get(i).autoMachine().name());
            }
            sheet.getRow(0).createCell(0).setCellValue("obj_val");
            sheet.getRow(0).createCell(1).setCellValue(soln.objVal());
            workbook.write(new FileOutputStream(this.filePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write solution checker summary
    public void writeSolnCheck(String status, int mistakes) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filePath));
            XSSFSheet sheet = workbook.getSheet("single_param");
            sheet.createRow(20).createCell(0).setCellValue("Solution Checker Status");
            sheet.getRow(20).createCell(1).setCellValue(status);
            sheet.createRow(21).createCell(0).setCellValue("Number of Mistakes");
            sheet.getRow(21).createCell(1).setCellValue(mistakes);
            workbook.write(new FileOutputStream(this.filePath));
            workbook.close();
            workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.summaryFilePath));
            sheet = workbook.getSheet("solution_check");
            sheet.createRow(this.problemInstance + 1).createCell(0).setCellValue(this.problemInstance);
            sheet.getRow(this.problemInstance + 1).createCell(1).setCellValue(status);
            sheet.getRow(this.problemInstance + 1).createCell(2).setCellValue(mistakes);
            workbook.write(new FileOutputStream(this.summaryFilePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for RSP solutioon
    public void writeSum(SolutionRSP soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.summaryFilePath));
            XSSFSheet sheet = workbook.getSheet("rsp");
            sheet.createRow(this.problemInstance + 1).createCell(0).setCellValue(this.problemInstance);
            sheet.getRow(this.problemInstance + 1).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.problemInstance + 1).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.problemInstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.problemInstance + 1).createCell(4).setCellValue(soln.elapsedTime());
            workbook.write(new FileOutputStream(this.summaryFilePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for packing solution
    public void writeSum(SolutionPack soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.summaryFilePath));
            XSSFSheet sheet = workbook.getSheet("current_exp_pack");
            sheet.createRow(this.problemInstance + 1).createCell(0).setCellValue(this.problemInstance);
            sheet.getRow(this.problemInstance + 1).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.problemInstance + 1).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.problemInstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.problemInstance + 1).createCell(4).setCellValue(soln.elapsedTime());
            sheet.getRow(this.problemInstance + 1).createCell(5).setCellValue(soln.infeasCounter());
            workbook.write(new FileOutputStream(this.summaryFilePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for scheduling solution
    public void writeSum(SolutionSched soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.summaryFilePath));
            XSSFSheet sheet = workbook.getSheet("current_exp_sched");
            sheet.createRow(this.problemInstance + 1).createCell(0).setCellValue(this.problemInstance);
            sheet.getRow(this.problemInstance + 1).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.problemInstance + 1).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.problemInstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.problemInstance + 1).createCell(4).setCellValue(soln.elapsedTime());
            workbook.write(new FileOutputStream(this.summaryFilePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for LBBD solution
    public void writeSum(Solution soln, int infeasCounter) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.summaryFilePath));
            XSSFSheet sheet = workbook.getSheet("current_exp");
            sheet.createRow(this.problemInstance + 1).createCell(0).setCellValue(this.problemInstance);
            sheet.getRow(this.problemInstance + 1).createCell(1).setCellValue(soln.status());
            sheet.getRow(this.problemInstance + 1).createCell(2).setCellValue(soln.b2sS().size());
            sheet.getRow(this.problemInstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.problemInstance + 1).createCell(4).setCellValue(soln.elapsedTime());
            sheet.getRow(this.problemInstance + 1).createCell(9).setCellValue(infeasCounter);
            workbook.write(new FileOutputStream(this.summaryFilePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write extra summary information for LBBD solution
    public void writeSumExtra(int iteration, int overHour, double totElapsedTime, int nonOptimal) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.summaryFilePath));
            XSSFSheet sheet = workbook.getSheet("current_exp");
            sheet.getRow(this.problemInstance + 1).createCell(5).setCellValue(iteration);
            sheet.getRow(this.problemInstance + 1).createCell(6).setCellValue(overHour);
            sheet.getRow(this.problemInstance + 1).createCell(7).setCellValue(totElapsedTime);
            sheet.getRow(this.problemInstance + 1).createCell(8).setCellValue(nonOptimal);
            workbook.write(new FileOutputStream(this.summaryFilePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write quality over time for CP sched
    public void writeQual(ArrayList<Double> quality, ArrayList<Double> times) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filePath));
            XSSFSheet sheet = workbook.getSheet("quality_over_time");
            for (int i = 0; i < quality.size(); i++) {
                sheet.createRow(i + 1).createCell(0).setCellValue(i + 1);
                sheet.getRow(i + 1).createCell(1).setCellValue(quality.get(i));
                sheet.getRow(i + 1).createCell(2).setCellValue(times.get(i));
            }
            workbook.write(new FileOutputStream(this.filePath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
