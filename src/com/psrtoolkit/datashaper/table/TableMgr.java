/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Manage the data tables and their traversal counts
 */
package com.psrtoolkit.datashaper.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.psrtoolkit.datashaper.config.ConfigReader;
import com.psrtoolkit.datashaper.constants.NumericLimit;
import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.enumeration.FKeyProviderType;
import com.psrtoolkit.datashaper.enumeration.PopulationCalcMethod;
import com.psrtoolkit.datashaper.enumeration.PopulationMode;
import com.psrtoolkit.datashaper.enumeration.RecordIdType;
import com.psrtoolkit.datashaper.enumeration.TableRelationship;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.idmgr.IdMgr;
import com.psrtoolkit.datashaper.idmgr.IdMgrORCL;
import com.psrtoolkit.datashaper.idmgr.IdMgrPostgreSQL;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;
import com.psrtoolkit.datashaper.structure.DynamicStringArray;
import com.psrtoolkit.datashaper.util.CleanupUtil;
import com.psrtoolkit.datashaper.util.OperationUtil;
import com.psrtoolkit.datashaper.util.StringUtil;

//import com.psrtoolkit.datashaper.idmgr.IdMgrMYSQL;
/**
 * Manage and traverse the data tables hierarchy <p> Commit new records at end
 * of hierarchy cycle. Repeat the traversal through the hierarchy until reaching
 * maxTotalCountLimit threshold value.
 *
 * @author Wan
 *
 */
public class TableMgr {

    DbType dbType = DbType.NONE;
    private int maxHierarchyLevel = 1; //the maximum level in the dependency hierarchy
    private long maxTotalCountLimit = 0L; //maximum total number of records to be generated
    private long startTime = 0L;
    //private long startMemory = 0L;
    private int curReportInterval = 1;
    String userSignature = "";
    private List<DataTable> tableList = new ArrayList<DataTable>();
    private Map<String, DataTable> tableMap = new HashMap<String, DataTable>();
    boolean restrictedVersion = false;
    Properties defaultLicense = null;
    int defaultMaxTables = 0;
    int defaultMaxRecords = 0;

    public TableMgr() {
    }

    public TableMgr(boolean licensed, Properties defaultLicense) {
        this.restrictedVersion = licensed;
        this.defaultLicense = defaultLicense;

        String maxTbl = defaultLicense.getProperty("maxTables");
        if (maxTbl != null) {
            defaultMaxTables = Integer.parseInt(maxTbl);
        }

        String maxRecords = defaultLicense.getProperty("maxRecords");
        if (maxRecords != null) {
            defaultMaxRecords = Integer.parseInt(maxRecords);
        }
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    /**
     * Add table instance to the hierarchy
     *
     * @param table the DataTable instance to be added
     */
    public void addTable(DataTable table) {
        tableList.add(table);
        tableMap.put(table.getName(), table);
    }

    /**
     * Iterate through the tables hierarchy (no recursion!) to generate records
     * starting from level-one table and up. <p> For each table processed, allow
     * it to populate records up the specified 'chunk' size and then suspend the
     * populating action in that table. Move on to the next table at the same
     * level. When all tables at the same level have reached their 'chunk' size,
     * commit the transaction. <p> Continue the populating action in the tables
     * and commit at 'chunk' size at the same level until all the tables have
     * reached maxTotalCountLimit. <p> Move up to next higher level and repeat
     * the same process. <p> Continue in this fashion until all levels of the
     * hierarchy are reached and processed. <p>
     *
     * @return true for success; false for failure
     * @exception throws DataShaperException runtime exception
     */
    public boolean populate(Connection conn, IdMgr sharedIdMgr) {
        boolean success = false;
        long curTotalCount = 0L;
        long lastCurTotalCount = 0L;
        long curReadOnlyTotalCount = 0L;
        //long lastCurReadOnlyTotalCount = 0L;

//        if (defaultMaxRecords != StringUtil.MAX_NEW_RECORDS) // in case StringUtil constant class file is hacked
//        {
//            throw new DataShaperException("Fatal error: the program has been compromised, execution is aborted");
//        }

        buildDependencies(conn, sharedIdMgr);

        while (curTotalCount < maxTotalCountLimit) {
            for (int i = 0; i < maxHierarchyLevel; i++) {
                curTotalCount = 0L;
                curReadOnlyTotalCount = 0L;
                for (DataTable table : tableList) {
                    if (table.getHierarchyLevel() == (i + 1)) { //hierarchy level start with 1; i.e. not 0-based
                        if (table.getPopulationMode() == PopulationMode.CREATE_NEW) {
                            doPopulate(conn, table);
                        }
                    }
                    curTotalCount = curTotalCount + table.getCurTotalRecordsGenerated();
                    if (table.getPopulationMode() == PopulationMode.USE_EXISTING) {
                        curReadOnlyTotalCount = curReadOnlyTotalCount + table.getCurTotalRecordsGenerated();
                    }
                    if (restrictedVersion && curTotalCount >= StringUtil.MAX_NEW_RECORDS) {
                        break;
                    }
                }

                OperationUtil.commit(conn);
                if ((curTotalCount - curReadOnlyTotalCount) > (curReportInterval * NumericLimit.ONEHUNDRED_THOUSAND)) {
                    System.out.println("Interim summary of records created:");
                    System.out.println("Completed creation of " + (curTotalCount - curReadOnlyTotalCount) + " records");
                    for (DataTable t : tableList) {
                        if (t.getPopulationMode() == PopulationMode.CREATE_NEW) {
                            System.out.println("\t" + t.getName() + ": " + t.getCurTotalRecordsGenerated());
                        }
                    }
                    System.out.println("Remaining number of records to be created: " + (maxTotalCountLimit - curTotalCount - curReadOnlyTotalCount));
                    long endTime = System.currentTimeMillis();
                    System.out.println("Percent completed: " + (curTotalCount - curReadOnlyTotalCount) * 100 / maxTotalCountLimit);
                    System.out.println("Cummulative elapsed time: " + (endTime - startTime) + " milliseconds");
                    //long endMemory = Runtime.getRuntime().totalMemory() -
                    //Runtime.getRuntime().freeMemory();
                    //System.out.println("Cummulative memory used: " + (endMemory - startMemory) + " bytes");
                    System.out.println("***************************************************\n");
                    curReportInterval++;
                }

                //stop processing when every table has reach its maximum 
                //count of generated records
                if (curTotalCount == maxTotalCountLimit) {
                    System.out.println("*************** Summary of records created: **************");
                    System.out.println("Completed run and reached maxTotalCountLimit: " + (curTotalCount - curReadOnlyTotalCount));
                    for (DataTable t : tableList) {
                        if (t.getPopulationMode() == PopulationMode.CREATE_NEW) {
                            System.out.println("\t" + t.getName() + ": " + t.getCurTotalRecordsGenerated());
                        }
                    }
                    break;
                }

                if (restrictedVersion && curTotalCount >= StringUtil.MAX_NEW_RECORDS) {
                    System.out.println("The number of new records created has exceeded the maximum "
                            + StringUtil.MAX_NEW_RECORDS + "-record limit");
                    System.out.println("*************** Summary of records created: **************");
                    for (DataTable t : tableList) {
                        if (t.getPopulationMode() == PopulationMode.CREATE_NEW) {
                            System.out.println("\t" + t.getName() + ": " + t.getCurTotalRecordsGenerated());
                        }
                    }
                    return success;
                }

                //something wrong happened, the run is unable to reach its maxTotalCountLimit
                if (i == (maxHierarchyLevel - 1)) {
                    if (lastCurTotalCount == curTotalCount) {
                        StringBuilder message2 = new StringBuilder(256);
                        message2.append("Error: something wrong happened in the run, it is unable to ").append("reach its maxTotalCountLimit (").append(maxTotalCountLimit).append("). ").append("The current total count stays at ").append(curTotalCount);
                        throw new DataShaperException(message2.toString());
                    }
                }

                lastCurTotalCount = curTotalCount;
                //lastCurReadOnlyTotalCount = curReadOnlyTotalCount;

                //check if staying at the current hierarchy level is required
                boolean advanceToNextLevel = true;
                for (DataTable table : tableList) {
                    if (table.getHierarchyLevel() == (i + 1)) { //hierarchy level start with 1; i.e. not 0-based
                        if (table.hasReachedMaxCountLimit() == false) {
                            i = i - 1;
                            advanceToNextLevel = false;
                            break;
                        }
                    }
                }

                if (advanceToNextLevel) {
                    for (DataTable table : tableList) {
                        if (table.getHierarchyLevel() == (i + 2)) { //next level
                            //recalculate if the table is relying on filtered results through filters
                            if (table.needToRecalulatePopulationSize() == true) {
                                reCalculateTablePopulation(table);
                            }
                        }
                    }
                }
            }
        }
        return success;
    }

    /**
     * Look up the DataTable instance by its name
     *
     * @param tableName the table name
     * @return the DataTable instance
     */
    public DataTable lookup(String tableName) {
        return tableMap.get(tableName);
    }

    public void setStartTime(long starTime) {
        this.startTime = starTime;
    }

    /*
     * public void setStartMemory(long startMemory) { this.startMemory =
     * startMemory; }
     */
    public void setUserSignature(String userSignature) {
        this.userSignature = userSignature;
    }

    private void buildDependencies(Connection conn, IdMgr sharedIdMgr) {
        int highestHierarchyLevel = 0;
        long maxCountLimit = 0L;
        Map<String, String> cyclicDependencyMap = new HashMap<String, String>();

//        if (defaultMaxTables != StringUtil.MAX_TABLES) //in case StringUtil constant class file is hacked
//        {
//            throw new DataShaperException("Fatal error: the program has been compromised, execution is aborted");
//        }

        if (restrictedVersion && tableList.size() > StringUtil.MAX_TABLES) {
            StringBuilder message = new StringBuilder();
            for (DataTable table : tableList) {
                message.append(table.getName()).append(" ");
            }
            throw new DataShaperException("Error: the number of tables specified in the configuration exceeds the " + StringUtil.MAX_TABLES + "-table limit \n"
                    + "\t" + message.toString());
        }

        boolean specialCase = false;

        for (DataTable table : tableList) {
            //INFO: fix up the references to the parent tables which were
            //loaded after the children during the configuration read
            //Also, fix up the filter references
            List<FKeyProvider> fKeyProviderArray = table.getfKeyProviderArray();
            for (FKeyProvider provider : fKeyProviderArray) {
                if (provider.getTable() == null) {
                    DataTable tbl = lookup(provider.getName());
                    provider.setTable(tbl);
                }
                provider.getTable().setHasChildren(true);
                if (provider.hasFilter()) {
                    provider.getTable().convertFilterToRule(provider.getFilter());
                }
                if (provider.hasDenorm()) {
                    if (provider.hasFilter()) {
                        provider.getTable().addFilterDenormSpecs(provider.getFilter(),
                                provider.getDenormSpecArray(), provider.getDenormColumnArray());
                    } else {
                        provider.getTable().addDenormSpecs(provider.getDenormSpecArray(),
                                provider.getDenormColumnArray());
                    }
                }
            }

            //check if there is any local override over shared idMgr
            String localSequenceName = table.getLocalSequence();
            if (localSequenceName.isEmpty() == true) {
                table.setIdMgr(sharedIdMgr);
            } else {
                if (localSequenceName.equals(ConfigReader.getSharedSequence()) == true) {
                    table.setIdMgr(sharedIdMgr);
                } else {
                    IdMgr idMgr = null;
                    if (ConfigReader.getDbType() == DbType.ORCL) {
                        idMgr = new IdMgrORCL(localSequenceName);
                        table.setIdMgr(idMgr);
                    } else if (ConfigReader.getDbType() == DbType.POSTGRESQL) {
                        idMgr = new IdMgrPostgreSQL(localSequenceName);
                        table.setIdMgr(idMgr);
                    } else {
                        throw new DataShaperException("Error: local sequence for "
                                + ConfigReader.getDbType().toString() + " not yet supported");
                    }
                    idMgr.setConnection(conn);
                }
            }

            //Special handling for MSSQLServer where IDENTITY_COLUMN is used
            //Override IDENTITY_INSERT with 'SET IDENTITY_INSERT table-name ON' and
            //reset it back to original with 'SET IDENTITY_INSERT table-name OFF after data insertion.
            //Do all three in one SQL statement so that the same session is used.
            //TODO: table name is logical. Need new method to return physical table name.
            specialCase = (table.getDbType() == DbType.MSSQLSERVER
                    && table.getPopulationCalcMethod() == PopulationCalcMethod.BY_INPUT_DATA
                    && table.getPopulationMode() == PopulationMode.CREATE_NEW);
            if (specialCase) {
                String onStr = " SET IDENTITY_INSERT [dbo].[" + table.getName() + "] ON ";
                String offStr = " SET IDENTITY_INSERT [dbo].[" + table.getName() + "] OFF ";
                String sqlStmt = onStr + table.getSqlStatement() + offStr;
                table.setSqlStmt(sqlStmt);
            }

        }

        //figure out the depth of each table in the hierarchy
        for (DataTable table : tableList) {
            cyclicDependencyMap.clear();
            int level = FindDepthTraveled(table, cyclicDependencyMap, 1);
            table.setHierarchyLevel(level);
        }

        for (DataTable table : tableList) {
            if (table.getHierarchyLevel() > highestHierarchyLevel) {
                highestHierarchyLevel = table.getHierarchyLevel();
            }
        }
        maxHierarchyLevel = highestHierarchyLevel;

        for (int i = 0; i < maxHierarchyLevel; i++) {

            //figure out the number of records to create for 1:1, 1:M and M:M tables
            //TODO: will combined FOR loops later
            for (DataTable table : tableList) {
                int size = 0;
                if (table.getHierarchyLevel() == (i + 1)) { //hierarchy level start with 1; i.e. not 0-based
                    if (table.getPopulationMode() == PopulationMode.CREATE_NEW) {
                        if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_SPECIFIED_NUMBER) {
                            //set usage count on fKeyProvider in case of 1:1 or 1:m
                            size = CalculateSize(table, false);
                        } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_INPUT_DATA) {
                            size = table.getInputDataListSize();
                            table.setMaxLimitInThisRun(size);
                        } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_DERIVATION) {
                            size = CalculateSize(table, false);
                            table.setMaxLimitInThisRun(size);
                        } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_QUERY) {
                            StringUtil.HandleInvalidOptions("implicit <size create-new=\"true\">", "by-query",
                                    "<table-population name=\"" + table.getName() + "\">", "<population-spec>");
                        }
                    } else if (table.getPopulationMode() == PopulationMode.USE_EXISTING) {
                        if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_INPUT_DATA) {
                            size = table.getInputDataListSize();
                            table.setMaxLimitInThisRun(size);
                            table.setCurTotalRecordsGenerated(size); //nothing to generate, so set the size now before the run
                            //table.setNewIdList(table.getAssignedIdList());
                        } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_QUERY) {
                            size = getSizeByQuery(conn, table);
                            table.setMaxLimitInThisRun(size);
                            table.setCurTotalRecordsGenerated(size);
                        } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_DERIVATION) {
                            StringUtil.HandleInvalidOptions("<size create-new=\"false\">", "by-derivation",
                                    "<table-population name=\"" + table.getName() + "\">", "<population-spec>");
                        }
                    }
                }
            }
        }

        for (DataTable table : tableList) {
            //if (table.getHierarchyLevel() > highestHierarchyLevel)
            //highestHierarchyLevel = table.getHierarchyLevel();

            int size = table.getMaxLimitInThisRun();
            if (size < table.getChunksize()) {
                table.setChunkSize(size);
            }
            maxCountLimit = maxCountLimit + table.getMaxLimitInThisRun();
            table.setUserSignature(userSignature);
            table.setRestrictedVersion(restrictedVersion);
        }
        //maxHierarchyLevel = highestHierarchyLevel;
        maxTotalCountLimit = maxCountLimit;
    }

    private void reCalculateTablePopulation(DataTable table) {
        int originalSize = table.getMaxLimitInThisRun();
        System.out.println(table.getName() + " original size: " + originalSize);

        int size = 0;
        if (table.getPopulationMode() == PopulationMode.CREATE_NEW) {
            if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_SPECIFIED_NUMBER) {
                //set usage count on fKeyProvider in case of 1:1 or 1:m
                size = CalculateSize(table, true);
            } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_INPUT_DATA) {
                size = table.getInputDataListSize();
                table.setMaxLimitInThisRun(size);
            } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_DERIVATION) {
                size = CalculateSize(table, true);
                table.setMaxLimitInThisRun(size);
            } else if (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_QUERY) {
                StringUtil.HandleInvalidOptions("implicit <size create-new=\"true\">", "by-query",
                        "<table-population name=\"" + table.getName() + "\">", "<population-spec>");
            }
        }
        //This is never true for level-2 or more children tables since they can't have existing data to begin with.
		/*
         * else if (table.getPopulationMode() == PopulationMode.USE_EXISTING) {
         * if (table.getPopulationCalcMethod() ==
         * PopulationCalcMethod.BY_INPUT_DATA) { size =
         * table.getInputDataListSize(); table.setMaxLimitInThisRun(size);
         * table.setCurTotalRecordsGenerated(size); //nothing to generate, so
         * set the size now before the run
         * //table.setNewIdList(table.getAssignedIdList()); } else if
         * (table.getPopulationCalcMethod() == PopulationCalcMethod.BY_QUERY) {
         * //size = getSizeByQuery(conn, table);
         * table.setMaxLimitInThisRun(size);
         * table.setCurTotalRecordsGenerated(size); } else if
         * (table.getPopulationCalcMethod() ==
         * PopulationCalcMethod.BY_DERIVATION) {
         * StringUtil.HandleInvalidOptions("<size create-new=\"false\">",
         * "by-derivation", "<table-population name=\"" + table.getName() +
         * "\">", "<population-spec>"); } }
         */

        //adjust the total count limit in this run
        if (originalSize > size) {
            maxTotalCountLimit = maxTotalCountLimit - (originalSize - size);
        }
        System.out.println("\t" + table.getName() + " size after filtering: " + size);

    }

    private int CalculateSize(DataTable table, boolean reCalculate) {

        if (table == null) {
            return 0;
        }

        List<FKeyProvider> fKeyProviders = table.getfKeyProviderArray();
        if (fKeyProviders == null) {
            return 0;
        }

        int length = fKeyProviders.size();
        if (length == 0) {
            return 0;
        }

        int primaryProviderSize = 0;
        int firstSecondary = 0;
        int firstPrimary = 0;
        for (FKeyProvider p : fKeyProviders) {
            if (p.getType() == FKeyProviderType.MAIN) {
                primaryProviderSize++;
                if (firstPrimary == 0) {
                    firstPrimary = primaryProviderSize;
                }
            } else if (p.getType() == FKeyProviderType.SECONDARY) {
                if (firstSecondary == 0) {
                    firstSecondary = primaryProviderSize;
                }
            }
        }

        StringBuilder message = new StringBuilder();
        if (table.getTableRelationship() == TableRelationship.ONE2MANY
                || table.getTableRelationship() == TableRelationship.ONE2ONE) {
            if (primaryProviderSize > 1) {
                message.append("Error: found more than one primary parent tables ").append("in 1:M relationship for table '").append(table.getName()).append("'");
                throw new DataShaperException(message.toString());
            }

        } else if (table.getTableRelationship() == TableRelationship.MANY2MANY) {
            if (primaryProviderSize > 2) {
                message.append("Error: found more than two primary parent tables ").append("in M:M relationship for table '").append(table.getName()).append("'");
                throw new DataShaperException(message.toString());
            }
        } else if (table.getTableRelationship() == TableRelationship.NONE) {
            message.append("Error: found one or more parents tables that").append(" have no relationship with table'").append(table.getName()).append("'");
            throw new DataShaperException(message.toString());
        }

        if (firstSecondary > firstPrimary) {
            message.append("Error: secondary provider must not appear before main provider(s) in table '"
                    + table.getName() + "'");
        }

        int size = 0;
        int size1 = 0;
        int size2 = 1;
        int forcedSize = 0;
        //int originalSize = 0;
        //int originalSize2 = 1; 
        int count = fKeyProviders.size();
        FKeyProvider provider = null;
        for (int i = 0; i < count; i++) {
            provider = fKeyProviders.get(i);
            DataTable parentTable = provider.getTable();
            if (provider.hasFilter() && parentTable.hasReachedMaxCountLimit()) {
                //The size can be determined after the parent table completed its data generation and populate the filtered id array
                if (parentTable.recordIdType == RecordIdType.NUMERIC) {
                    DynamicLongArray numericArr = parentTable.getFilteredNewIdList(provider.getFilter());
                    size = numericArr.lastpos();
                } else {
                    DynamicStringArray alphaNumericArr = parentTable.getFilteredNewAlphaNumericIdList(provider.getFilter());
                    size = alphaNumericArr.lastpos();
                }
            } else {
                size = parentTable.getMaxLimitInThisRun();
            }

            forcedSize = provider.getForcedPopulationSize();
            if (forcedSize > 0) {
                if (forcedSize > size) {
                    throw new DataShaperException("Error: the population size '" + forcedSize
                            + "' specified in foreign key provider '<fkey-provider name=\"" + provider.getName()
                            + "\">' in '<table-population name=\"" + table.getName() + "\">'"
                            + " is more than the parent table '" + parentTable.getName() + "' can provide");
                }
                size = forcedSize;
            } else {
                size = (int) (provider.getPercentage() * size) / 100;
                if (size == 0) {
                    size = 1;
                }
            }

            //Only the first two providers in the list matter in counting 
            //the total for the M:M table population size
            if (i == 0) {
                size1 = size;
            } else if (i == 1) {
                size2 = size;
            }

            if (reCalculate && i == 0) {
                if (primaryProviderSize == 1) {
                    System.out.println("\t" + table.getName() + "'s main provider '"
                            + provider.getName() + "' will provide " + size);
                } else {
                    System.out.println("\t" + table.getName() + "'s first main provider '"
                            + provider.getName() + "' will provide " + size);
                }
            }

            if (i != 1) {
                if (table.getTableRelationship() == TableRelationship.ONE2ONE) {
                    //meaning: each row id from the provider can be used in the 
                    //child table's foreign key column only once
                    provider.setUsageMaxCount(1);
                    //} else if (table.getTableRelationship() == TableRelationship.ONE2MANY) {
                } else {
                    //meaning: each row id from the provider can  be used in the 
                    //child table's foreign key column for 'size1' times
                    provider.setUsageMaxCount(size);
                }
            }

            if (i == 1 && primaryProviderSize == 2) {
                if (reCalculate) {
                    System.out.println("\t" + table.getName() + "'s second main provider '"
                            + provider.getName() + "' will provide " + size);
                }
                //meaning: each row id from the first provider can be used in the 
                //intersection table's first foreign key column for 'size2' times
                FKeyProvider provider1 = fKeyProviders.get(0);
                provider1.setUsageMaxCount(size2);
                //meaning: each row id from the second provider can be used in the 
                //intersection table's second foreign key column only one time
                provider.setUsageMaxCount(1);
            }
        }
        size = size1 * size2;
        return size;
    }

    /**
     * Recursively search for the hierarchy level for the table
     *
     * @param table
     * @param cyclicDependencyMap
     * @param depth current depth
     */
    private int FindDepthTraveled(DataTable table,
            Map<String, String> cyclicDependencyMap, int depth) {

        if (cyclicDependencyMap.get(table.getName()) != null) {
            StringBuilder message = new StringBuilder();
            message.append("Error: circular dependency detected in table-population '").append(table.getName()).append("'");
            throw new DataShaperException(message.toString());
        }

        cyclicDependencyMap.put(table.getName(), table.getName());
        List<FKeyProvider> fKeyProviderArray = table.getfKeyProviderArray();

        //increment depth level when there are parents found
        if (fKeyProviderArray.size() > 0) {
            depth++;
        }

        //loop through the parents and recursively advance the depth
        //check which parental route goes the deepest
        int deepestDepth = depth;
        for (FKeyProvider provider : fKeyProviderArray) {
            depth = FindDepthTraveled(provider.getTable(), cyclicDependencyMap, depth);
            if (depth > deepestDepth) {
                deepestDepth = depth;
            }
        }

        return deepestDepth;

    }

    private int getSizeByQuery(Connection conn, DataTable table) {
        Statement stmt = null;
        ResultSet result = null;
        int size = 0;
        String sqlStmt = null;

        try {
            if (conn == null) {
                throw new DataShaperException("Error: connection cannot be null");
            }

            if (table.hasExecutedQuery()) {
                return table.getNewIdList().lastpos();
            }

            stmt = conn.createStatement();
            StringBuilder sqlStr = new StringBuilder();

            sqlStmt = table.getSqlStatement();
            if (sqlStmt == null || sqlStmt.isEmpty()) {
                throw new DataShaperException("Error: table '" + table.getName() + "' does not have a SQL statment for execution");
            }
            String temp = sqlStmt.toUpperCase();

            String selectStr = "SELECT ";
            String fromStr = " FROM ";
            String countstarStr = "COUNT(*)";

            int first = temp.indexOf(selectStr);
            int second = temp.indexOf(fromStr);

            if (first < 0 || second < 0) {
                throw new DataShaperException("Error: invalid SQL statement found in table '" + table.getName() + "'");
            }

            sqlStr.append(sqlStmt.substring(first, selectStr.length())).append(countstarStr).append(sqlStmt.subSequence(second, sqlStmt.length()));

            result = stmt.executeQuery(sqlStr.toString());
            int count = 0;
            while (result.next()) {
                count = result.getInt(1);
            }

            if (count > NumericLimit.ONEMILLION) //limit to one million records
            {
                throw new DataShaperException("Error: the result size from the query in table '" + table.getName() + "' is more than 1 million");
            } else if (count == 0) {
                throw new DataShaperException("Error: no results are found in the query in table '" + table.getName() + "'");
            }
            CleanupUtil.close(result);

            result = stmt.executeQuery(sqlStmt);
            List<String> filterColumns = table.getColumnsPositionList();
            Map<String, Integer> filterColumnPositionMap = table.getColumnsPositionMap();
            int position = 0;
            String[] retreivedDataList = null;

            if (filterColumns != null && filterColumns.size() > 0) {
                retreivedDataList = new String[filterColumns.size() + 1]; //count the first column, which is always the ID column
            }

            if (table.getRecordIdType() == RecordIdType.NUMERIC) {
                long id = 0L;
                while (result.next()) {
                    id = Long.valueOf(result.getString(1));
                    table.getNewIdList().add(id);
                    if (filterColumns != null && filterColumns.size() > 0) {
                        for (String filterColumn : filterColumns) {
                            position = filterColumnPositionMap.get(filterColumn);
                            //TODO: handle non-string type columns
                            retreivedDataList[position - 1] = result.getString(position);
                        }
                        if (table.hasDenorm()) {
                            table.doDenorm(retreivedDataList);
                        }
                        table.doFilter(id, null, retreivedDataList);
                        Arrays.fill(retreivedDataList, null); //remove references to String objects
                    }
                }
                size = table.getNewIdList().lastpos();
            } else {
                String idStr = null;
                while (result.next()) {
                    idStr = result.getString(1);
                    table.getNewAlphaNumericIdList().add(idStr);
                    if (filterColumns != null && filterColumns.size() > 0) {
                        for (String filterColumn : filterColumns) {
                            position = filterColumnPositionMap.get(filterColumn);
                            //TODO: handle non-string type columns
                            retreivedDataList[position - 1] = result.getString(position);
                        }
                        if (table.hasDenorm()) {
                            table.doDenorm(retreivedDataList);
                        }
                        table.doFilter(-1, idStr, retreivedDataList);
                        Arrays.fill(retreivedDataList, null); //remove references to String objects
                    }

                }
                size = table.getNewAlphaNumericIdList().lastpos();
            }
            table.setHasExecutedQuery(true);
            return size;
        } catch (Exception ex) {
            StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_256);
            message.append("Error: failed to execute query [").append(sqlStmt).append("]").append(" in table '").append(table.getName()).append("'");
            throw new DataShaperException(message.toString(), ex);
        } finally {
            CleanupUtil.close(result);
            CleanupUtil.close(stmt);
        }
    }

    public void cleaup() {
        for (DataTable table : tableList) {
            table.cleanup();
        }
    }

    //TODO: will simplify the code further once a clear pattern emerged
    private void doPopulate(Connection conn, DataTable table) {
        if (table.getDbType() == DbType.MSSQLSERVER) {
            table.setUseReturnGeneratedKeyOption(false);
            table.populateNoBatch(conn);
        } else if (table.getDbType() == DbType.ORCL) {
            //Statement.RETURN_GENERATED_KEYS never seems to work in Oracle jdbc driver
            table.setUseReturnGeneratedKeyOption(false);
            if (table.hasSequenceInConfig()) {
                table.populate(conn);
            } else {
                table.populateNoBatch(conn);
            }
        } else if (table.getDbType() == DbType.DB2) {
            if (table.hasSequenceInConfig()) {
                table.populate(conn);
            } else {
                table.setUseReturnGeneratedKeyOption(false);
                table.populateNoBatch(conn);
            }
        } else if (table.getDbType() == DbType.POSTGRESQL) {
            //Statement.RETURN_GENERATED_KEYS never seems to work in PostgreSQL jdbc driver
            table.setUseReturnGeneratedKeyOption(false);
            if (table.hasSequenceInConfig()) {
                table.populate(conn);
            } else {
                table.populateNoBatch(conn);
            }
        } else {
            table.populate(conn);
        }
    }
}
