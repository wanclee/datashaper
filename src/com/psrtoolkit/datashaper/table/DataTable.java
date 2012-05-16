/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * DataTable represents a node in the dependency hierarchy
 *
 * Note: this file contains vendor specific class reference oracle.sql.BLOB and
 * oracle.sql.CLOB
 */
package com.psrtoolkit.datashaper.table;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//sorry, vendor-specific classes are referenced in this file
//import oracle.sql.BLOB;
//import oracle.sql.CLOB;

import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.enumeration.PopulationCalcMethod;
import com.psrtoolkit.datashaper.enumeration.PopulationMode;
import com.psrtoolkit.datashaper.enumeration.RecordIdType;
import com.psrtoolkit.datashaper.enumeration.TableRelationship;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.idmgr.IdMgr;
import com.psrtoolkit.datashaper.rules.RulesEngine;
import com.psrtoolkit.datashaper.rules.RulesExecutionStack;
import com.psrtoolkit.datashaper.structure.DynamicArrayOfStringArray;
import com.psrtoolkit.datashaper.structure.DynamicBlobArray;
import com.psrtoolkit.datashaper.structure.DynamicClobArray;
import com.psrtoolkit.datashaper.structure.DynamicIntArray;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;
import com.psrtoolkit.datashaper.structure.DynamicStringArray;
import com.psrtoolkit.datashaper.util.CleanupUtil;
import com.psrtoolkit.datashaper.util.StringUtil;

//import oracle.sql.BLOB;
//import oracle.sql.CLOB;
//import COM.ibm.db2.app.Clob;
/**
 * DataTable represents a node in the data table dependency hierarchy
 *
 * @author Wan
 *
 */
public class DataTable {

    public DataTable(String tableName) {
        this.name = tableName;
    }
    DbType dbType = DbType.NONE;
    RecordIdType recordIdType = RecordIdType.NUMERIC;
    //population mode
    PopulationMode populationMode = PopulationMode.CREATE_NEW;
    //population size calculation method
    PopulationCalcMethod popCalcMethod = PopulationCalcMethod.UNDEFINED;
    //table relationship
    TableRelationship relationship = TableRelationship.NONE;
    //depth in the dependency hierarchy;
    private int hierarchyLevel = 0;
    //counter for tracking current total number of records generated
    private int curTotalRecordsGenerated = 0;
    //maximum number of records that can be generated in the run
    private int maxLimitInThisRun = 0;
    //next index in inputDataList to access
    private int nextInputDataListIndexToAccess = 0;
    private int chunkSize = 100; //commit at 10 records at a time
    private int reachingChunkSizeCount = 0;
    private IdMgr idMgr = null;
    private String name = "";
    private String sqlStmt = "";
    private String userSignature = "";
    private String localSequenceName = "";
    private String[][] inputDataList = null;
    private String[] dataTypeList = null;
    private DynamicLongArray newIdList = new DynamicLongArray();
    private DynamicStringArray newAlphaNumericIdList = new DynamicStringArray();
    //parent tables who supply the FKEY values to this table
    //applicable only to 1:1, 1:M, and M:M relationship
    private Map<String, FKeyProvider> fKeyProviderMap = null;
    private List<FKeyProvider> fKeyProviderArray = new ArrayList<FKeyProvider>();
    private Map<String, FKeyProvider> fKeyProviderMapByDenormSpec = null;
    //secondary provider support
    private Map<String, FKeyProvider> fKeySecondaryProviderMap = null;
    private List<FKeyProvider> fKeySecondaryProviderArray = null;
    //private Map<String, FKeyProvider> fKeySecondaryProviderMapByDenormSpec = null;
    //filter support
    private Map<String, DynamicLongArray> filteredNewIdListMap = null;
    private Map<String, DynamicStringArray> filteredNewAlphaNumericIdListMap = null;
    private Map<String, RulesExecutionStack<String>> rulesExecutionStackMap = null;
    private ArrayList<String> filterList = new ArrayList<String>();
    private Map<String, DynamicIntArray> returnGeneratedKeysFitlerRulesMatchingStatusArrayMap = null;
    //bind variable column position support
    private Map<String, Integer> columnsPositionMap = null;
    private List<String> columnsPositionList = null;
    //denorm column support
    private Map<String, Map<String, String>> filteredDenormSpecsMap = null;
    private ArrayList<String> filteredDenormSpecsArray = null;
    private Map<String, String> denormSpecsMap = null;
    private ArrayList<String> denormSpecsArray = null;
    private DynamicArrayOfStringArray denormSubstitutedDataCache = null;
    private Map<String, DynamicArrayOfStringArray> filteredDenormSubstitutedDataCacheMap = null;
    private boolean hasChildren = false;
    private boolean hasExecutedQuery = false;
    private boolean hasExternallySuppliedIDs = false;
    private boolean useReturnGeneratedKeyOption = false;
    private boolean hasFilters = false;
    private boolean hasDenorm = false;
    private boolean hasFilteredDenorm = false;
    private boolean restrictedVersion = false;
    //LOB objects support
    private DynamicBlobArray blobArr = null;
    private DynamicClobArray clobArr = null;
    private HashMap<String, String> clobsMap = null;
    private HashMap<String, byte[]> blobsMap = null;

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public void setPopulationMode(PopulationMode mode) {
        this.populationMode = mode;
    }

    public void setPopulationCalcMethod(PopulationCalcMethod method) {
        this.popCalcMethod = method;
    }

    public void setTableRelationship(TableRelationship relationship) {
        this.relationship = relationship;
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public void setMaxLimitInThisRun(int limit) {
        this.maxLimitInThisRun = limit;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setSqlStmt(String sqlStmt) {
        this.sqlStmt = sqlStmt;
    }

    public void setUserSignature(String userSignature) {
        this.userSignature = userSignature;
    }

    public void addInputDataList(String[][] inputDataList) {
        this.inputDataList = inputDataList;
    }

    public void addDataTypeList(String[] dataTypeList) {
        this.dataTypeList = dataTypeList;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public void setNewIdList(DynamicLongArray idList) {
        this.newIdList = idList;
    }

    public void setCurTotalRecordsGenerated(int curTotalRecordsGenerated) {
        this.curTotalRecordsGenerated = curTotalRecordsGenerated;
    }

    public void setHasExecutedQuery(boolean queryExecuted) {
        this.hasExecutedQuery = queryExecuted;
    }

    public void setUseReturnGeneratedKeyOption(boolean useReturnGeneratedKeyOption) {
        this.useReturnGeneratedKeyOption = useReturnGeneratedKeyOption;
    }

    public void setHasExternallySuppliedIDs(boolean hasExternallySuppliedIDs) {
        this.hasExternallySuppliedIDs = hasExternallySuppliedIDs;
    }

    public void setLocalSequence(String sequenceName) {
        localSequenceName = sequenceName;
    }

    public void setIdMgr(IdMgr idMgr) {
        this.idMgr = idMgr;
    }

    public void setRecordIdType(RecordIdType idType) {
        this.recordIdType = idType;
    }

    public void setRestrictedVersion(boolean restrictedFlag) {
        this.restrictedVersion = restrictedFlag;
    }

    /**
     * Add the instance of FKeyProvider to the map by its key. <p> This method
     * is relied upon by TableMgr to perform circular dependency checking.
     *
     * @param key String value contain the foreigh key provider specification in
     * the form of ${ds-fkey-provider}=tableName
     * @param fKeyProvider an instance of FKeyProvider which references the
     * parent table on behalf of 'this' table instance
     */
    public void addFKeyProvider(String key, FKeyProvider fKeyProvider) {
        if (fKeyProviderMap == null) {
            fKeyProviderMap = new HashMap<String, FKeyProvider>();
        }
        if (fKeyProviderArray == null) {
            fKeyProviderArray = new ArrayList<FKeyProvider>();
        }
        if (fKeyProviderMap.get(key) == null) {
            fKeyProviderMap.put(key, fKeyProvider);
            fKeyProviderArray.add(fKeyProvider);
        }
    }

    public void addFKeySecondaryProvider(String key, FKeyProvider fKeyProvider) {
        if (fKeySecondaryProviderMap == null) {
            fKeySecondaryProviderMap = new HashMap<String, FKeyProvider>();
        }
        if (fKeySecondaryProviderArray == null) {
            fKeySecondaryProviderArray = new ArrayList<FKeyProvider>();
        }

        if (fKeySecondaryProviderMap.get(key) == null) {
            fKeySecondaryProviderMap.put(key, fKeyProvider);
            fKeySecondaryProviderArray.add(fKeyProvider);
        }
    }

    /**
     * Used in supporting denormalized column value specification behavior;
     * another way of adding the instance of FKeyProvider to the map by its key.
     *
     * @param key String value contain the denormalized column value
     * specification in the form of ${ds-denorm-val}=tableName.columnName
     * @param fKeyProvider an instance of FKeyProvider which references the
     * parent table on behalf of 'this' table instance
     */
    public void addFKeyProviderByDenormSpec(String denormSpec, FKeyProvider fKeyProvider) {
        if (fKeyProviderMapByDenormSpec == null) {
            fKeyProviderMapByDenormSpec = new HashMap<String, FKeyProvider>();
        }
        if (fKeyProviderMapByDenormSpec.get(denormSpec) == null) {
            fKeyProviderMapByDenormSpec.put(denormSpec, fKeyProvider);
        }
    }

    /**
     * Convert the filter spec into executable RulesExecutionStack instance by
     * calling the rules engine to parse the filter. <p> Examples of filter
     * specification in the configuration xml file: example 1:
     * <filter>TIMEZONE='UTC-7'</filter> example 2: <filter>AGE > 18 AND AGE <
     * 55</filter>
     *
     * @param filter String value containing the filter specification in the
     * form of columnName = 'something' with AND, OR, PARENTHESIS combination
     */
    public void convertFilterToRule(String filter) {
        if (rulesExecutionStackMap == null) {
            rulesExecutionStackMap = new HashMap<String, RulesExecutionStack<String>>();
        }
        if (rulesExecutionStackMap.get(filter) == null) {
            RulesExecutionStack<String> ruleExecStack = RulesEngine.parseRules(filter);
            rulesExecutionStackMap.put(filter, ruleExecStack);
            filterList.add(filter);
            hasFilters = true;
        }
        if (recordIdType == RecordIdType.NUMERIC) {
            if (filteredNewIdListMap == null) {
                filteredNewIdListMap = new HashMap<String, DynamicLongArray>();
            }
            if (filteredNewIdListMap.get(filter) == null) {
                filteredNewIdListMap.put(filter, new DynamicLongArray());
            }
        } else {
            if (filteredNewAlphaNumericIdListMap == null) {
                filteredNewAlphaNumericIdListMap = new HashMap<String, DynamicStringArray>();
            }
            if (filteredNewAlphaNumericIdListMap.get(filter) == null) {
                filteredNewAlphaNumericIdListMap.put(filter, new DynamicStringArray());
            }
        }
        if (useReturnGeneratedKeyOption) {
            if (returnGeneratedKeysFitlerRulesMatchingStatusArrayMap == null) {
                returnGeneratedKeysFitlerRulesMatchingStatusArrayMap = new HashMap<String, DynamicIntArray>();
            }
            if (returnGeneratedKeysFitlerRulesMatchingStatusArrayMap.get(filter) == null) {
                returnGeneratedKeysFitlerRulesMatchingStatusArrayMap.put(filter, new DynamicIntArray());
            }
        }
    }

    /**
     * @param columnName String representing the column name corresponding to
     * the bind variable
     * @param position the integer value indicating the position in the bind
     * variable list
     */
    public void addBindVariableColumnPosition(String columnName, int position) {
        if (columnsPositionMap == null) {
            columnsPositionMap = new HashMap<String, Integer>();
        }
        if (columnsPositionList == null) {
            columnsPositionList = new ArrayList<String>();
        }

        if (columnsPositionMap.get(columnName) != null) {
            throw new DataShaperException("Error: duplicate column found");
        }
        columnsPositionMap.put(columnName, position);

        columnsPositionList.add(columnName);
    }

    /**
     * The method is called by the foreign key provider when it has <filter>
     * specification in its table-population specification which reference
     * 'this' table
     *
     * @param filter filtering specification
     * @param denormSpecs list of denormalized column value specifications
     * @param denormColumns list of column names
     */
    public void addFilterDenormSpecs(String filter, List<String> denormSpecs, List<String> denormColumns) {
        if (filteredDenormSpecsMap == null) {
            filteredDenormSpecsMap = new HashMap<String, Map<String, String>>();
        }

        Map<String, String> existDenormSpecsMap = filteredDenormSpecsMap.get(filter);
        if (existDenormSpecsMap == null) {
            existDenormSpecsMap = new HashMap<String, String>();
            filteredDenormSpecsMap.put(filter, existDenormSpecsMap);
        }

        if (filteredDenormSpecsArray == null) {
            filteredDenormSpecsArray = new ArrayList<String>();
        }

        String denormSpecStr = null;
        String columnName = null;
        for (int i = 0; i < denormSpecs.size(); i++) {
            denormSpecStr = denormSpecs.get(i);
            columnName = denormColumns.get(i);
            if (existDenormSpecsMap.get(denormSpecStr) == null) {
                existDenormSpecsMap.put(denormSpecStr, columnName);
                filteredDenormSpecsArray.add(denormSpecStr);
            }
        }
        hasFilteredDenorm = true;
    }

    /**
     * The method is called by the foreign key provider
     *
     * @param filter filtering specification
     * @param denormSpecs list of denormalized column value specifications
     * @param denormColumns list of column names
     */
    public void addDenormSpecs(List<String> denormSpecs, List<String> denormColumns) {
        if (denormSpecsMap == null) {
            denormSpecsMap = new HashMap<String, String>();
        }

        if (denormSpecsArray == null) {
            denormSpecsArray = new ArrayList<String>();
        }

        String denormSpecStr = null;
        String columnName = null;
        for (int i = 0; i < denormSpecs.size(); i++) {
            denormSpecStr = denormSpecs.get(i);
            columnName = denormColumns.get(i);
            if (denormSpecsMap.get(denormSpecStr) == null) {
                denormSpecsMap.put(denormSpecStr, columnName);
                denormSpecsArray.add(denormSpecStr);
            }
        }
        hasDenorm = true;
    }

    /**
     * Used only when filter is specified in the configuration
     *
     * @return true if recalculation of population size after filtering
     * specification is executed by the rules engine; otherwise false
     */
    public boolean needToRecalulatePopulationSize() {
        boolean recalulatePopulationSize = false;

        if (relationship == TableRelationship.NONE) {
            recalulatePopulationSize = false;
        }

        if (fKeyProviderArray.size() == 0) {
            recalulatePopulationSize = false;
        }

        for (FKeyProvider provider : fKeyProviderArray) {
            if (provider.hasFilter()) {
                recalulatePopulationSize = true;
                break;
            }
        }
        return recalulatePopulationSize;

    }

    /**
     * Perform the task of identify and storing the field value into the data
     * cache, which will be used to populate denormalized columns later. There
     * is only just one cache for all the denorm columns in the table.
     *
     * @param substitutedData String[] of data whose place-holders have been
     * fully substituted
     */
    public void doDenorm(String[] substitutedData) {
        if (substitutedData == null) {
            return;
        }

        int size = substitutedData.length;
        if (size == 0) {
            return;
        }

        int denormArrSize = denormSpecsArray.size();

        if (denormArrSize == 0) {
            return;
        }

        String[] dataList = new String[size];
        String columnName = null;
        Integer bindvarPos = null;

        if (denormSubstitutedDataCache == null) {
            denormSubstitutedDataCache = new DynamicArrayOfStringArray();
        }

        for (String denormSpec : denormSpecsArray) {
            columnName = denormSpecsMap.get(denormSpec);
            if (columnName == null) {
                throw new DataShaperException("Error: unable to find column '"
                        + columnName + "' that is referenced in the denorm spec '" + denormSpec + "'");
            }
            bindvarPos = columnsPositionMap.get(columnName);
            if (bindvarPos == null) {
                throw new DataShaperException("Error: unable to find the bind variable position for column '"
                        + columnName + "' that is referenced in the denorm spec '" + denormSpec + "'");
            }

            dataList[bindvarPos.intValue() - 1] = substitutedData[bindvarPos.intValue() - 1];
        }
        denormSubstitutedDataCache.add(dataList);

    }

    /**
     * Perform the task of identify and storing the field value into the data
     * cache by using filter as key, which will be used to populate denormalized
     * columns later. There is only just one cache per filter for all the denorm
     * columns in the table. <p> If the table has multiple filters (usually as a
     * result of multiple FKeyProviders having different filters), each filter
     * will be associated with a separate data cache for denorm use.
     *
     * @param substitutedData String[] of data whose place-holders have been
     * fully substituted
     */
    private void doFilterDenorm(String filter, String[] substitutedData) {
        if (substitutedData == null) {
            return;
        }

        int size = substitutedData.length;
        if (size == 0) {
            return;
        }

        int denormArrSize = filteredDenormSpecsArray.size();

        if (denormArrSize == 0) {
            return;
        }

        if (filteredDenormSubstitutedDataCacheMap == null) {
            filteredDenormSubstitutedDataCacheMap = new HashMap<String, DynamicArrayOfStringArray>();
        }

        DynamicArrayOfStringArray arrArr = filteredDenormSubstitutedDataCacheMap.get(filter);
        if (arrArr == null) {
            arrArr = new DynamicArrayOfStringArray();
            filteredDenormSubstitutedDataCacheMap.put(filter, arrArr);
        }

        String[] dataList = new String[size];
        Map<String, String> denormsMap = null;
        String columnName = null;
        Integer bindvarPos = null;

        denormsMap = filteredDenormSpecsMap.get(filter);
        for (String denormSpec : filteredDenormSpecsArray) {
            columnName = denormsMap.get(denormSpec);
            if (columnName == null) {
                throw new DataShaperException("Error: unable to find column '"
                        + columnName + "' that is referenced in the denorm spec '" + denormSpec + "'");
            }
            bindvarPos = columnsPositionMap.get(columnName);
            if (bindvarPos == null) {
                throw new DataShaperException("Error: unable to find the bind variable position for column '"
                        + columnName + "' that is referenced in the denorm spec '" + denormSpec + "'");
            }

            dataList[bindvarPos.intValue() - 1] = substitutedData[bindvarPos.intValue() - 1];
        }

        arrArr.add(dataList);
    }

    /**
     * Create new records until reaching the threshold value -
     * maxLimitInThisRun. <p> When reachingChunkSizeCount reaches the chunkSize,
     * the creation is suspended to allow other tables in the hierarchy to be
     * processed. Data TableMgr is managing the traversal of hierarchy. <p> The
     * creation process resumes when Data TableMgr start a new traversal cycle
     * through the tables hierarchy. <p> Key concept: a table is only allowed to
     * create up to maxLimitInThisRun records, but a commit in chunks of record
     * is preferred over a commit of all records at the end. <p> When the table
     * reached the specified chunk size, it suspends itself and allows other
     * tables to run. <p> Note: transaction in the connection is not committed
     * here; instead, the Data TableMgr will do that at the end of traversal
     * through the hierarchy. <p> The method is suitable for jdbc drivers that
     * support executeBatch() or combined
     * Statement.RETURN_GENERATED_KEYS/executeBatch(). <p> For example, MySQL
     * works well with Statement.RETURN_GENERATED_KEYS/executeBatch(), but not
     * with executeBatch() alone since it doesn't support sequence.
     *
     * @param conn database connection
     * @return true as success; false as failure
     * @exception throws DataShaperException runtime exception
     */
    public boolean populate(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            if (conn == null) {
                throw new DataShaperException("Error: connection cannot be null\n" + "Check table spec - " + name);
            }

            if (maxLimitInThisRun == 0) {
                throw new DataShaperException("Error: The maximum total number of records limit cannot be zero\n" + "Check table spec - " + name);
            }

            if (chunkSize == 0) {
                throw new DataShaperException("Error: The maximum chunk size cannot be zero\n" + "Check table spec - " + name);
            }

            if (sqlStmt == null || sqlStmt.isEmpty()) {
                throw new DataShaperException("Error: The insert SQL statement cannot be empty\n" + "Check table spec - " + name);
            }

            if (hasReachedMaxCountLimit()) {
                return true;
            }

            if (hasReachedChunkSize()) {
                return true;
            }

            if (hasReachedRestriction()) {
                return true;
            }

            conn.setAutoCommit(false);
            if (useReturnGeneratedKeyOption == true) {
                stmt = conn.prepareStatement(sqlStmt, Statement.RETURN_GENERATED_KEYS);
            } else {
                stmt = conn.prepareStatement(sqlStmt);
            }

            String[] substitutedData = null;

            for (int i = 0; i < maxLimitInThisRun; i++) {
                if (hasReachedMaxCountLimit()) {
                    break;
                }

                //suspend the run until TableMgr starts new traversal cycle 
                //through the tables hierarchy.
                if (hasReachedChunkSize()) {
                    reachingChunkSizeCount = 0; //reset to zero
                    break;
                }

                if (hasReachedRestriction()) {
                    break;
                }

                //pick up from where it was last left off before the suspension
                int startFrom = nextInputDataListIndexToAccess;

                for (int k = startFrom; k < inputDataList.length; k++) {
                    //clone the inputdata String array since modifications may occur after substitution is performed
                    if (hasFilters || hasDenorm) {
                        substitutedData = inputDataList[k].clone();
                    }
                    doBinding(stmt, inputDataList[k], dataTypeList, k, substitutedData);

                    if (hasDenorm) {
                        doDenorm(substitutedData);
                    }

                    //if there are filters to process, perform the complex filtering work and cache results
                    if (hasFilters) {
                        doFilter(-1L, null, substitutedData);
                    }
                    if (substitutedData != null) {
                        Arrays.fill(substitutedData, null); //remove references 
                    }
                    stmt.addBatch();
                    nextInputDataListIndexToAccess = (k + 1) % inputDataList.length;

                    curTotalRecordsGenerated++;
                    reachingChunkSizeCount++;
                    if (curTotalRecordsGenerated == maxLimitInThisRun) {
                        break;
                    }

                    if (reachingChunkSizeCount == chunkSize) {
                        //INFO: do not reset reachingChunkSizeCount in the inner For loop.
                        //Should let the outer FOR loop to verify if hasReachedChunkSize() is true.
                        //reachingChunkSizeCount = 0L; //reset to zero.
                        break;
                    }

                    if (hasReachedRestriction()) {
                        break;
                    }
                }
            }

            stmt.executeBatch();
            cleanBlobsAndClobs(); //free any references to temporary Blob and Clob object instances.

            //for some jdbc drivers such as MySql, this is the way to get generated keys from the server after insertion
            if (useReturnGeneratedKeyOption == true && hasChildren == true) {
                result = stmt.getGeneratedKeys();
                if (recordIdType == RecordIdType.NUMERIC) {
                    long newId = 0L;
                    while (result.next()) {
                        newId = result.getLong(1);
                        newIdList.add(newId);
                        if (hasFilters) {
                            doFilterWithReturnedGeneratedKeys(newId, null, newIdList.lastpos() - 1);
                        }
                    }
                } else {
                    String newIdStr = null;
                    while (result.next()) {
                        newIdStr = result.getString(1);
                        newAlphaNumericIdList.add(newIdStr);
                        if (hasFilters) {
                            doFilterWithReturnedGeneratedKeys(-1, newIdStr, newAlphaNumericIdList.lastpos() - 1);
                        }
                    }
                }
            }

            //INFO: do not commit here; instead, leave it to TableMgr to do it.
            //conn.commit();
            return true;
        } catch (BatchUpdateException qex) {
            StringUtil.handleChainOfNextExceptionsAndCauses(qex, sqlStmt);
            return false;
        } catch (SQLException sqlex) {
            StringUtil.handleChainOfNextExceptionsAndCauses(sqlex, sqlStmt);
            return false;
        } catch (Exception ex) {
            StringUtil.handleChainOfNextExceptionsAndCauses(ex, sqlStmt);
            return false;
        } finally {
            CleanupUtil.close(result);
            CleanupUtil.close(stmt);
        }
    }

    /**
     * doFilter performs complex filtering algorithm. <p> The method enumerates
     * through the filter list, and executes the rules engine on the filtering
     * rules on the substituted String[] data. <p> Special case:
     * useReturnGeneratedKeyOption is a special case where a block of newly
     * generated ids are returned via
     * Statement.RETURN_GENERATED_KEYS/executeBatch() combination option, the
     * inputdata list has been bound to the insert statement, but the ids won't
     * be available until insertion is performed. The rules execution results,
     * both passes and failures, on the input data list are stored temporarily
     * in rulesMatchingStatusArray associated with the filter. Each filter will
     * have one corresponding rulesMatchingStatusArray that can be found in
     * returnGeneratedKeysFitlerRulesMatchingStatusArrayMap. The map acts like a
     * temporary cache and will be accessed again when the block ids are
     * returned. Such step is performed in doFilterWithReturnedGeneratedKeys()
     * call. This is very memory intensive as every filter will need a dynamic
     * array up to size of table's population size; the use of DynamicIntArray
     * to store passes or failures is to save memory space. <p> Normal case 1:
     * the new ids are pre-fetched from Sequence in the schema from db server
     * prior to insertion of records. Normal case 2: the new ids are returned
     * through getGeneratedIndexes/executeUpate() combination, one per inputdata
     * If the rules under the filter are satisfied, the id will be added to
     * filteredNewIdList or filteredNewAlphaNumericIdList associated with the
     * filter. <p> The input parameter newId and newAlphaNumericId are mutually
     * exclusive since a table in the schema can only have either one, but not
     * both.
     *
     * @param newId numeric id, meaningful only if the record id type in the
     * table in the schema is numeric
     * @param newAlphaNumericId, meaningful only if the record id type in the
     * table in the schema is numeric
     * @param substitutedData inputdata that has been fully substituted and
     * bound to the insert statement
     */
    public void doFilter(long newId, String newAlphaNumericId, String[] substitutedData) {
        boolean rulesSatisfied = false;
        DynamicIntArray rulesMatchingStatusArray = null;
        if (returnGeneratedKeysFitlerRulesMatchingStatusArrayMap == null) {
            returnGeneratedKeysFitlerRulesMatchingStatusArrayMap = new HashMap<String, DynamicIntArray>();
        }

        for (String filter : filterList) {
            rulesSatisfied = RulesEngine.rulesSatisfied(rulesExecutionStackMap.get(filter), columnsPositionMap, substitutedData);
            rulesMatchingStatusArray = returnGeneratedKeysFitlerRulesMatchingStatusArrayMap.get(filter);
            if (rulesSatisfied) {
                if (useReturnGeneratedKeyOption) {
                    //the block of new ids has not been returned at this stage yet, so their rules execution
                    //status are temporarily stored in a status array.
                    rulesMatchingStatusArray.add(StringUtil.RULES_SATISFIED);
                } else {
                    if (recordIdType == RecordIdType.NUMERIC) {
                        DynamicLongArray idList = filteredNewIdListMap.get(filter);
                        if (newId < 0L) {
                            idList.add(Long.parseLong(substitutedData[0])); //id is pre-fetched
                        } else {
                            idList.add(newId); //id is obtained from getGeneratedIndexes()
                        }
                    } else {
                        DynamicStringArray alphaNumericIdList = filteredNewAlphaNumericIdListMap.get(filter);
                        if (newAlphaNumericId == null) {
                            alphaNumericIdList.add(substitutedData[0]); //id is pre-fetched
                        } else {
                            alphaNumericIdList.add(newAlphaNumericId); //id is obtained from getGeneratedIndexes()
                        }
                    }
                }

                //keeping data in cache for filtered denormalized columns
                if (hasFilteredDenorm) {
                    doFilterDenorm(filter, substitutedData);
                }

            } else {
                if (useReturnGeneratedKeyOption) {
                    rulesMatchingStatusArray.add(StringUtil.RULES_NOTSATISFIED);
                }
            }
        }
    }

    /**
     * The method handles the special case of filtering new ids arised from
     * Statement.RETURN_GENERATED_KEYS/executeBatch() combination option.
     *
     * @param newId numeric id
     * @param newIdStr alphanumeric id, mutually exclusive from numeric id
     * @param position the index position in the matching status array the holds
     * the pass or failure states of filtering rules
     */
    public void doFilterWithReturnedGeneratedKeys(long newId, String newIdStr, int position) {
        DynamicIntArray rulesMatchingStatusArray = null;
        DynamicLongArray idList = null;
        DynamicStringArray alphaNumericIdList = null;
        if (returnGeneratedKeysFitlerRulesMatchingStatusArrayMap == null) {
            returnGeneratedKeysFitlerRulesMatchingStatusArrayMap = new HashMap<String, DynamicIntArray>();
        }
        for (String filter : filterList) {
            rulesMatchingStatusArray = returnGeneratedKeysFitlerRulesMatchingStatusArrayMap.get(filter);
            if (rulesMatchingStatusArray.get(position) == StringUtil.RULES_SATISFIED) {
                if (recordIdType == RecordIdType.NUMERIC) {
                    idList = filteredNewIdListMap.get(filter);
                    idList.add(newId);
                } else {
                    alphaNumericIdList = filteredNewAlphaNumericIdListMap.get(filter);
                    alphaNumericIdList.add(newIdStr);
                }
            }
        }
    }

    /**
     * The method handles cases where the jdbc drivers provided by the vendors
     * do not support return of generated keys with executeBatch() or have bugs
     * (e.g. Oracle ojdbc5.jar and ojdbc6.jar) that prevents the successful
     * return of generated keys after batch insert. The method instead uses a
     * getGeneratedIndexes/executeUpdate() combination to work around the
     * problem. It is the common denominator where most of the drivers can
     * perform reliably well. <p> For db that supports Sequence, data shaper can
     * pre-fetch a block of ids through the sequence's nextval query and still
     * support batch insert. Call populate() method instead of this one. <p>
     * MySQL supports only auto increment column (similar to Identity), but it
     * works well with getGeneratedKeys/executeBatch(), so call populate()
     * method instead of this one. <p> MSSQLServer supports only Identity
     * column, and also it doesn't support getGeneratedKeys with executeBatch(),
     * thus this method is the only option MSSQLServer scenario will use.
     *
     * @param conn database connection
     * @return true if successful; otherwise false
     */
    public boolean populateNoBatch(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet result = null;
        boolean special = false;

        try {
            if (conn == null) {
                throw new DataShaperException("Error: connection cannot be null\n" + "Check table spec - " + name);
            }

            if (maxLimitInThisRun == 0) {
                throw new DataShaperException("Error: The maximum total number of records limit cannot be zero\n" + "Check table spec - " + name);
            }

            if (chunkSize == 0) {
                throw new DataShaperException("Error: The maximum chunk size cannot be zero\n" + "Check table spec - " + name);
            }

            if (sqlStmt == null || sqlStmt.isEmpty()) {
                throw new DataShaperException("Error: The insert SQL statement cannot be empty\n" + "Check table spec - " + name);
            }

            if (hasReachedMaxCountLimit()) {
                return true;
            }

            if (hasReachedChunkSize()) {
                return true;
            }

            if (hasReachedRestriction()) {
                return true;
            }

            conn.setAutoCommit(false);

            int[] generatedIndexes = new int[1];
            generatedIndexes[0] = 1;

            stmt = conn.prepareStatement(sqlStmt, generatedIndexes);
            String[] substitutedData = null;

            for (int i = 0; i < maxLimitInThisRun; i++) {
                if (hasReachedMaxCountLimit()) {
                    break;
                }

                //suspend the run until TableMgr starts new traversal cycle 
                //through the tables hierarchy.
                if (hasReachedChunkSize()) {
                    reachingChunkSizeCount = 0; //reset to zero
                    break;
                }

                if (hasReachedRestriction()) {
                    break;
                }

                //pick up from where it was last left off before the suspension
                int startFrom = nextInputDataListIndexToAccess;

                for (int k = startFrom; k < inputDataList.length; k++) {

                    if (hasFilters || hasDenorm) {
                        substitutedData = inputDataList[k].clone();
                    }
                    doBinding(stmt, inputDataList[k], dataTypeList, k, substitutedData);

                    if (hasDenorm) {
                        doDenorm(substitutedData);
                    }

                    stmt.executeUpdate();
                    cleanBlobsAndClobs(); //free any references to temporary Blob and Clob object instances.

                    if (hasChildren == true) {
                        result = stmt.getGeneratedKeys();
                        if (recordIdType == RecordIdType.NUMERIC) {
                            long newId = 0L;
                            while (result.next()) {
                                newId = result.getLong(1);
                                newIdList.add(newId);
                                if (hasFilters) {
                                    doFilter(newId, null, substitutedData);
                                }
                            }
                        } else {
                            String newIdStr = null;
                            while (result.next()) {
                                newIdStr = result.getString(1);
                                newAlphaNumericIdList.add(newIdStr);
                                if (hasFilters) {
                                    doFilter(-1L, newIdStr, substitutedData);
                                }
                            }
                        }
                    }
                    if (substitutedData != null) {
                        Arrays.fill(substitutedData, null); //remove references 
                    }
                    nextInputDataListIndexToAccess = (k + 1) % inputDataList.length;

                    curTotalRecordsGenerated++;
                    reachingChunkSizeCount++;
                    if (curTotalRecordsGenerated == maxLimitInThisRun) {
                        break;
                    }

                    if (reachingChunkSizeCount == chunkSize) {
                        //INFO: do not reset reachingChunkSizeCount in the inner For loop.
                        //Should let the outer FOR loop to verify if hasReachedChunkSize() is true.
                        //reachingChunkSizeCount = 0L; //reset to zero.
                        break;
                    }

                    if (hasReachedRestriction()) {
                        break;
                    }
                }
            }

            //INFO: do not commit here; instead, leave it to TableMgr to do it.
            //conn.commit();
            return true;
        } catch (BatchUpdateException qex) {
            StringUtil.handleChainOfNextExceptionsAndCauses(qex, sqlStmt);
            return false;
        } catch (SQLException sqlex) {
            StringUtil.handleChainOfNextExceptionsAndCauses(sqlex, sqlStmt);
            return false;
        } catch (Exception ex) {
            StringUtil.handleChainOfNextExceptionsAndCauses(ex, sqlStmt);
            return false;
        } finally {
            CleanupUtil.close(result);
            CleanupUtil.close(stmt);
        }
    }

    /**
     * DoBinding binds the input data to the bind variables specified in the row
     * specification in the data shape configuration file <p>
     *
     * Bind the variables to the statement; set appropriate parameter indexes
     * and data types
     *
     * @param stmt the prepared stmt
     * @param inputData the string array containing the tokens of data
     * @param dataTypeList the string array containing the JDBC datatypes for
     * the data tokens
     * @param parentIndex
     * @param substitutedInputData the String[] that contains data whose
     * place-holders have been substituted
     * @exception throws DataShaperException runtime exception
     */
    private void doBinding(PreparedStatement stmt, String[] inputData, String[] dataTypeList, int parentIndex, String[] substitutedInputData) {
        try {
            if (stmt == null) {
                throw new DataShaperException("Error: statement cannot be null or in closed state\n" + "Check table spec - " + name);
            }

            if (inputData == null || inputData.length == 0) {
                throw new DataShaperException("Error: inputData cannot be null or empty\n" + "Check table spec - " + name);
            }

            if (dataTypeList == null || dataTypeList.length == 0) {
                throw new DataShaperException("Error: dataTypeList cannot be null or empty\n" + "Check table spec - " + name);
            }

            if (inputData.length != dataTypeList.length) {
                throw new DataShaperException("\nError: inputData list must have the same size as dataTypeList\n" + "Check table spec - " + name + "\n"
                        + "The input data list size at row number <" + (parentIndex + 1) + "> is " + inputData.length + "\n"
                        + "and the data type list size is " + dataTypeList.length + "\n");
            }

            int k = 0;
            boolean alphanumeric = (recordIdType == RecordIdType.ALPHANUMERIC);
            String idStr = null;
            long id = -1L;
            String dataStr = null;
            String dataType = null;

            for (int i = 0; i < inputData.length; i++) {
                k = i + 1;
                dataStr = inputData[i];
                dataType = dataTypeList[i];
                if (inputData[i].equals(StringUtil.DS_NEXTVAL)) {
                    if (alphanumeric) {
                        idStr = idMgr.getNextAlphaNumericId();
                    } else {
                        id = idMgr.getNextId();
                    }

                    doBindingByDataType(stmt, k, dataType, id, idStr, dataStr);

                    if (hasChildren) {
                        if (alphanumeric) {
                            newAlphaNumericIdList.add(idStr);
                        } else {
                            newIdList.add(id);
                        }
                        if (substitutedInputData != null) {
                            substitutedInputData[i] = alphanumeric ? idStr : String.valueOf(id);
                        }
                    }
                } else if (dataStr.contains(StringUtil.DS_ROW_ID)) {
                    if (alphanumeric) {
                        if (i == 0 && newAlphaNumericIdList != null && newAlphaNumericIdList.lastpos() > parentIndex) {
                            idStr = newAlphaNumericIdList.get(parentIndex);
                            doBindingByDataType(stmt, k, dataType, id, idStr, dataStr);
                            if (substitutedInputData != null) {
                                substitutedInputData[i] = idStr;
                            }
                        } else {
                            throw new DataShaperException("Error: assigned row id must be"
                                    + " the first item in the input-data row");
                        }
                    } else {
                        if (i == 0 && newIdList != null && newIdList.lastpos() > parentIndex) {
                            id = newIdList.get(parentIndex);
                            doBindingByDataType(stmt, k, dataType, id, idStr, dataStr);
                            if (substitutedInputData != null) {
                                substitutedInputData[i] = String.valueOf(id);
                            }
                        } else {
                            throw new DataShaperException("Error: assigned row id must be"
                                    + " the first item in the input-data row");
                        }
                    }
                } else if (dataStr.contains(StringUtil.DS_FOREIGN_KEY_PROVIDER)
                        || dataStr.contains(StringUtil.DS_FOREIGN_KEY_SECONDARY_PROVIDER)) {
                    FKeyProvider fKeyProvider = fKeyProviderMap.get(dataStr);
                    if (fKeyProvider == null) {
                        StringUtil.HandleInvalidValue(dataStr,
                                "<input-data>", "<table name=\"" + name + "\">");
                    }
                    if (alphanumeric) {
                        idStr = fKeyProvider.getNextFKeyAlphaNumericId();
                    } else {
                        id = fKeyProvider.getNextFKeyId();
                    }

                    doBindingByDataType(stmt, k, dataType, id, idStr, dataStr);

                    if (substitutedInputData != null) {
                        substitutedInputData[i] = alphanumeric ? idStr : String.valueOf(id);
                    }
                } else if (dataStr.contains(StringUtil.DS_PICKLIST)) {
                    FKeyProvider fKeyProvider = fKeyProviderMapByDenormSpec.get(dataStr);
                    if (fKeyProvider == null) {
                        StringUtil.HandleInvalidValue(dataStr,
                                "<input-data>", "<table name=\"" + name + "\">");
                    }
                    //always get next id to 'prime' the current index position
                    if (alphanumeric) {
                        idStr = fKeyProvider.getNextFKeyAlphaNumericId();
                    } else {
                        id = fKeyProvider.getNextFKeyId();
                    }

                    String denormValue = fKeyProvider.getCurrentDenormValue(dataStr);
                    doBindingByDataType(stmt, k, dataType, id, idStr, denormValue);
                    if (substitutedInputData != null) {
                        substitutedInputData[i] = denormValue;
                    }
                } else if (dataStr.contains(StringUtil.DS_DENORM_VAL)) {
                    //Should arrive here only after DS_FOREIGN_KEY_PROVIDER or DS_FOREIGN_KEY_SECONDARY_PROVIDER are processed
                    FKeyProvider fKeyProvider = fKeyProviderMapByDenormSpec.get(dataStr);
                    if (fKeyProvider == null) {
                        StringUtil.HandleInvalidValue(dataStr,
                                "<input-data>", "<table name=\"" + name + "\">");
                    }
                    String denormValue = fKeyProvider.getCurrentDenormValue(dataStr);
                    doBindingByDataType(stmt, k, dataType, id, idStr, denormValue);
                    if (substitutedInputData != null) {
                        substitutedInputData[i] = denormValue;
                    }
                } else {
                    //TODO: need an array of sizes to be more precise; for now the max limit is 256.
                    String s = StringUtil.doSubstitution(dataStr, userSignature,
                            idMgr.getCurId(), idMgr.getCurAlphaNumericId(), 256);
                    doBindingByDataType(stmt, k, dataType, id, idStr, s);
                    if (substitutedInputData != null) {
                        substitutedInputData[i] = s;
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataShaperException("Error: failure in doBinding", ex);
        }
    }

    /**
     * @param stmt PreparedStatement
     * @param bindvarPos bind variable position, 1-based integer value
     * @param dataType data type for the column in the row specification
     * @param id used in numeric id scenario; if not used, its value should be
     * set to -1L
     * @param idStr used in alphanumeric id scenario (exclusive from id); if not
     * used, its value should be set to null
     * @param dataStr the data string to be bound to the bind variable. Used
     * only when both id and idStr are null.
     */
    private void doBindingByDataType(PreparedStatement stmt, int bindvarPos, String dataType, long id, String idStr, String dataStr) {
        try {
            if (dataType.equals("java.lang.String")) {
                if (idStr != null) {
                    stmt.setString(bindvarPos, idStr);
                } else {
                    stmt.setString(bindvarPos, dataStr);
                }
            } else if (dataType.equals("java.math.BigDecimal")) {
                if (id > 0L) {
                    stmt.setBigDecimal(bindvarPos, BigDecimal.valueOf(id));
                } else {
                    stmt.setBigDecimal(bindvarPos, BigDecimal.valueOf(Long.parseLong(dataStr)));
                }
            } else if (dataType.equals("boolean")) {
                stmt.setBoolean(bindvarPos, Boolean.parseBoolean(dataStr));
            } else if (dataType.equals("byte")) {
                stmt.setByte(bindvarPos, Byte.valueOf(dataStr));
            } else if (dataType.equals("short")) {
                stmt.setShort(bindvarPos, Short.parseShort(dataStr));
            } else if (dataType.equals("int")) {
                stmt.setInt(bindvarPos, Integer.parseInt(dataStr));
            } else if (dataType.equals("long")) {
                if (id > 0L) {
                    stmt.setLong(bindvarPos, id);
                } else {
                    stmt.setLong(bindvarPos, Long.parseLong(dataStr));
                }
            } else if (dataType.equals("float")) {
                stmt.setFloat(bindvarPos, Float.parseFloat(dataStr));
            } else if (dataType.equals("double")) {
                stmt.setDouble(bindvarPos, Double.parseDouble(dataStr));
            } else if (dataType.equals("byte[]")) {
                stmt.setBytes(bindvarPos, dataStr.getBytes());
            } else if (dataType.equals("java.sql.Date")) {
                stmt.setDate(bindvarPos, Date.valueOf(dataStr));
            } else if (dataType.equals("java.sql.Time")) {
                stmt.setTime(bindvarPos, Time.valueOf(dataStr));
            } else if (dataType.equals("java.sql.Timestamp")) {
                stmt.setTimestamp(bindvarPos, Timestamp.valueOf(dataStr));
            } else if (dataType.equals("java.sql.Clob")) {
                String inputDataStr = null;
                if (dataStr.contains(StringUtil.DS_TEXTFILE)) {
                    if (clobsMap == null) {
                        clobsMap = new HashMap<String, String>();
                    }

                    inputDataStr = clobsMap.get(dataStr);
                    if (inputDataStr == null) {
                        String[] tokens = dataStr.split("=");
                        if (tokens == null || tokens.length != 2 || tokens[1].isEmpty()) {
                            throw new DataShaperException("Error: unable to find or open external file referenced in place holder '"
                                    + dataStr + "'");
                        }
                        Scanner scanner = null;
                        FileInputStream istream = null;
                        StringBuilder text = null;
                        try {
                            istream = new FileInputStream(tokens[1]);
                            scanner = new Scanner(istream, "UTF-8");
                            text = new StringBuilder();
                            String newline = System.getProperty("line.separator");
                            while (scanner.hasNextLine()) {
                                text.append(scanner.nextLine() + newline);
                            }
                        } catch (Exception e) {
                            throw new DataShaperException("Error: unable to find or open external file referenced in place holder '"
                                    + dataStr + "'", e);
                        } finally {
                            if (scanner != null) {
                                scanner.close();
                            }
                            if (istream != null) {
                                istream.close();
                            }
                        }
                        inputDataStr = text.toString();
                        clobsMap.put(dataStr, inputDataStr);
                    }
                } else {
                    inputDataStr = dataStr;
                }
                //TODO: dataStr should be pointing to a large text file instead of holding a large text.
                //This is because large text in xml configuration file is not practical
                Clob clob = null;
                if (clobArr == null) {
                    clobArr = new DynamicClobArray(chunkSize);
                }
                if (dbType == DbType.ORCL) {
                    clob = oracle.sql.CLOB.createTemporary(idMgr.getConnection(), false, oracle.sql.CLOB.DURATION_SESSION);
                    clob.setString(1L, inputDataStr);
                    stmt.setClob(bindvarPos, clob);
                    /*
                     * } else if (dbType == DbType.DB2) {
                     * stmt.setString(bindvarPos, inputDataStr); } else if
                     * (dbType == DbType.MYSQL) { stmt.setString(bindvarPos,
                     * inputDataStr); } else if (dbType == DbType.POSTGRESQL) {
                     * stmt.setString(bindvarPos, inputDataStr); } else if
                     * (dbType == DbType.MSSQLSERVER) {
                     * stmt.setString(bindvarPos, inputDataStr);
                     */
                } else {
                    stmt.setString(bindvarPos, inputDataStr.toString());

                }
                clobArr.add(clob); //store it away for cleanup later to prevent memory leaks
                //stmt.setObject(bindvarPos, dataStr.getBytes());
            } else if (dataType.equals("java.sql.Blob")) {
                byte[] dataBytes = null;
                if (dataStr.contains(StringUtil.DS_BINARYFILE)) {
                    if (blobsMap == null) {
                        blobsMap = new HashMap<String, byte[]>();
                    }

                    dataBytes = blobsMap.get(dataStr);
                    if (dataBytes == null) {
                        String[] tokens = dataStr.split("=");
                        if (tokens == null || tokens.length != 2 || tokens[1].isEmpty()) {
                            throw new DataShaperException("Error: unable to find or open external file referenced in place holder '"
                                    + dataStr + "'");
                        }
                        FileInputStream istream = null;
                        try {
                            istream = new FileInputStream(tokens[1]);
                            dataBytes = new byte[istream.available()];
                            istream.read(dataBytes);
                        } catch (Exception e) {
                            throw new DataShaperException("Error: unable to find or open external file referenced in place holder '"
                                    + dataStr + "'", e);
                        } finally {
                            if (istream != null) {
                                istream.close();
                            }
                        }
                        blobsMap.put(dataStr, dataBytes);
                    }
                } else {
                    dataBytes = dataStr.getBytes();
                }
                //TODO: dataStr should be pointing to a large binary file instead of holding a large byte[].
                //This is because large binary in xml configuration file is not practical
                Blob blob = null;
                if (blobArr == null) {
                    blobArr = new DynamicBlobArray(chunkSize);
                }
                if (dbType == DbType.ORCL) {
                    blob = oracle.sql.BLOB.createTemporary(idMgr.getConnection(), false, oracle.sql.BLOB.DURATION_SESSION);
                    blob.setBytes(1L, dataBytes);
                    stmt.setBlob(bindvarPos, blob);
                } else if (dbType == DbType.POSTGRESQL) {
                    ByteArrayInputStream istream = new ByteArrayInputStream(dataBytes);
                    stmt.setBinaryStream(bindvarPos, istream, istream.available());
                    /*
                     * } else if (dbType == DbType.DB2) {
                     * stmt.setBinaryStream(bindvarPos, new
                     * ByteArrayInputStream(dataBytes)); } else if (dbType ==
                     * DbType.MYSQL) { stmt.setBinaryStream(bindvarPos, new
                     * ByteArrayInputStream(dataBytes)); } else if (dbType ==
                     * DbType.MSSQLSERVER) { stmt.setBinaryStream(bindvarPos,
                     * new ByteArrayInputStream(dataBytes));
                     */
                } else {
                    stmt.setBinaryStream(bindvarPos, new ByteArrayInputStream(dataBytes));
                }
                blobArr.add(blob);
            } else if (dataType.equals("java.sql.Array")) {
                throw new DataShaperException(
                        "Error: java.sql.Array data type is not yet supported");
            } else if (dataType.equals("java.sql.Ref")) {
                throw new DataShaperException(
                        "Error: java.sql.Ref data type is not yet supported");
            } else if (dataType.equals("java.sql.Struct")) {
                throw new DataShaperException(
                        "Error: java.sql.Struct data type is not yet supported");
            } else {
                StringBuilder message = new StringBuilder();
                message.append("Error: unknown or unsupported data type - ").append(dataType);
                throw new DataShaperException(message.toString());
            }
        } catch (Exception ex) {
            throw new DataShaperException("Error: failure in doBinding", ex);
        }
    }

    public void cleanup() {
        if (idMgr != null) {
            idMgr.cleanup();
            idMgr = null;
        }
    }

    /**
     * The method uses denormSpecStr to obtain the column name from
     * denormSpecsMap. It uses the index to obtain the index position into the
     * cache array. The entry in the cache array is further accessed using the
     * column position which can be obtained from columnsPositionMap by using
     * the column name as the key.
     *
     * @param index an integer indication the position into the data cache
     * @param denormSpecStr a String containing the denormalized column value
     * specification used as a key to obtain the column name from denorm specs
     * map.
     * @return the String value at the column position in the indexed entry in
     * the data cache.
     */
    public String getDenormValue(int index, String denormSpecStr) {
        String dataStr = null;
        String columnName = denormSpecsMap.get(denormSpecStr);
        if (columnName == null) {
            throw new DataShaperException("Error: unable to locate the column '"
                    + columnName
                    + "' specified in the denormalized value specification '"
                    + denormSpecStr + "'");
        }
        Integer position = columnsPositionMap.get(columnName);
        if (position == null) {
            throw new DataShaperException("Error: unable to find column '"
                    + columnName + "' that is referenced in the denorm spec '" + denormSpecStr + "'");
        }

        if (denormSubstitutedDataCache.lastpos() < index) {
            throw new DataShaperException("Error: the cache size for the generated data is smaller than the index value used to access the cache");
        }

        String[] dataArr = denormSubstitutedDataCache.get(index);

        int i = position.intValue();
        if (dataArr.length < i) {
            throw new DataShaperException("Error: the cache size for the generated data is smaller than the index value used to access the cache");
        }

        dataStr = dataArr[i - 1];
        return dataStr;
    }

    /**
     * The method uses the filter name to obtain the filter-specific denorm
     * specifications map from filteredDenormSpecsMap. It uses the denormSpecStr
     * to obtain the column name from the filter-specific denorm specifications
     * map. It uses the filter name to obtain the filter-specific data cache
     * from filteredDenormSubstitutedDataCacheMap. It uses the index to obtain
     * the indexed position into the data cache. The entry in the cache array is
     * further accessed using the column position which can be obtained from
     * columnsPositionMap by using the column name as the key.
     *
     * @param filter name of the filter
     * @param index an integer indication the position into the data cache
     * @param denormSpecStr a String containing the denormalized column value
     * specification used as a key to obtain the column name from denorm specs
     * map.
     * @return the String value at the column position in the indexed entry in
     * the data cache.
     */
    public String getFilterDenormValue(String filter, int index, String denormSpecStr) {
        String dataStr = null;
        Map<String, String> specsMap = filteredDenormSpecsMap.get(filter);
        if (specsMap == null) {
            throw new DataShaperException(
                    "Error: unable to locate the denormalized value specifications associated with the filter '"
                    + filter + "'");
        }

        String columnName = specsMap.get(denormSpecStr);
        if (columnName == null) {
            throw new DataShaperException("Error: unable to locate the column '"
                    + columnName
                    + "' specified in the denormalized column value specification '"
                    + denormSpecStr + "'");
        }

        Integer position = columnsPositionMap.get(columnName);
        if (position == null) {
            throw new DataShaperException("Error: unable to find column '"
                    + columnName + "' that is referenced in the denorm spec '" + denormSpecStr + "'");
        }

        DynamicArrayOfStringArray dataCache = filteredDenormSubstitutedDataCacheMap.get(filter);
        if (dataCache == null) {
            throw new DataShaperException(
                    "Error: unable to locate the data cache for the denormalized column value specification '"
                    + denormSpecStr + "' associated with the filter '"
                    + filter + "'");
        }
        if (dataCache.lastpos() < index) {
            throw new DataShaperException("Error: the cache size for the generated data is smaller than the index value used to access the cache");
        }

        String[] dataArr = dataCache.get(index);

        int i = position.intValue();
        if (dataArr.length < i) {
            throw new DataShaperException("Error: the cache size for the generated data is smaller than the index value used to access the cache");
        }

        dataStr = dataArr[i - 1];

        return dataStr;

    }

    private void cleanBlobsAndClobs() {
        try {
            if (clobArr != null) {
                for (int i = 0; i < clobArr.lastpos(); i++) {
                    if (dbType == DbType.ORCL) {
                        oracle.sql.CLOB cb = (oracle.sql.CLOB) clobArr.get(i);
                        if (cb != null) {
                            cb.freeTemporary();
                        }
                    } else {
                        Clob cbGeneral = clobArr.get(i);
                        if (cbGeneral != null) {
                            cbGeneral.free();
                        }
                    }
                }
                clobArr.clear();
            }

            if (blobArr != null) {
                for (int i = 0; i < blobArr.lastpos(); i++) {
                    if (dbType == DbType.ORCL) {
                        oracle.sql.BLOB b = (oracle.sql.BLOB) blobArr.get(i);
                        if (b != null) {
                            b.freeTemporary();
                        }
                    } else {
                        Blob bGeneral = blobArr.get(i);
                        if (bGeneral != null) {
                            bGeneral.free();
                        }
                    }
                }
                blobArr.clear();
            }
        } catch (Exception ex) {
            throw new DataShaperException("Error: unable to clean Blob or Clob references", ex);
        }
    }

    public DbType getDbType() {
        return dbType;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public String getName() {
        return name;
    }

    public int getMaxLimitInThisRun() {
        return maxLimitInThisRun;
    }

    public int getCurTotalRecordsGenerated() {
        return curTotalRecordsGenerated;
    }

    public PopulationMode getPopulationMode() {
        return populationMode;
    }

    public PopulationCalcMethod getPopulationCalcMethod() {
        return popCalcMethod;
    }

    public TableRelationship getTableRelationship() {
        return relationship;
    }

    public int getInputDataListSize() {
        return inputDataList.length;
    }

    public int getChunksize() {
        return chunkSize;
    }

    ;
	public Map<String, FKeyProvider> getfKeyProviderMap() {
        return fKeyProviderMap;
    }

    public List<FKeyProvider> getfKeyProviderArray() {
        return fKeyProviderArray;
    }

    public DynamicLongArray getNewIdList() {
        return newIdList;
    }

    public DynamicStringArray getNewAlphaNumericIdList() {
        return newAlphaNumericIdList;
    }

    public String getSqlStatement() {
        return sqlStmt;
    }

    public String getLocalSequence() {
        return localSequenceName;
    }

    public boolean hasReachedMaxCountLimit() {
        return (curTotalRecordsGenerated == maxLimitInThisRun);
    }

    private boolean hasReachedChunkSize() {
        return (reachingChunkSizeCount == chunkSize);
    }

    public boolean hasExecutedQuery() {
        return hasExecutedQuery;
    }

    public boolean hasExternallySuppliedIDs() {
        return hasExternallySuppliedIDs;
    }

    public boolean hasSequenceInConfig() {
        return idMgr.hasSequenceInConfig();
    }

    public boolean hasDenorm() {
        return hasDenorm;
    }

    public DynamicLongArray getFilteredNewIdList(String filterName) {
        return filteredNewIdListMap.get(filterName);
    }

    public DynamicStringArray getFilteredNewAlphaNumericIdList(String filterName) {
        return filteredNewAlphaNumericIdListMap.get(filterName);
    }

    public RecordIdType getRecordIdType() {
        return recordIdType;
    }

    public List<String> getColumnsPositionList() {
        return columnsPositionList;
    }

    public Map<String, Integer> getColumnsPositionMap() {
        return columnsPositionMap;
    }

    public boolean hasReachedRestriction() {
        return (restrictedVersion && curTotalRecordsGenerated == StringUtil.MAX_NEW_RECORDS);
    }
}
