/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.config;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.enumeration.FKeyAccessMethod;
import com.psrtoolkit.datashaper.enumeration.FKeyProviderType;
import com.psrtoolkit.datashaper.enumeration.PopulationCalcMethod;
import com.psrtoolkit.datashaper.enumeration.PopulationMode;
import com.psrtoolkit.datashaper.enumeration.RecordIdType;
import com.psrtoolkit.datashaper.enumeration.TableRelationship;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;
import com.psrtoolkit.datashaper.table.DataTable;
import com.psrtoolkit.datashaper.table.FKeyProvider;
import com.psrtoolkit.datashaper.util.DoubleUtil;
import com.psrtoolkit.datashaper.util.IntegerUtil;
import com.psrtoolkit.datashaper.util.LongUtil;
import com.psrtoolkit.datashaper.util.StringUtil;

/**
 * @author Wan
 *
 */
public class ConfigHandler extends DefaultHandler {

    StringBuilder tempStr = new StringBuilder(StringUtil.BUFFER_SIZE_512);
    DataTable tempTablePopul = null;
    FKeyProvider tempFKeyProvider = null;
    DataTable tempTable = null;
    List<String[]> tempInputDataListOfStringArr = new ArrayList<String[]>();
    DynamicLongArray tempAssignedIdList = null;
    StringBuilder tempAttrStr = new StringBuilder(StringUtil.BUFFER_SIZE_100);

    public ConfigHandler() {
    }

    @Override
    public void startDocument() {
        //System.out.println("Called startDocument()");
    }

    @Override
    public void endDocument() throws SAXException {
        //System.out.println("Called endDocument()");
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        /*
         * System.out.println("Called startElement; " + "uri: " + uri + ";
         * localName: " + localName + "; qName: " + qName + "; attributes: " +
         * attributes);
         */
        if (localName.equals("datashape")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("database")) {
            String dbtype = attributes.getValue("type");
            if (dbtype.equals(DbType.ORCL.toString())) {
                ConfigReader.setDbType(DbType.ORCL);
            } else if (dbtype.equals(DbType.DB2.toString())) {
                ConfigReader.setDbType(DbType.DB2);
            } else if (dbtype.equals(DbType.MSSQLSERVER.toString())) {
                ConfigReader.setDbType(DbType.MSSQLSERVER);
            } else if (dbtype.equals(DbType.MYSQL.toString())) {
                ConfigReader.setDbType(DbType.MYSQL);
            } else if (dbtype.equals(DbType.POSTGRESQL.toString())) {
                ConfigReader.setDbType(DbType.POSTGRESQL);
            } else if (dbtype.equals(DbType.SQLITE.toString())) {
                ConfigReader.setDbType(DbType.SQLITE);
            } else if (dbtype.equals(DbType.SYBASE.toString())) {
                ConfigReader.setDbType(DbType.SYBASE);
            } else {
                StringUtil.HandleUndefinedValue(dbtype, "<database>", "<datashape>");
            }
        } else if (localName.equals("connection")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("population-spec")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("table-population")) {
            String name = attributes.getValue("name");
            tempTablePopul = ConfigReader.getTableMgr().lookup(name);
            if (tempTablePopul == null) {
                tempTablePopul = new DataTable(name);
                tempTablePopul.setDbType(ConfigReader.getDbType());
                //System.out.println("temptable is: " + tempTablePopul.getName());
                ConfigReader.getTableMgr().addTable(tempTablePopul);
            }
        } else if (localName.equals("size")) {
            String attrCreateNewStr = attributes.getValue("create-new");
            if (attrCreateNewStr.isEmpty()) {
                tempTablePopul.setPopulationMode(PopulationMode.CREATE_NEW);
            } else if (attrCreateNewStr.equals("true")) {
                tempTablePopul.setPopulationMode(PopulationMode.CREATE_NEW);
            } else if (attrCreateNewStr.equals("false")) {
                tempTablePopul.setPopulationMode(PopulationMode.USE_EXISTING);
            } else {
                StringUtil.HandleUndefinedValue(attrCreateNewStr, "create-new", "<size>",
                        "<table-population>", tempTablePopul.getName());
            }
        } else if (localName.equals("random-select")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("relationship")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("fkey-providers")) { //plural 'providers'
            //explicitly announces here "do nothing"
        } else if (localName.equals("fkey-provider")) {
            String name = attributes.getValue("name");
            //INFO: the referenced table may not have been loaded yet;
            //therefore the lookup in TableMgr may return null DataTable instance.
            //The TableMgr will resolve it eventually with buildDependencies() call.
            DataTable fkeyTable = ConfigReader.getTableMgr().lookup(name);
            //fkeyTable.setHasChildren(true);
            tempFKeyProvider = new FKeyProvider(name, fkeyTable);
            tempFKeyProvider.setType(FKeyProviderType.MAIN);
            String key = StringUtil.DS_FOREIGN_KEY_PROVIDER + "=" + name; //e.g. ${ds-fkey-provider}=USR
            tempTablePopul.addFKeyProvider(key, tempFKeyProvider);
        } else if (localName.equals("fkey-secondary-provider")) {
            String name = attributes.getValue("name");
            //INFO: the referenced table may not have been loaded yet;
            //therefore the lookup in TableMgr may return null DataTable instance.
            //The TableMgr will resolve it eventually with buildDependencies() call.
            DataTable fkeyTable = ConfigReader.getTableMgr().lookup(name);
            //fkeyTable.setHasChildren(true);
            tempFKeyProvider = new FKeyProvider(name, fkeyTable);
            tempFKeyProvider.setType(FKeyProviderType.SECONDARY);
            tempFKeyProvider.setSecondaryProviderFlag(true);
            String key = StringUtil.DS_FOREIGN_KEY_SECONDARY_PROVIDER + "=" + name; //e.g. ${ds-fkey-secondary-provider}=USR
            tempTablePopul.addFKeyProvider(key, tempFKeyProvider);
        } else if (localName.equals("picklist")) {
            String name = attributes.getValue("name");
            //INFO: the referenced table may not have been loaded yet;
            //therefore the lookup in TableMgr may return null DataTable instance.
            //The TableMgr will resolve it eventually with buildDependencies() call.
            DataTable fkeyTable = ConfigReader.getTableMgr().lookup(name);
            //fkeyTable.setHasChildren(true);
            tempFKeyProvider = new FKeyProvider(name, fkeyTable);
            tempFKeyProvider.setType(FKeyProviderType.PICKLIST);
            String key = StringUtil.DS_PICKLIST + "=" + name; //e.g. ${ds-picklist}=USERTYPE
            tempTablePopul.addFKeyProvider(key, tempFKeyProvider);
        } else if (localName.equals("percent")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("filter")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("tables-spec")) {
            //about to start processing tables-spec, reset everything
            tempTablePopul = null;
            tempFKeyProvider = null;
            tempStr.delete(0, tempStr.length());  //reset the temp
        } else if (localName.equals("shared-sequence")) {
            String name = attributes.getValue("name");
            if (name.isEmpty() == false) {
                ConfigReader.setSharedSequence(name);
            } else {
                StringUtil.HandleEmptyness("<name>", "<shared-sequence>", "<tables-spec>");
            }
        } else if (localName.equals("table")) {
            String name = attributes.getValue("name");
            tempTable = ConfigReader.getTableMgr().lookup(name);
            //TODO: check for null tempTable for elements nested inside this element
            if (tempTable == null) {
                System.out.println("Warning: table '" + name
                        + "' in <tables-spec> is not referenced "
                        + "in <population-spec> and will be ingored");
                return;
            }
            String recordIdType = attributes.getValue("record-id-type");
            if (recordIdType.isEmpty()) {
                tempTable.setRecordIdType(RecordIdType.NUMERIC);
            } else if (recordIdType.equals("numeric") == true) {
                tempTable.setRecordIdType(RecordIdType.NUMERIC);
            } else if (recordIdType.equals("alphanumeric") == true) {
                tempTable.setRecordIdType(RecordIdType.ALPHANUMERIC);
            } else {
                StringUtil.HandleUndefinedValue(recordIdType, "record-id-type",
                        "<table name=\"" + tempTable.getName() + "\">", "<tables-spec>");
            }
        } else if (localName.equals("local-sequence")) {
            if (tempTable == null) {
                return;
            }
            String name = attributes.getValue("name");
            if (name.isEmpty() == false) {
                tempTable.setLocalSequence(name);
            } else {
                StringUtil.HandleEmptyness("<name>", "<local-sequence>",
                        "<table name=\"" + tempTable.getName() + "\">", "<tables-spec>");
            }
        } else if (localName.equals("sql-statement")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("data-type")) {
            //explicitly announces here "do nothing"			
        } else if (localName.equals("input-data")) {
            tempAssignedIdList = new DynamicLongArray();
            //tempTable.setAssignedIdList(tempAssignedIdList);
        } else if (localName.equals("row")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("column-positions-in-query")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("bindvar-column-positions")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("column")) {
            String name = attributes.getValue("name");
            if (name.isEmpty() == false) {
                tempAttrStr.append(name);
            } else {
                StringUtil.HandleEmptyness("name attribute", "<column>",
                        "<table name=\"" + tempTable.getName() + "\">", "<tables-spec>");
            }
        } else {
            StringUtil.HandleUndefinedElement(localName, "<datashape>");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        /*
         * System.out.println("Called endElement; " + "uri: " + uri + "; " +
         * "localName: " + localName + "; qName: " + qName);
         */
        if (localName.equals("datashape")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("database")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("connection")) {
            ConfigReader.setConnectionStr(tempStr.toString());
        } else if (localName.equals("population-spec")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("table-population")) {
            tempTablePopul = null; //reset the temp 
        } else if (localName.equals("relationship")) {
            String rel = tempStr.toString();
            if (rel.isEmpty()) {
                tempTablePopul.setTableRelationship(TableRelationship.NONE);
            } else if (rel.equals("NONE")) {
                tempTablePopul.setTableRelationship(TableRelationship.NONE);
            } else if (rel.equals("1:1")) {
                tempTablePopul.setTableRelationship(TableRelationship.ONE2ONE);
            } else if (rel.equals("1:M")) {
                tempTablePopul.setTableRelationship(TableRelationship.ONE2MANY);
            } else if (rel.equals("M:M")) {
                tempTablePopul.setTableRelationship(TableRelationship.MANY2MANY);
            } else {
                StringUtil.HandleUndefinedValue(rel, "<relationship>",
                        "<table-population>", tempTablePopul.getName());
            }
        } else if (localName.equals("size")) {
            String sizeStr = tempStr.toString();
            if (tempFKeyProvider == null) { //make sure  'size' is not due to <fkey-providers>'s <size>
                if (sizeStr.equals("by-derivation")) {
                    tempTablePopul.setPopulationCalcMethod(PopulationCalcMethod.BY_DERIVATION);
                } else if (sizeStr.equals("by-input-data")) {
                    tempTablePopul.setPopulationCalcMethod(PopulationCalcMethod.BY_INPUT_DATA);
                } else if (sizeStr.equals("by-query")) {
                    tempTablePopul.setPopulationCalcMethod(PopulationCalcMethod.BY_QUERY);
                } else {
                    int size = IntegerUtil.parseInt(sizeStr, 1, Integer.MAX_VALUE, "<size>",
                            "<table-population>", tempTablePopul.getName());
                    if (size == 0) {
                        StringUtil.HandleInsufficientValue(sizeStr, "1", "<size>",
                                "<table-population>", tempTablePopul.getName());
                    }
                    tempTablePopul.setMaxLimitInThisRun(size);
                    tempTablePopul.setPopulationCalcMethod(PopulationCalcMethod.BY_SPECIFIED_NUMBER);
                }
            } else {
                if (sizeStr.equals("by-derivation") || sizeStr.equals("by-input-data") || sizeStr.equals("by-query")) {
                    StringUtil.HandleOnlyNumericAllowed("'<size>",
                            "<fkey-provider name=\"" + tempFKeyProvider.getName() + "\">",
                            "<table-population name=\"" + tempTablePopul.getName() + "\">");
                } else {
                    int size = IntegerUtil.parseInt(sizeStr, 1, Integer.MAX_VALUE, "'<size>",
                            "<fkey-provider name=\"" + tempFKeyProvider.getName() + "\">",
                            "<table-population =\"" + tempTablePopul.getName() + "\">");
                    if (size == 0) {
                        StringUtil.HandleInsufficientValue(sizeStr, "1", "'<size>",
                                "<fkey-provider name=\"" + tempFKeyProvider.getName() + "\">",
                                "<table-population =\"" + tempTablePopul.getName() + "\">");
                    }
                    tempFKeyProvider.setForcedPopulationSize(size);
                }

            }
        } else if (localName.equals("fkey-providers")) { //plural 'providers'
            //explicitly announces here "do nothing"
        } else if (localName.equals("fkey-provider")) {
            tempFKeyProvider = null; //reset the temp
        } else if (localName.equals("fkey-secondary-provider")) {
            tempFKeyProvider = null; //reset the temp
        } else if (localName.equals("picklist")) {
            tempFKeyProvider = null; //reset the temp
        } else if (localName.equals("filter")) {
            String filterName = tempStr.toString();
            if (filterName.isEmpty()) {
                StringUtil.HandleEmptyness("<filter>", "<fkey-provider>", "<table-population>", tempTablePopul.getName());
            }
            tempFKeyProvider.setFilter(filterName);
        } else if (localName.equals("percent")) {
            String percentStr = tempStr.toString();
            double percent = DoubleUtil.parseDouble(percentStr, 0.0000000000001, 100, "<percent>", "<fkey-provider>",
                    "<table-population>", tempTablePopul.getName());
            tempFKeyProvider.setPercentage(percent);
        } else if (localName.equals("random-select")) {
            String selectStr = tempStr.toString();
            if (selectStr.isEmpty()) {
                tempFKeyProvider.setFKeyAccessMethod(FKeyAccessMethod.SEQUENTIAL);
            } else if (selectStr.equals("false")) {
                tempFKeyProvider.setFKeyAccessMethod(FKeyAccessMethod.SEQUENTIAL);
            } else if (selectStr.equals("true")) {
                tempFKeyProvider.setFKeyAccessMethod(FKeyAccessMethod.RANDOM);
            } else {
                StringUtil.HandleUndefinedValue(selectStr, "<random-select>", "<fkey-provider>",
                        "<table-population>", tempTablePopul.getName());
            }
        } else if (localName.equals("tables-spec")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("table")) {
            tempTable = null; //reset the temp
        } else if (localName.equals("sql-statement")) {
            if (tempTable == null) {
                return;
            }
            String sqlStr = tempStr.toString();
            if (sqlStr.isEmpty()) {
                StringUtil.HandleEmptyness("<sql-statement>", "<table>", tempTable.getName());
            }
            tempTable.setSqlStmt(sqlStr);
        } else if (localName.equals("data-type")) {
            if (tempTable == null) {
                return;
            }
            String datatypeStr = tempStr.toString();
            if (datatypeStr.isEmpty()) {
                StringUtil.HandleEmptyness("<data-type>", "<table>", tempTable.getName());
            }
            String[] dataTypeList = StringUtil.split(datatypeStr, ",", "");
            tempTable.addDataTypeList(dataTypeList);
        } else if (localName.equals("input-data")) {
            if (tempTable == null) {
                return;
            }
            if (tempInputDataListOfStringArr.size() == 0) {
                StringUtil.HandleEmptyness("<input-data>", "<table>", tempTable.getName());
            }
            String[][] arrarr = (String[][]) tempInputDataListOfStringArr.toArray(
                    new String[tempInputDataListOfStringArr.size()][]);
            tempTable.addInputDataList(arrarr);
            if (arrarr != null && arrarr.length > 0) {
                String[] rowItemList = arrarr[0];
                if (rowItemList != null && rowItemList.length > 0) {
                    if (rowItemList[0].contains(StringUtil.DS_ROW_ID)) {
                        tempTable.setHasExternallySuppliedIDs(true);
                    } else {
                        tempTable.setHasExternallySuppliedIDs(false);
                        if (rowItemList[0].equals(StringUtil.DS_NEXTVAL) == false) {
                            //rely on Statement.RETURN_GENERATED_KEYS option after insertion occurred
                            tempTable.setUseReturnGeneratedKeyOption(true);
                        } else {
                            //or else will fetch new ids from the sequencer prior to inserting records
                            tempTable.setUseReturnGeneratedKeyOption(false);
                        }
                    }
                }
                //handle ${ds-denorm-val}=tableName.columnName
                String denormSpecStr = null;
                for (int i = 0; i < rowItemList.length; i++) {
                    if (rowItemList[i].contains(StringUtil.DS_DENORM_VAL)) {
                        denormSpecStr = new String(rowItemList[i]);
                        StringTokenizer tokenizer = new StringTokenizer(denormSpecStr, "=.");
                        int size = tokenizer.countTokens();
                        if (size != 3) {
                            StringUtil.handleIncompleteDefinition(denormSpecStr, "<row>", "<input-data>",
                                    "<table name=\"" + tempTable.getName() + "\">", "<table-specs>");
                        }

                        tokenizer.nextToken();//skip the first one - ${ds-denorm-val}
                        String tableName = tokenizer.nextToken().trim();
                        String columnName = tokenizer.nextToken().trim();
                        String fkeyProviderName = StringUtil.DS_FOREIGN_KEY_PROVIDER + "=" + tableName;
                        String fkeySecondaryProviderName = StringUtil.DS_FOREIGN_KEY_SECONDARY_PROVIDER + "=" + tableName;

                        //verify if ${ds-fkey-provider}=tableName for the table exists in input-data row
                        boolean bExist = false;
                        for (int j = 0; j < rowItemList.length; j++) {
                            if (rowItemList[j].equals(fkeyProviderName) || rowItemList[j].equals(fkeySecondaryProviderName)) {
                                bExist = true;
                                if (i < j) {
                                    //Note: the order of ${ds-fkey-provider} and ${ds-denorm-val} are important.
                                    //${ds-fkey-provider} must appear first because provider.getNextId() call
                                    //internally sets the access index into the data cache 
                                    //that provder.getCurrentDenormValue() will use.
                                    //Same principal applies to ${ds-fkey-secondary-provider} and ${ds-denorm-val}
                                    throw new DataShaperException("Error: the denormalized column value specification '"
                                            + denormSpecStr
                                            + "' must not appear before the foreign key provider specification '"
                                            + fkeyProviderName + "' or '" + fkeySecondaryProviderName + "'");
                                }
                            }
                        }
                        if (bExist == false) {
                            throw new DataShaperException("Error: '" + fkeyProviderName
                                    + "' or '" + fkeySecondaryProviderName + "' must also exist in the same input-data row as '"
                                    + denormSpecStr + "' in a separate field. It must also appears before '"
                                    + denormSpecStr + "' field");
                        }

                        FKeyProvider provider = tempTable.getfKeyProviderMap().get(fkeyProviderName);
                        if (provider == null) {
                            provider = tempTable.getfKeyProviderMap().get(fkeySecondaryProviderName);
                        }
                        if (provider == null) {
                            throw new DataShaperException("Error: unable to locate the foreign key provider '"
                                    + fkeyProviderName + "' that can provide the denormalized value for column '"
                                    + denormSpecStr + "' in input-data row in <table name=\"" + tempTable.getName()
                                    + "\">" + "in <tables-spec>");
                        }

                        //adding both the specification 'denormSpecStr' and the parsed token 'columnName' to the provider, who will
                        //eventually add them to the parent table referenced by the provider.
                        //note: do not call tempTable.addDenormSpecs() to add these two parameters because the provider need them
                        //to initialize the denorm support correctly. The provider will add these parameters to the table later.
                        provider.addDenormColumnSpecs(denormSpecStr, columnName);

                        //add to provider maps so it can be located quickly later in doBinding() call
                        tempTable.addFKeyProviderByDenormSpec(denormSpecStr, provider);
                    } else if (rowItemList[i].contains(StringUtil.DS_PICKLIST)) {
                        denormSpecStr = new String(rowItemList[i]);
                        StringTokenizer tokenizer = new StringTokenizer(denormSpecStr, "=.");
                        int size = tokenizer.countTokens();
                        if (size != 3) {
                            StringUtil.handleIncompleteDefinition(denormSpecStr, "<row>", "<input-data>",
                                    "<table name=\"" + tempTable.getName() + "\">", "<table-specs>");
                        }

                        tokenizer.nextToken();//skip the first one - ${ds-denorm-val}
                        String tableName = tokenizer.nextToken().trim();
                        String columnName = tokenizer.nextToken().trim();
                        String picklistName = StringUtil.DS_PICKLIST + "=" + tableName;
                        FKeyProvider provider = tempTable.getfKeyProviderMap().get(picklistName);
                        if (provider == null) {
                            throw new DataShaperException("Error: unable to locate the picklist '"
                                    + picklistName + "' that can provide the list of values for column '"
                                    + denormSpecStr + "' in input-data row in <table name=\"" + tempTable.getName()
                                    + "\">" + "in <tables-spec>");
                        }

                        //treat picklist like a denorm even though picklist doesn't require an ID column being explicitly specified as denormalized 
                        provider.addDenormColumnSpecs(denormSpecStr, columnName);

                        //add to provider maps so it can be located quickly later in doBinding() call
                        tempTable.addFKeyProviderByDenormSpec(denormSpecStr, provider);
                    }
                }
            }
            if (tempTable.hasExternallySuppliedIDs()) {
                tempTable.setNewIdList(tempAssignedIdList);
            }

            tempInputDataListOfStringArr.clear();
        } else if (localName.equals("row")) {
            if (tempTable == null) {
                return;
            }
            String rowStr = tempStr.toString();
            if (rowStr.isEmpty()) {
                StringUtil.HandleEmptyness("<row>", "<input-data>",
                        "<table>", tempTable.getName());
            }
            String[] rowItemList = StringUtil.split(rowStr, ",", "'");
            tempInputDataListOfStringArr.add(rowItemList);

            /*
             * if (rowItemList != null && rowItemList.length > 0 &&
             * rowItemList[0].equals(StringUtil.DS_NEXTVAL)) {
             * tempTable.setUseReturnGeneratedKeyOption(false); } else { //else
             * the ids will be fetched from the sequencer before insert
             * tempTable.setUseReturnGeneratedKeyOption(true); }
             */
            //special handling - the input-data row's first item contains an 
            //externally assigned row id
            if (rowItemList != null && rowItemList.length > 0
                    && rowItemList[0].contains(StringUtil.DS_ROW_ID)) {
                //rowItemList[0] can look like this: ${ds-row-id}=10002
                String[] temp = rowItemList[0].split("=");
                if (temp.length == 2) {
                    long id = LongUtil.parseLong(temp[1], 1, Long.MAX_VALUE,
                            "<row>", "<input-data", "<table>", "<tables-spec>");
                    tempAssignedIdList.add(id);
                } else if (temp.length == 1) {
                    StringUtil.HandleInvalidValue(rowItemList[0],
                            "<row>", "<input-data>", "<table>", "<tables-spec>");
                } //else; temp.length == 0 is OK
            }
        } else if (localName.equals("column-positions-in-query")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("bindvar-column-positions")) {
            //explicitly announces here "do nothing"
        } else if (localName.equals("column")) {
            if (tempTable == null) {
                return;
            }
            String bindvarColumn = tempAttrStr.toString();
            String positionStr = tempStr.toString();
            int position = IntegerUtil.parseInt(positionStr, 1, 500, "<column name=\"" + bindvarColumn + "\">",
                    "<table>", "<tables-spec>");
            tempTable.addBindVariableColumnPosition(bindvarColumn, position);
        } else {
            //StringUtil.HandleUndefinedElement(localName, "<datashape>");
        }
        tempStr.delete(0, tempStr.length());  //reset the temp
        tempAttrStr.delete(0, tempAttrStr.length());
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw new DataShaperException("Error encountered in pasering configuration xml file", e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw new DataShaperException("Fatal error encountered in pasering configuration xml file", e);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        throw new DataShaperException("Warnings encountered in pasering configuration xml file", e);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        //System.out.println("Called characters(); char[] contains: " + new String(ch, start, length));
        tempStr.append(ch, start, length);
    }
}
