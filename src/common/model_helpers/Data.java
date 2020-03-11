package common.model_helpers;
import java.io.*;
import java.util.*;

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
    public int[] jobindices;
    public int[] jobtob0;
    public String[] jobtotoptool;
    public int[] b0indices;
    public String[] b0toptool;
    public int[] b0tob1;
    public int[] b1indices;
    public int[] b1tob2;
    public String[] b1bottomtool;
    public int[] b1vol;
    public int[] b2indices;
    public int[] b2cap;
    public int[] b2volused;

    // Scheduling solution parameters
    public int[] jobpreps;
    public int[] jobprepe;
    public int[] joblayups;
    public int[] joblayupe;
    public int[] jobautos;
    public int[] jobautoe;
    public int[] jobdemoulds;
    public int[] jobdemoulde;
    public int[] jobtardy;
    public int[] b1preps;
    public int[] b1prepe;
    public String[] b1preplabour;
    public int[] b1prepqty;
    public String[] b1prepmachine;
    public int[] b1layups;
    public int[] b1layupe;
    public String[] b1layuplabour;
    public int[] b1layupqty;
    public String[] b1layupmachine;
    public int[] b1demoulds;
    public int[] b1demoulde;
    public String[] b1demouldlabour;
    public int[] b1demouldqty;
    public String[] b1demouldmachine;
    public int[] b2autos;
    public int[] b2autoe;
    public String[] b2automachine;

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
                try {
                    this.rspjoborder[i] = (int) sheet.getRow(i + 1).getCell(6).getNumericCellValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            this.jobindices = new int[this.numjob];
            this.jobtob0 = new int[this.numjob];
            this.jobtotoptool = new String[this.numjob];
            this.b0indices = new int[this.numb0_new];
            this.b0toptool = new String[this.numb0_new];
            this.b0tob1 = new int[this.numb0_new];
            this.b1indices = new int[this.numb1_new];
            this.b1tob2 = new int[this.numb1_new];
            this.b1bottomtool = new String[this.numb1_new];
            this.b1vol = new int[this.numb1_new];
            this.b2indices = new int[this.numb2_new];
            this.b2cap = new int[this.numb2_new];
            this.b2volused = new int[this.numb2_new];
            for (int i = 0; i < this.numjob; i++) {
                this.jobindices[i] = (int) sheet.getRow(i + 1).getCell(3).getNumericCellValue();
                this.jobtob0[i] = (int) sheet.getRow(i + 1).getCell(4).getNumericCellValue();
                this.jobtotoptool[i] = sheet.getRow(i + 1).getCell(5).getStringCellValue();
            }
            for (int i = 0; i < this.numb0_new; i++) {
                this.b0indices[i] = (int) sheet.getRow(i + 1).getCell(16).getNumericCellValue();
                this.b0tob1[i] = (int) sheet.getRow(i + 1).getCell(17).getNumericCellValue();
                this.b0toptool[i] = sheet.getRow(i + 1).getCell(18).getStringCellValue();
            }
            for (int i = 0; i < this.numb1_new; i++) {
                this.b1indices[i] = (int) sheet.getRow(i + 1).getCell(20).getNumericCellValue();
                this.b1tob2[i] = (int) sheet.getRow(i + 1).getCell(21).getNumericCellValue();
                this.b1bottomtool[i] = sheet.getRow(i + 1).getCell(22).getStringCellValue();
                this.b1vol[i] = (int) sheet.getRow(i + 1).getCell(23).getNumericCellValue();
            }
            for (int i = 0; i < this.numb2_new; i++) {
                this.b2indices[i] = (int) sheet.getRow(i + 1).getCell(40).getNumericCellValue();
                this.b2cap[i] = (int) sheet.getRow(i + 1).getCell(41).getNumericCellValue();
                this.b2volused[i] = (int) sheet.getRow(i + 1).getCell(42).getNumericCellValue();
            }
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Get packing solution parameters
    public void readSchedParams() {

        try {

            Workbook workbook = new XSSFWorkbook(this.filepath);
            Sheet sheet = workbook.getSheet("results");
            this.jobpreps = new int[this.numjob];
            this.jobprepe = new int[this.numjob];
            this.joblayups = new int[this.numjob];
            this.joblayupe = new int[this.numjob];
            this.jobautos = new int[this.numjob];
            this.jobautoe = new int[this.numjob];
            this.jobdemoulds = new int[this.numjob];
            this.jobdemoulde = new int[this.numjob];
            this.jobtardy = new int[this.numjob];
            this.b1preps = new int[this.numb1_new];
            this.b1prepe = new int[this.numb1_new];
            this.b1preplabour = new String[this.numb1_new];
            this.b1prepqty = new int[this.numb1_new];
            this.b1prepmachine = new String[this.numb1_new];
            this.b1layups = new int[this.numb1_new];
            this.b1layupe = new int[this.numb1_new];
            this.b1layuplabour = new String[this.numb1_new];
            this.b1layupqty = new int[this.numb1_new];
            this.b1layupmachine = new String[this.numb1_new];
            this.b1demoulds = new int[this.numb1_new];
            this.b1demoulde = new int[this.numb1_new];
            this.b1demouldlabour = new String[this.numb1_new];
            this.b1demouldqty = new int[this.numb1_new];
            this.b1demouldmachine = new String[this.numb1_new];
            this.b2autos = new int[this.numb2_new];
            this.b2autoe = new int[this.numb2_new];
            this.b2automachine = new String[this.numb2_new];
            for (int i = 0; i < this.numjob; i++) {
                this.jobpreps[i] = (int) sheet.getRow(i + 1).getCell(6).getNumericCellValue();
                this.jobprepe[i] = (int) sheet.getRow(i + 1).getCell(7).getNumericCellValue();
                this.joblayups[i] = (int) sheet.getRow(i + 1).getCell(8).getNumericCellValue();
                this.joblayupe[i] = (int) sheet.getRow(i + 1).getCell(9).getNumericCellValue();
                this.jobautos[i] = (int) sheet.getRow(i + 1).getCell(10).getNumericCellValue();
                this.jobautoe[i] = (int) sheet.getRow(i + 1).getCell(11).getNumericCellValue();
                this.jobdemoulds[i] = (int) sheet.getRow(i + 1).getCell(12).getNumericCellValue();
                this.jobdemoulde[i] = (int) sheet.getRow(i + 1).getCell(13).getNumericCellValue();
                this.jobtardy[i] = (int) sheet.getRow(i + 1).getCell(14).getNumericCellValue();
            }
            for (int i = 0; i < this.numb1_new; i++) {
                this.b1preps[i] = (int) sheet.getRow(i + 1).getCell(24).getNumericCellValue();
                this.b1prepe[i] = (int) sheet.getRow(i + 1).getCell(25).getNumericCellValue();
                this.b1preplabour[i] = sheet.getRow(i + 1).getCell(26).getStringCellValue();
                this.b1prepqty[i] = (int) sheet.getRow(i + 1).getCell(27).getNumericCellValue();
                this.b1prepmachine[i] = sheet.getRow(i + 1).getCell(28).getStringCellValue();
                this.b1layups[i] = (int) sheet.getRow(i + 1).getCell(29).getNumericCellValue();
                this.b1layupe[i] = (int) sheet.getRow(i + 1).getCell(30).getNumericCellValue();
                this.b1layuplabour[i] = sheet.getRow(i + 1).getCell(31).getStringCellValue();
                this.b1layupqty[i] = (int) sheet.getRow(i + 1).getCell(32).getNumericCellValue();
                this.b1layupmachine[i] = sheet.getRow(i + 1).getCell(33).getStringCellValue();
                this.b1demoulds[i] = (int) sheet.getRow(i + 1).getCell(34).getNumericCellValue();
                this.b1demoulde[i] = (int) sheet.getRow(i + 1).getCell(35).getNumericCellValue();
                this.b1demouldlabour[i] = sheet.getRow(i + 1).getCell(36).getStringCellValue();
                this.b1demouldqty[i] = (int) sheet.getRow(i + 1).getCell(37).getNumericCellValue();
                this.b1demouldmachine[i] =sheet.getRow(i + 1).getCell(38).getStringCellValue();
            }
            for (int i = 0; i < this.numb2_new; i++) {
                this.b2autos[i] = (int) sheet.getRow(i + 1).getCell(43).getNumericCellValue();
                this.b2autoe[i] = (int) sheet.getRow(i + 1).getCell(44).getNumericCellValue();
                this.b2automachine[i] = sheet.getRow(i + 1).getCell(45).getStringCellValue();
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
            }
            workbook.write(new FileOutputStream(this.filepath));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // Write scheduling solution
    public void writeSched(SolutionSched soln) {

        if (soln.b2sS().size() > 0) {

            try {

                XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
                XSSFSheet sheet = workbook.getSheet("results");
                for (AutoBatchS a : soln.b2sS()) {
                    for (JobS j : a.jobsS()) {
                        for (int i = 0; i < this.numjob; i++) {
                            if (this.jobindices[i] == j.index()) {
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
                        for (int i = 0; i < this.numb1_new; i++) {
                            if (this.b1indices[i] == t.index()) {
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
                    for (int i = 0; i < this.numb2_new; i++) {
                        if (this.b2indices[i] == a.index()) {
                            sheet.getRow(i + 1).createCell(43).setCellValue(a.autoStart());
                            sheet.getRow(i + 1).createCell(44).setCellValue(a.autoEnd());
                            sheet.getRow(i + 1).createCell(45).setCellValue(a.autoMachine().name());
                            break;
                        }
                    }
                }
                sheet.getRow(0).createCell(0).setCellValue("obj_val");
                sheet.getRow(0).createCell(1).setCellValue(soln.objVal());
                workbook.write(new FileOutputStream(this.filepath));
                workbook.close();

            } catch (Exception e) {

                e.printStackTrace();

            }

        } else if (soln.acts().size() > 0) {

            try {

                XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
                XSSFSheet sheet = workbook.getSheet("results");
                for (Activity a : soln.acts()) {
                    if (a.type() == 0) {
                        for (Job j : a.b1().jobs()) {
                            for (int i = 0; i < this.numjob; i++) {
                                if (this.jobindices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(6).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(7).setCellValue(a.end());
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < this.numb1_new; i++) {
                            if (this.b1indices[i] == a.b1().index()) {
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
                            for (int i = 0; i < this.numjob; i++) {
                                if (this.jobindices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(8).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(9).setCellValue(a.end());
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < this.numb1_new; i++) {
                            if (this.b1indices[i] == a.b1().index()) {
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
                            for (int i = 0; i < this.numjob; i++) {
                                if (this.jobindices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(10).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(11).setCellValue(a.end());
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < numb2_new; i++) {
                            if (this.b2indices[i] == a.b2().index()) {
                                sheet.getRow(i + 1).createCell(43).setCellValue(a.start());
                                sheet.getRow(i + 1).createCell(44).setCellValue(a.end());
                                sheet.getRow(i + 1).createCell(45).setCellValue(a.b2().autoMachineH().name());
                                break;
                            }
                        }
                    } else if (a.type() == 3) {
                        for (Job j : a.b1().jobs()) {
                            for (int i = 0; i < this.numjob; i++) {
                                if (this.jobindices[i] == j.index()) {
                                    sheet.getRow(i + 1).createCell(12).setCellValue(a.start());
                                    sheet.getRow(i + 1).createCell(13).setCellValue(a.end());
                                    sheet.getRow(i + 1).createCell(14).setCellValue(Math.max(0, a.end() - j.due()));
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < numb1_new; i++) {
                            if (this.b1indices[i] == a.b1().index()) {
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
                workbook.write(new FileOutputStream(this.filepath));
                workbook.close();

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }

    // Write LBBD solution
    public void writeLBBD(Solution soln, ArrayList<Integer> objs, ArrayList<Double> times) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
            XSSFSheet sheet = workbook.getSheet("single_param");
            sheet.getRow(3).getCell(1).setCellValue(soln.jobsS().size());
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

    // Write solution checker summary
    public void writeSolnCheck(String status, int mistakes) {

        try {

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(this.filepath));
            XSSFSheet sheet = workbook.getSheet("single_param");
            sheet.createRow(20).createCell(0).setCellValue("Solution Checker Status");
            sheet.getRow(20).createCell(1).setCellValue(status);
            sheet.createRow(21).createCell(0).setCellValue("Number of Mistakes");
            sheet.getRow(21).createCell(1).setCellValue(mistakes);
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
            sheet.createRow(this.probleminstance + 1).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(this.probleminstance + 1).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.probleminstance + 1).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.probleminstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.probleminstance + 1).createCell(4).setCellValue(soln.elapsedTime());
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
            sheet.createRow(this.probleminstance + 1).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(this.probleminstance + 1).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.probleminstance + 1).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.probleminstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.probleminstance + 1).createCell(4).setCellValue(soln.elapsedTime());
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
            sheet.createRow(this.probleminstance + 1).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(this.probleminstance + 1).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.probleminstance + 1).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.probleminstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.probleminstance + 1).createCell(4).setCellValue(soln.elapsedTime());
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
            sheet.createRow(this.probleminstance + 1).createCell(0).setCellValue(this.probleminstance);
            sheet.getRow(this.probleminstance + 1).createCell(1).setCellValue(soln.numJob());
            sheet.getRow(this.probleminstance + 1).createCell(2).setCellValue(soln.status());
            sheet.getRow(this.probleminstance + 1).createCell(3).setCellValue(soln.objVal());
            sheet.getRow(this.probleminstance + 1).createCell(4).setCellValue(soln.elapsedTime());
            sheet.getRow(this.probleminstance + 1).createCell(5).setCellValue(soln.iteration());
            sheet.getRow(this.probleminstance + 1).createCell(6).setCellValue(soln.overHour());
            sheet.getRow(this.probleminstance + 1).createCell(7).setCellValue(soln.totElapsedTime());
            sheet.getRow(this.probleminstance + 1).createCell(8).setCellValue(soln.nonOptimal());
            workbook.write(new FileOutputStream(this.sumfile));
            workbook.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
