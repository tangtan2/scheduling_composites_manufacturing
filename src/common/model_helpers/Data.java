package common.model_helpers;
import java.io.*;
import java.util.*;
import models.relaxed_cp_sched.JobRSP;
import models.relaxed_cp_sched.SolutionRSP;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class Data {

    // Filepaths and problem instance
    private final String filepath;
    private final String sumfile;
    private final int probleminstance;

    // Instance parameters
    public int numjob;
    public int totjobs;
    public int numtopbatch;
    public int numb1;
    public int numb2;
    public int numbottom;
    public int numtop;
    public int numtoptobottom;
    public int numjobtotool;
    public int nummachine;
    public int numbatchtomachine;
    public int numshift;
    public int numlabourteam;
    public int numlabourskill;
    public int numskilltomachine;
    public int numdistinctauto;
    public int horizon;
    public int waittime;
    public String[] jobname;
    public int[] dj;
    public int[] sj;
    public int[] autocapperjob;
    public String[] jobpartfamily;
    public int[] rspjoborder;
    public String[][] jobsteps;
    public int[][] jobsteptimes;
    public String[] bottomname;
    public int[] bottommin;
    public int[] bottomcap;
    public int[] bottomqty;
    public int[] bottomsize;
    public String[] topname;
    public int[] topmin;
    public int[] topcap;
    public int[] topqty;
    public String[][] toptobottom;
    public String[][] jobtotools;
    public String[] machinename;
    public int[] machineqty;
    public int[] machinecap;
    public String[][] batchtomachine;
    public int[] batchtime;
    public String[] shiftname;
    public int[] shiftstart;
    public int[] shiftend;
    public int[][] machineshift;
    public String[] labourteamname;
    public String[] labourskillname;
    public int[][] labourmatrix;
    public String[][] labourtomachine;
    public int[] labourqtyrequired;
    public int[][] labourshift;

    // Packing solution parameters
    public int numb0_new;
    public int numb1_new;
    public int numb2_new;
    public int[] jobtob0;
    public String[] jobtotoptool;
    public String[] b0toptool;
    public int[] b0tob1;
    public int[] b1tob2;
    public String[] b1bottomtool;
    public int[] b1vol;
    public int[] b2cap;
    public int[] b2volused;

    // Constructor
    public Data(String path, String sum, int pi) {

        this.filepath = path;
        this.sumfile = sum;
        this.probleminstance = pi;

    }

    // Get instance parameters
    public void readInstanceParams() {

        try {

            Workbook workbook = new XSSFWorkbook(this.filepath);
            Sheet sheet = workbook.getSheet("single_param");
            this.numjob = (int) sheet.getRow(1).getCell(1).getNumericCellValue();
            this.totjobs = (int) sheet.getRow(2).getCell(1).getNumericCellValue();
            this.numtopbatch = (int) sheet.getRow(3).getCell(1).getNumericCellValue();
            this.numb1 = (int) sheet.getRow(4).getCell(1).getNumericCellValue();
            this.numb2 = (int) sheet.getRow(5).getCell(1).getNumericCellValue();
            this.numbottom = (int) sheet.getRow(6).getCell(1).getNumericCellValue();
            this.numtop = (int) sheet.getRow(7).getCell(1).getNumericCellValue();
            this.numtoptobottom = (int) sheet.getRow(8).getCell(1).getNumericCellValue();
            this.numjobtotool = (int) sheet.getRow(9).getCell(1).getNumericCellValue();
            this.nummachine = (int) sheet.getRow(10).getCell(1).getNumericCellValue();
            this.numbatchtomachine = (int) sheet.getRow(11).getCell(1).getNumericCellValue();
            this.numshift = (int) sheet.getRow(12).getCell(1).getNumericCellValue();
            this.numlabourteam = (int) sheet.getRow(13).getCell(1).getNumericCellValue();
            this.numlabourskill = (int) sheet.getRow(14).getCell(1).getNumericCellValue();
            this.numskilltomachine = (int) sheet.getRow(15).getCell(1).getNumericCellValue();
            this.numdistinctauto = (int) sheet.getRow(16).getCell(1).getNumericCellValue();
            this.horizon = (int) sheet.getRow(17).getCell(1).getNumericCellValue();
            this.waittime = (int) sheet.getRow(18).getCell(1).getNumericCellValue();
            sheet = workbook.getSheet("order_jobs");
            this.jobname = new String[this.numjob];
            this.dj = new int[this.numjob];
            this.sj = new int[this.numjob];
            this.autocapperjob = new int[this.numjob];
            this.jobpartfamily = new String[this.numjob];
            this.rspjoborder = new int[this.numjob];
            for (int i = 0; i < this.numjob; i++) {
                this.jobname[i] = Integer.toString((int) sheet.getRow(i + 1).getCell(1).getNumericCellValue());
                this.dj[i] = (int) sheet.getRow(i + 1).getCell(2).getNumericCellValue() * 7 * 24 * 60;
                this.sj[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                this.autocapperjob[i] = (int) sheet.getRow(i + 1).getCell(4).getNumericCellValue();
                this.jobpartfamily[i] = sheet.getRow(i + 1).getCell(5).getStringCellValue();
                this.rspjoborder[i] = (int) sheet.getRow(i + 1).getCell(6).getNumericCellValue();
            }
            sheet = workbook.getSheet("jobs");
            this.jobsteps = new String[this.numjob][4];
            this.jobsteptimes = new int[this.numjob][4];
            for (int i = 0; i < this.numjob; i++) {
                for (int j = 0; j < this.totjobs; j++) {
                    if (Integer.toString((int) sheet.getRow(j + 1).getCell(0).getNumericCellValue()).equals(this.jobname[i])) {
                        for (int k = 0; k < 4; k++) {
                            this.jobsteps[i][k] = sheet.getRow(j + 1).getCell(3 + 2 * k).getStringCellValue();
                            if (sheet.getRow(j + 1).getCell(3 + 2 * k).getStringCellValue().contains("Autoclave")) {
                                this.jobsteptimes[i][k] = 0;
                            } else {
                                this.jobsteptimes[i][k] = (int) sheet.getRow(j + 1).getCell(4 + 2 * k).getNumericCellValue();
                            }
                        }
                        break;
                    }
                }
            }
            sheet = workbook.getSheet("bottom_tools");
            this.bottomname = new String[this.numbottom];
            this.bottomcap = new int[this.numbottom];
            this.bottomqty = new int[this.numbottom];
            this.bottomsize = new int[this.numbottom];
            this.bottommin = new int[this.numbottom];
            for (int i = 0; i < this.numbottom; i++) {
                this.bottomname[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.bottomcap[i] = (int) sheet.getRow(i + 1).getCell(2).getNumericCellValue();
                this.bottomqty[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                this.bottomsize[i] = (int) sheet.getRow(i + 1).getCell(4).getNumericCellValue();
                if (sheet.getRow(i + 1).getCell(1).getStringCellValue().equals("Yes")) {
                    this.bottommin[i] = this.bottomcap[i];
                } else {
                    this.bottommin[i] = 0;
                }
            }
            sheet = workbook.getSheet("top_tools");
            this.topname = new String[this.numtop];
            this.topcap = new int[this.numtop];
            this.topqty = new int[this.numtop];
            this.topmin = new int[this.numtop];
            for (int i = 0; i < this.numtop; i++) {
                this.topname[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.topcap[i] = (int) sheet.getRow(i + 1).getCell(2).getNumericCellValue();
                this.topqty[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                if (sheet.getRow(i + 1).getCell(1).getStringCellValue().equals("Yes")) {
                    this.topmin[i] = this.topcap[i];
                } else {
                    this.topmin[i] = 0;
                }
            }
            sheet = workbook.getSheet("top_to_bottom");
            this.toptobottom = new String[this.numtoptobottom][2];
            for (int i = 0; i < this.numtoptobottom; i++) {
                this.toptobottom[i][0] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.toptobottom[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
            }
            sheet = workbook.getSheet("job_to_tool");
            this.jobtotools = new String[this.numjobtotool][3];
            for (int i = 0; i < this.numjobtotool; i++) {
                this.jobtotools[i][0] = Integer.toString((int) sheet.getRow(i + 1).getCell(0).getNumericCellValue());
                this.jobtotools[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
                this.jobtotools[i][2] = sheet.getRow(i + 1).getCell(2).getStringCellValue();
            }
            sheet = workbook.getSheet("machines");
            this.machinename = new String[this.nummachine];
            this.machineqty = new int[this.nummachine];
            this.machinecap = new int[this.nummachine];
            for (int i = 0; i < this.nummachine; i++) {
                this.machinename[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.machineqty[i] = (int) sheet.getRow(i + 1).getCell(1).getNumericCellValue();
                this.machinecap[i] = (int) sheet.getRow(i + 1).getCell(5).getNumericCellValue();
            }
            sheet = workbook.getSheet("batch_to_machine");
            this.batchtomachine = new String[this.numbatchtomachine][2];
            this.batchtime = new int[this.numbatchtomachine];
            for (int i = 0; i < this.numbatchtomachine; i++) {
                this.batchtomachine[i][0] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.batchtomachine[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
                this.batchtime[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
            }
            for (int i = 0; i < this.numjob; i++) {
                for (int j = 0; j < this.numbatchtomachine; j++) {
                    if (this.jobsteps[i][2].equals(this.batchtomachine[j][1])) {
                        jobsteptimes[i][2] = this.batchtime[j];
                        break;
                    }
                }
            }
            sheet = workbook.getSheet("machine_shifts");
            this.shiftname = new String[this.numshift];
            this.shiftstart = new int[this.numshift];
            this.shiftend = new int[this.numshift];
            this.machineshift = new int[this.nummachine][this.numshift];
            int shift = 1;
            int day = 0;
            for (int i = 0; i < this.numshift; i++) {
                String name = sheet.getRow(0).getCell(i + 1).getStringCellValue() + "-" + shift;
                this.shiftname[i] = name;
                this.shiftstart[i] = (int) Math.round((sheet.getRow(1).getCell(i + 1).getNumericCellValue() + day) * 24 * 60);
                shift++;
                if (shift > 7) {
                    shift = 1;
                    day++;
                }
                this.shiftend[i] = (int) Math.round((sheet.getRow(2).getCell(i + 1).getNumericCellValue() + day) * 24 * 60);
            }
            for (int i = 0; i < this.numshift; i++) {
                for (int j = 0; j < this.nummachine; j++) {
                    if (sheet.getRow(3 + j).getCell(1 + i).getStringCellValue().equals("ON")) {
                        this.machineshift[j][i] = this.machineqty[j];
                    } else {
                        this.machineshift[j][i] = 0;
                    }
                }
            }
            sheet = workbook.getSheet("labour_skills");
            this.labourteamname = new String[this.numlabourteam];
            this.labourskillname = new String[this.numlabourskill];
            this.labourmatrix = new int[this.numlabourteam][this.numlabourskill];
            for (int i = 0; i < this.numlabourteam; i++) {
                this.labourteamname[i] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
            }
            for (int i = 0; i < this.numlabourskill; i++) {
                this.labourskillname[i] = sheet.getRow(0).getCell(i + 1).getStringCellValue();
            }
            for (int i = 0; i < this.numlabourteam; i++) {
                for (int j = 0; j < this.numlabourskill; j++) {
                    this.labourmatrix[i][j] = (int) sheet.getRow(i + 1).getCell(j + 1).getNumericCellValue();
                }
            }
            sheet = workbook.getSheet("labour_to_machine");
            this.labourtomachine = new String[this.numskilltomachine][3];
            this.labourqtyrequired = new int[this.numskilltomachine];
            for (int i = 0; i < this.numskilltomachine; i++) {
                this.labourtomachine[i][0] = sheet.getRow(i + 1).getCell(0).getStringCellValue();
                this.labourtomachine[i][1] = sheet.getRow(i + 1).getCell(1).getStringCellValue();
                this.labourtomachine[i][2] = sheet.getRow(i + 1).getCell(2).getStringCellValue();
                this.labourqtyrequired[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
            }
            sheet = workbook.getSheet("labour_shifts");
            this.labourshift = new int[this.numlabourteam][this.numshift];
            for (int i = 0; i < this.numshift; i++) {
                for (int j = 0; j < this.numlabourteam; j++) {
                    this.labourshift[j][i] = (int) sheet.getRow(3 + j).getCell(1 + i).getNumericCellValue();
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

            Workbook workbook = new XSSFWorkbook(this.filepath);
            Sheet sheet = workbook.getSheet("single_param");
            this.numb0_new = (int) sheet.getRow(3).getCell(1).getNumericCellValue();
            this.numb1_new = (int) sheet.getRow(4).getCell(1).getNumericCellValue();
            this.numb2_new = (int) sheet.getRow(5).getCell(1).getNumericCellValue();
            sheet = workbook.getSheet("results");
            this.jobtob0 = new int[this.numjob];
            this.jobtotoptool = new String[this.numjob];
            this.b0toptool = new String[this.numb0_new];
            this.b0tob1 = new int[this.numb0_new];
            this.b1tob2 = new int[this.numb1_new];
            this.b1bottomtool = new String[this.numb1_new];
            this.b1vol = new int[this.numb1_new];
            this.b2cap = new int[this.numb2_new];
            this.b2volused = new int[this.numb2_new];
            for (int i = 0; i < this.numjob; i++) {
                this.jobtob0[i] = (int) sheet.getRow(i + 1).getCell(4).getNumericCellValue();
                this.jobtotoptool[i] = sheet.getRow(i + 1).getCell(5).getStringCellValue();
            }
            for (int i = 0; i < this.numb0_new; i++) {
                this.b0tob1[i] = (int) sheet.getRow(i + 1).getCell(17).getNumericCellValue();
                this.b0toptool[i] = sheet.getRow(i + 1).getCell(18).getStringCellValue();
            }
            for (int i = 0; i < this.numb1_new; i++) {
                this.b1tob2[i] = (int) sheet.getRow(i + 1).getCell(21).getNumericCellValue();
                this.b1bottomtool[i] = sheet.getRow(i + 1).getCell(22).getStringCellValue();
                this.b1vol[i] = (int) sheet.getRow(i + 1).getCell(23).getNumericCellValue();
            }
            for (int i = 0; i < this.numb2_new; i++) {
                this.b2cap[i] = (int) sheet.getRow(i + 1).getCell(41).getNumericCellValue();
                this.b2volused[i] = (int) sheet.getRow(i + 1).getCell(42).getNumericCellValue();
            }
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write RSP solution
    public void writeRSP(SolutionRSP soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
            XSSFSheet sheet = workbook.getSheet("order_jobs");
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
            workbook.write(new FileOutputStream(this.filepath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write packing solution
    public void writePack(SolutionPack soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
            XSSFSheet sheet = workbook.getSheet("single_param");
            sheet.getRow(3).getCell(1).setCellValue(soln.b0s().size());
            sheet.getRow(4).getCell(1).setCellValue(soln.b1s().size());
            sheet.getRow(5).getCell(1).setCellValue(soln.b2s().size());
            sheet = workbook.getSheet("results");
            for (int i = 0; i < soln.numJob(); i++) {
                sheet.createRow(i + 1).createCell(3).setCellValue(i);
                sheet.getRow(i + 1).createCell(4).setCellValue(soln.b0s().indexOf(soln.jobs().get(i).b0()));
                sheet.getRow(i + 1).createCell(5).setCellValue(soln.jobs().get(i).b0().topTool().name());
            }
            for (int i = 0; i < soln.b0s().size(); i++) {
                sheet.getRow(i + 1).createCell(16).setCellValue(i);
                sheet.getRow(i + 1).createCell(17).setCellValue(soln.b1s().indexOf(soln.b0s().get(i).b1()));
                sheet.getRow(i + 1).createCell(18).setCellValue(soln.b0s().get(i).topTool().name());
            }
            for (int i = 0; i < soln.b1s().size(); i++) {
                sheet.getRow(i + 1).createCell(20).setCellValue(i);
                sheet.getRow(i + 1).createCell(21).setCellValue(soln.b2s().indexOf(soln.b1s().get(i).b2()));
                sheet.getRow(i + 1).createCell(22).setCellValue(soln.b1s().get(i).bottomTool().name());
                sheet.getRow(i + 1).createCell(23).setCellValue(soln.b1s().get(i).size());
            }
            for (int i = 0; i < soln.b2s().size(); i++) {
                sheet.getRow(i + 1).createCell(40).setCellValue(i);
                sheet.getRow(i + 1).createCell(41).setCellValue(soln.b2s().get(i).capacity());
                sheet.getRow(i + 1).createCell(42).setCellValue(soln.b2s().get(i).sumToolSize());
            }
            workbook.write(new FileOutputStream(this.filepath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write scheduling solution
    public void writeSched(SolutionSched soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
            XSSFSheet sheet = workbook.getSheet("results");
            for (int i = 0; i < soln.numJob(); i++) {
                sheet.getRow(i + 1).createCell(6).setCellValue(soln.jobs().get(i).prepStart());
                sheet.getRow(i + 1).createCell(7).setCellValue(soln.jobs().get(i).prepEnd());
                sheet.getRow(i + 1).createCell(8).setCellValue(soln.jobs().get(i).layupStart());
                sheet.getRow(i + 1).createCell(9).setCellValue(soln.jobs().get(i).layupEnd());
                sheet.getRow(i + 1).createCell(10).setCellValue(soln.jobs().get(i).autoStart());
                sheet.getRow(i + 1).createCell(11).setCellValue(soln.jobs().get(i).autoEnd());
                sheet.getRow(i + 1).createCell(12).setCellValue(soln.jobs().get(i).demouldStart());
                sheet.getRow(i + 1).createCell(13).setCellValue(soln.jobs().get(i).demouldEnd());
                sheet.getRow(i + 1).createCell(14).setCellValue(soln.jobs().get(i).tardiness());
            }
            for (int i = 0; i < soln.b1s().size(); i++) {
                sheet.getRow(i + 1).createCell(24).setCellValue(soln.b1s().get(i).prepStart());
                sheet.getRow(i + 1).createCell(25).setCellValue(soln.b1s().get(i).prepEnd());
                sheet.getRow(i + 1).createCell(26).setCellValue(soln.b1s().get(i).prepLabour().name());
                sheet.getRow(i + 1).createCell(27).setCellValue(soln.b1s().get(i).prepQty());
                sheet.getRow(i + 1).createCell(28).setCellValue(soln.b1s().get(i).prepMachine().name());
                sheet.getRow(i + 1).createCell(29).setCellValue(soln.b1s().get(i).layupStart());
                sheet.getRow(i + 1).createCell(30).setCellValue(soln.b1s().get(i).layupEnd());
                sheet.getRow(i + 1).createCell(31).setCellValue(soln.b1s().get(i).layupLabour().name());
                sheet.getRow(i + 1).createCell(32).setCellValue(soln.b1s().get(i).layupQty());
                sheet.getRow(i + 1).createCell(33).setCellValue(soln.b1s().get(i).layupMachine().name());
                sheet.getRow(i + 1).createCell(34).setCellValue(soln.b1s().get(i).demouldStart());
                sheet.getRow(i + 1).createCell(35).setCellValue(soln.b1s().get(i).demouldEnd());
                sheet.getRow(i + 1).createCell(36).setCellValue(soln.b1s().get(i).demouldLabour().name());
                sheet.getRow(i + 1).createCell(37).setCellValue(soln.b1s().get(i).demouldQty());
                sheet.getRow(i + 1).createCell(38).setCellValue(soln.b1s().get(i).demouldMachine().name());
            }
            for (int i = 0; i < soln.b2s().size(); i++) {
                sheet.getRow(i + 1).createCell(43).setCellValue(soln.b2s().get(i).autoStart());
                sheet.getRow(i + 1).createCell(44).setCellValue(soln.b2s().get(i).autoEnd());
                sheet.getRow(i + 1).createCell(45).setCellValue(soln.b2s().get(i).autoMachine().name());
            }
            sheet.getRow(0).getCell(0).setCellValue("obj_val");
            sheet.getRow(0).getCell(1).setCellValue(soln.objVal());
            workbook.write(new FileOutputStream(this.filepath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write LBBD solution
    public void writeLBBD(Solution soln, ArrayList<Integer> objs, ArrayList<Double> times) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
            XSSFSheet sheet = workbook.getSheet("single_param");
            sheet.getRow(3).getCell(1).setCellValue(soln.b0s().size());
            sheet.getRow(4).getCell(1).setCellValue(soln.b1s().size());
            sheet.getRow(5).getCell(1).setCellValue(soln.b2s().size());
            sheet = workbook.getSheet("results");
            for (int i = 0; i < soln.numJob(); i++) {
                sheet.createRow(i + 1).createCell(3).setCellValue(i);
                sheet.getRow(i + 1).createCell(4).setCellValue(soln.b0s().indexOf(soln.jobs().get(i).b0()));
                sheet.getRow(i + 1).createCell(5).setCellValue(soln.jobs().get(i).b0().topTool().name());
                sheet.getRow(i + 1).createCell(6).setCellValue(soln.jobs().get(i).prepStart());
                sheet.getRow(i + 1).createCell(7).setCellValue(soln.jobs().get(i).prepEnd());
                sheet.getRow(i + 1).createCell(8).setCellValue(soln.jobs().get(i).layupStart());
                sheet.getRow(i + 1).createCell(9).setCellValue(soln.jobs().get(i).layupEnd());
                sheet.getRow(i + 1).createCell(10).setCellValue(soln.jobs().get(i).autoStart());
                sheet.getRow(i + 1).createCell(11).setCellValue(soln.jobs().get(i).autoEnd());
                sheet.getRow(i + 1).createCell(12).setCellValue(soln.jobs().get(i).demouldStart());
                sheet.getRow(i + 1).createCell(13).setCellValue(soln.jobs().get(i).demouldEnd());
                sheet.getRow(i + 1).createCell(14).setCellValue(soln.jobs().get(i).tardiness());
            }
            for (int i = 0; i < soln.b0s().size(); i++) {
                sheet.getRow(i + 1).createCell(16).setCellValue(i);
                sheet.getRow(i + 1).createCell(17).setCellValue(soln.b1s().indexOf(soln.b0s().get(i).b1()));
                sheet.getRow(i + 1).createCell(18).setCellValue(soln.b0s().get(i).topTool().name());
            }
            for (int i = 0; i < soln.b1s().size(); i++) {
                sheet.getRow(i + 1).createCell(20).setCellValue(i);
                sheet.getRow(i + 1).createCell(21).setCellValue(soln.b2s().indexOf(soln.b1s().get(i).b2()));
                sheet.getRow(i + 1).createCell(22).setCellValue(soln.b1s().get(i).bottomTool().name());
                sheet.getRow(i + 1).createCell(23).setCellValue(soln.b1s().get(i).size());
                sheet.getRow(i + 1).createCell(24).setCellValue(soln.b1s().get(i).prepStart());
                sheet.getRow(i + 1).createCell(25).setCellValue(soln.b1s().get(i).prepEnd());
                sheet.getRow(i + 1).createCell(26).setCellValue(soln.b1s().get(i).prepLabour().name());
                sheet.getRow(i + 1).createCell(27).setCellValue(soln.b1s().get(i).prepQty());
                sheet.getRow(i + 1).createCell(28).setCellValue(soln.b1s().get(i).prepMachine().name());
                sheet.getRow(i + 1).createCell(29).setCellValue(soln.b1s().get(i).layupStart());
                sheet.getRow(i + 1).createCell(30).setCellValue(soln.b1s().get(i).layupEnd());
                sheet.getRow(i + 1).createCell(31).setCellValue(soln.b1s().get(i).layupLabour().name());
                sheet.getRow(i + 1).createCell(32).setCellValue(soln.b1s().get(i).layupQty());
                sheet.getRow(i + 1).createCell(33).setCellValue(soln.b1s().get(i).layupMachine().name());
                sheet.getRow(i + 1).createCell(34).setCellValue(soln.b1s().get(i).demouldStart());
                sheet.getRow(i + 1).createCell(35).setCellValue(soln.b1s().get(i).demouldEnd());
                sheet.getRow(i + 1).createCell(36).setCellValue(soln.b1s().get(i).demouldLabour().name());
                sheet.getRow(i + 1).createCell(37).setCellValue(soln.b1s().get(i).demouldQty());
                sheet.getRow(i + 1).createCell(38).setCellValue(soln.b1s().get(i).demouldMachine().name());
            }
            for (int i = 0; i < soln.b2s().size(); i++) {
                sheet.getRow(i + 1).createCell(40).setCellValue(i);
                sheet.getRow(i + 1).createCell(41).setCellValue(soln.b2s().get(i).capacity());
                sheet.getRow(i + 1).createCell(42).setCellValue(soln.b2s().get(i).sumToolSize());
                sheet.getRow(i + 1).createCell(43).setCellValue(soln.b2s().get(i).autoStart());
                sheet.getRow(i + 1).createCell(44).setCellValue(soln.b2s().get(i).autoEnd());
                sheet.getRow(i + 1).createCell(45).setCellValue(soln.b2s().get(i).autoMachine().name());
            }
            sheet = workbook.getSheet("quality_over_time");
            for (int i = 0; i < objs.size(); i++) {
                sheet.createRow(i + 1).createCell(0).setCellValue(i + 1);
                sheet.getRow(i + 1).createCell(1).setCellValue(objs.get(i));
                sheet.getRow(i + 1).createCell(2).setCellValue(times.get(i));
            }
            workbook.write(new FileOutputStream(this.filepath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for RSP solutioon
    public void writeSum(SolutionRSP soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.sumfile));
            XSSFSheet sheet = workbook.getSheet("rsp");
            sheet.createRow(this.probleminstance).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(this.probleminstance).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.probleminstance).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.probleminstance).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.probleminstance).createCell(4).setCellValue(soln.elapsedTime());
            workbook.write(new FileOutputStream(this.sumfile));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for packing solution
    public void writeSum(SolutionPack soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.sumfile));
            XSSFSheet sheet = workbook.getSheet("current_exp_pack");
            sheet.createRow(this.probleminstance).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(this.probleminstance).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.probleminstance).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.probleminstance).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.probleminstance).createCell(4).setCellValue(soln.elapsedTime());
            workbook.write(new FileOutputStream(this.sumfile));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for scheduling solution
    public void writeSum(SolutionSched soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.sumfile));
            XSSFSheet sheet = workbook.getSheet("current_exp_sched");
            sheet.createRow(this.probleminstance).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(this.probleminstance).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.probleminstance).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.probleminstance).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.probleminstance).createCell(4).setCellValue(soln.elapsedTime());
            workbook.write(new FileOutputStream(this.sumfile));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write summary for LBBD solution
    public void writeSum(Solution soln) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.sumfile));
            XSSFSheet sheet = workbook.getSheet("current_exp");
            sheet.createRow(this.probleminstance).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(probleminstance).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(probleminstance).createCell(2).setCellValue(soln.status());
            sheet.getRow(probleminstance).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(probleminstance).createCell(4).setCellValue(soln.elapsedTime());
            sheet.getRow(probleminstance).createCell(5).setCellValue(soln.iteration());
            sheet.getRow(probleminstance).createCell(6).setCellValue(soln.overHour());
            sheet.getRow(probleminstance).createCell(7).setCellValue(soln.totElapsedTime());
            sheet.getRow(probleminstance).createCell(8).setCellValue(soln.nonOptimal());
            workbook.write(new FileOutputStream(this.sumfile));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
