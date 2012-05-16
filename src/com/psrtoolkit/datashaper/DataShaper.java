/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
package com.psrtoolkit.datashaper;

import java.text.DateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import com.psrtoolkit.datashaper.agent.DataShapeAgent;
import com.psrtoolkit.datashaper.agent.DataShapeAgentFactory;
import com.psrtoolkit.datashaper.config.ConfigReader;
//import com.psrtoolkit.datashaper.license.LicenseMgr;
import com.psrtoolkit.datashaper.table.TableMgr;
import com.psrtoolkit.datashaper.util.StringUtil;

public class DataShaper {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String argument = null;
        String value = null;
        String configFile = null;
        String userName = null;
        String password = null;
        String userSignature = null;
        StringBuilder buf = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        String inputParamsNValues = null;
        DataShapeAgent agent = null;

        try {
            for (String inpval : args) {
                buf.append(inpval.trim()).append(" ");
            }
            inputParamsNValues = buf.toString();
//			System.out.println("\n" + "Command Line DataShaper, patent pending");
            System.out.println(StringUtil.COPYRIGHT_NOTICE + "\n");

            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            //long startMemory = Runtime.getRuntime().totalMemory() -
            //Runtime.getRuntime().freeMemory();

            StringTokenizer tokenizer = new StringTokenizer(inputParamsNValues, "-");
            int size = tokenizer.countTokens();
            StringBuilder maskedInputStr = new StringBuilder();

            tokenizer = new StringTokenizer(inputParamsNValues, "-");
            size = tokenizer.countTokens();
            boolean bInvalidArgFound = false;

            for (int i = 0; i < size; i++) {
                try {
                    argument = tokenizer.nextToken(" ");
                    maskedInputStr.append(" ").append(argument);
                    value = tokenizer.nextToken();
                } catch (NoSuchElementException eno) {
                    bInvalidArgFound = true;
                    break;
                }
                if (argument.equals("-c") == true) {
                    configFile = new String(value);
                    maskedInputStr.append(" ").append(value);
                } else if (argument.equals("-u") == true) {
                    userName = new String(value);
                    maskedInputStr.append(" ").append(value);
                } else if (argument.equals("-p") == true) {
                    password = new String(value);
                    maskedInputStr.append(" ");
                    for (int j = 0; value != null && j < value.length(); j++) {
                        maskedInputStr.append("*");
                    }
                } else if (argument.equals("-s") == true) {
                    userSignature = new String(value);
                    maskedInputStr.append(" ").append(value);
                } else {
                    bInvalidArgFound = true;
                    maskedInputStr.append(" ").append(value);
                }
            }

            System.out.println(DataShaper.class.getCanonicalName() + maskedInputStr.toString() + "\n");

            if (size < 3 || size > 4) {
                System.out.printf(StringUtil.getCommandLineUsageMessage("\nError: insufficient or excessive input parameters ==> ", inputParamsNValues));
                System.exit(1);
            }

            if (bInvalidArgFound) {
                System.out.println(StringUtil.getCommandLineUsageMessage("\nError: invalid input argument ==> ", argument));
                System.exit(1);
            }

            // note: default.license is packaged inside datashaper.jar by the build process
            Properties defaultLicense = new Properties();
//			if (LicenseMgr.verifyLicense(defaultLicense, StringUtil.FILE_DEFAULT_LICENSE, StringUtil.VENDOR_NAME) == false)
//				System.exit(1);

            Properties license = new Properties();
//			if (LicenseMgr.verifyLicense(license, StringUtil.FILE_DATASHAPER_LICENSE, StringUtil.VENDOR_NAME) == false)
//				System.exit(1);

//			StringUtil.splashScreen(license, StringUtil.FILE_DATASHAPER_LICENSE, StringUtil.VENDOR_NAME);

            TableMgr tableMgr = new TableMgr(false, defaultLicense);
            tableMgr.setStartTime(startTime);
            //tableMgr.setStartMemory(startMemory);
            tableMgr.setUserSignature(userSignature);
            ConfigReader.read(configFile, tableMgr);
            agent = DataShapeAgentFactory.createAgent(ConfigReader.getDbType(),
                    ConfigReader.getSharedSequence());

            String connectionInfo = ConfigReader.getConnectionStr();
            //verify license's db vendor type against the one actually used
            if (license.isEmpty() == false) {
                String dbType = license.getProperty("db");
                if (dbType.equals("universal") == false && connectionInfo.contains(dbType.toLowerCase()) == false) {
                    System.out.println("Error: Datashaper run aborted. The db type specified in your license is"
                            + " different from what is specified in the connection string in the configuration.");
                    System.exit(1);
                }
                String version = license.getProperty("version");
                if (version != null) {
                    String firstAlphabet = version.substring(0, 1);
                    if (StringUtil.VERSION_MAJOR.equals(firstAlphabet != null ? firstAlphabet : "") == false) {
                        System.out.println("Error: Datashaper run aborted. The major version specified in your license is"
                                + " incompatible with the current version of this program.");
                        System.exit(1);
                    }
                }
            }

            agent.connect(connectionInfo, userName, password);

            agent.setTableMgr(tableMgr);
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - startTime) + " milliseconds");
            System.out.println("Connected to DB, begin populating data in tables...");
            agent.generate();
            agent.disconnect();
            agent = null;


            endTime = System.currentTimeMillis();
            System.out.println("DataShaper completed in " + (endTime - startTime) + " milliseconds");

            Date udate = new Date();
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.DEFAULT);
            System.out.println(df.format(udate) + "\n");

        } catch (Exception ex) {
            try {
                if (agent != null) {
                    agent.disconnect();
                }
            } catch (Exception exd) {
                exd.printStackTrace();
            }
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
