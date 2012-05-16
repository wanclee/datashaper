/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Unit test of DataShape configurator
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.psrtoolkit.datashaper.config.ConfigReader;
import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.table.DataTable;
import com.psrtoolkit.datashaper.table.TableMgr;

/**
 * @author Wan
 *
 */
public class ConfigReaderTest {
//	private String configFileName = "DataShapeConfig.xml";
    //private String configFileName = "DataShapeConfigExample.xml";

    /**
     * Test method for {@link com.psrtoolkit.datashaper.config.ConfigReader#read(java.lang.String)}.
     */
    @Test
    public void testReadConfigurationFile() {
        try {
            TableMgr tableMgr = new TableMgr();
            DataTable table = null;
            ConfigReader.read("samples/mysql/MYSQLExampleMany2ManyRelationship.xml", tableMgr);
            assertTrue(ConfigReader.getDbType() == DbType.MYSQL);
            assertTrue(ConfigReader.getConnectionStr().equals(
                    "jdbc:mysql://localhost:3306/actproj"));
            table = tableMgr.lookup("USR");
            assertTrue(table != null);
            table = tableMgr.lookup("LOC");
            assertTrue(table != null);
            table = tableMgr.lookup("USR_LOC");
            assertTrue(table != null);
//            table = tableMgr.lookup("INTEREST");
//            assertTrue(table != null);
//            table = tableMgr.lookup("PROFILE");
//            assertTrue(table != null);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testReadConfigurationInputStream() {
        try {
            String dataShapeDtdStr = "<?xml version='1.0' encoding='UTF-8'?> <!DOCTYPE datashape [ <!ELEMENT database ( connection ) > <!ELEMENT connection ( #PCDATA ) > <!ELEMENT datashape ( database, population-spec, tables-spec ) > <!ELEMENT population-spec ( table-population+ ) > <!ELEMENT tables-spec ( shared-sequence?, table+ ) > <!ELEMENT shared-sequence ( #PCDATA ) > <!ELEMENT table-population ( fkey-providers | relationship | size )* > <!ELEMENT fkey-providers ( fkey-provider+ ) > <!ELEMENT relationship ( #PCDATA ) > <!ELEMENT size ( #PCDATA ) > <!ELEMENT fkey-provider ( percent, random-select ) > <!ELEMENT percent ( #PCDATA ) > <!ELEMENT random-select ( #PCDATA ) > <!ELEMENT table ( local-sequence?, sql-statement, data-type, input-data ) > <!ELEMENT local-sequence ( #PCDATA ) > <!ELEMENT sql-statement ( #PCDATA ) > <!ELEMENT data-type ( #PCDATA ) > <!ELEMENT input-data ( row+ ) > <!ELEMENT row ( #PCDATA ) > <!ATTLIST database type NMTOKEN #REQUIRED > <!ATTLIST fkey-provider name NMTOKEN #REQUIRED > <!ATTLIST size create-new (true|false) \"true\" > <!ATTLIST table name NMTOKEN #REQUIRED record-id-type (numeric|alphanumeric) \"numeric\" > <!ATTLIST table-population name NMTOKEN #REQUIRED > <!ATTLIST shared-sequence name NMTOKEN #REQUIRED > <!ATTLIST local-sequence name NMTOKEN #REQUIRED > ]>";
            String dataShapeStr = "<datashape> <database type=\"ORCL\"> <connection>jdbc:oracle:thin:@localhost:1521:xe</connection> </database> <population-spec> <table-population name=\"USR\"> <size>100</size> </table-population> <table-population name=\"LOC\"> <size>100</size> </table-population> <table-population name=\"INTEREST\"> <size create-new=\"false\">by-query</size> </table-population> <table-population name=\"PROFILE\"> <size create-new=\"false\">by-input-data</size> </table-population> <table-population name=\"USR_LOC\"> <relationship>M:M</relationship> <size>by-derivation</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> <fkey-provider name=\"LOC\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> <table-population name=\"APPTCONFIRM\"> <relationship>1:M</relationship> <size>100</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> </population-spec> <tables-spec> <table name=\"USR\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR&quot; (USR_ID, EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;johndoe111@yahoo.com&apos;, &apos;John Doe ${ds-current-record-id}&apos;, &apos;user&apos;, &apos;0&apos;, &apos;A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}&apos;</row> </input-data> </table> <table name=\"LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;LOC&quot; (LOC_ID, NAME) VALUES (?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;Main Residence${ds-default-signature}&apos;</row> </input-data> </table> <table name=\"USR_LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR_LOC&quot; (USR_LOC_ID, LOC_ID, USR_ID) VALUES (?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;${ds-fkey-provider}=LOC&apos;, &apos;${ds-fkey-provider}=USR&apos;</row> </input-data> </table> </tables-spec> </datashape>";
            String dataShapeConfigurationStr = dataShapeDtdStr + dataShapeStr;
            InputStream inputStream = new ByteArrayInputStream(dataShapeConfigurationStr.getBytes("UTF-8"));
            TableMgr tableMgr = new TableMgr();
            DataTable table = null;
            ConfigReader.read(inputStream, tableMgr);
            assertTrue(ConfigReader.getDbType() == DbType.ORCL);
            assertTrue(ConfigReader.getConnectionStr().equals(
                    "jdbc:oracle:thin:@localhost:1521:xe"));
            table = tableMgr.lookup("USR");
            assertTrue(table != null);
            table = tableMgr.lookup("LOC");
            assertTrue(table != null);
            table = tableMgr.lookup("USR_LOC");
            assertTrue(table != null);
            table = tableMgr.lookup("INTEREST");
            assertTrue(table != null);
            table = tableMgr.lookup("PROFILE");
            assertTrue(table != null);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testReadConfigurationInputStreamNegativeInvalidValue() {
        try {
            String dataShapeDtdStr = "<?xml version='1.0' encoding='UTF-8'?> <!DOCTYPE datashape [ <!ELEMENT connection ( #PCDATA ) > <!ELEMENT database ( connection ) > <!ELEMENT datashape ( database, population-spec, tables-spec ) > <!ELEMENT population-spec ( table-population+ ) > <!ELEMENT tables-spec ( shared-sequence?, table+ ) > <!ELEMENT table-population ( fkey-providers | relationship | size )* > <!ELEMENT fkey-providers ( fkey-provider+ ) > <!ELEMENT relationship ( #PCDATA ) > <!ELEMENT size ( #PCDATA ) > <!ELEMENT fkey-provider ( percent, random-select ) > <!ELEMENT percent ( #PCDATA ) > <!ELEMENT random-select ( #PCDATA ) > <!ELEMENT table ( local-sequence?, sql-statement, data-type, input-data ) > <!ELEMENT sql-statement ( #PCDATA ) > <!ELEMENT data-type ( #PCDATA ) > <!ELEMENT input-data ( row+ ) > <!ELEMENT row ( #PCDATA ) > <!ATTLIST database type NMTOKEN #REQUIRED > <!ATTLIST fkey-provider name NMTOKEN #REQUIRED > <!ATTLIST size create-new (true|false) \"true\" > <!ATTLIST table name NMTOKEN #REQUIRED > <!ATTLIST table-population name NMTOKEN #REQUIRED > <!ATTLIST shared-sequence name NMTOKEN #REQUIRED > <!ATTLIST local-sequence name NMTOKEN #REQUIRED > ]>";
            String badDataShapeStr = "<datashape> <database type=\"ORCL\"> <connection>jdbc:oracle:thin:@localhost:1521:xe</connection> </database> <population-spec> <table-population name=\"USR\"> <size>100PPP</size> </table-population> <table-population name=\"LOC\"> <size>100</size> </table-population> <table-population name=\"INTEREST\"> <size create-new=\"false\">by-query</size> </table-population> <table-population name=\"PROFILE\"> <size create-new=\"false\">by-input-data</size> </table-population> <table-population name=\"USR_LOC\"> <relationship>M:M</relationship> <size>by-derivation</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> <fkey-provider name=\"LOC\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> <table-population name=\"APPTCONFIRM\"> <relationship>1:M</relationship> <size>100</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> </population-spec> <tables-spec> <table name=\"USR\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR&quot; (USR_ID, EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;johndoe111@yahoo.com&apos;, &apos;John Doe ${ds-current-record-id}&apos;, &apos;user&apos;, &apos;0&apos;, &apos;A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}&apos;</row> </input-data> </table> <table name=\"LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;LOC&quot; (LOC_ID, NAME) VALUES (?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;Main Residence${ds-default-signature}&apos;</row> </input-data> </table> <table name=\"USR_LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR_LOC&quot; (USR_LOC_ID, LOC_ID, USR_ID) VALUES (?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;${ds-fkey-provider}=LOC&apos;, &apos;${ds-fkey-provider}=USR&apos;</row> </input-data> </table> </tables-spec> </datashape>";
            String dataShapeConfigurationStr = dataShapeDtdStr + badDataShapeStr;
            InputStream inputStream = new ByteArrayInputStream(dataShapeConfigurationStr.getBytes("UTF-8"));
            TableMgr tableMgr = new TableMgr();
            ConfigReader.read(inputStream, tableMgr);
            assertTrue(false); //don't expect to arrive here
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assertTrue(ex.getCause().toString().contains("Error: invalid value '100PPP' found in '<size>' in '<table-population>' in 'USR'"));
        } catch (Exception ex) {
            assertFalse(false);
        }
    }

    @Test
    public void testReadConfigurationInputStreamNegativeUndefinedValue() {
        try {
            String dataShapeDtdStr = "<?xml version='1.0' encoding='UTF-8'?> <!DOCTYPE datashape [ <!ELEMENT connection ( #PCDATA ) > <!ELEMENT database ( connection ) > <!ELEMENT datashape ( database, population-spec, tables-spec ) > <!ELEMENT population-spec ( table-population+ ) > <!ELEMENT tables-spec ( shared-sequence?, table+ ) > <!ELEMENT table-population ( fkey-providers | relationship | size )* > <!ELEMENT fkey-providers ( fkey-provider+ ) > <!ELEMENT relationship ( #PCDATA ) > <!ELEMENT size ( #PCDATA ) > <!ELEMENT fkey-provider ( percent, random-select ) > <!ELEMENT percent ( #PCDATA ) > <!ELEMENT random-select ( #PCDATA ) > <!ELEMENT table ( local-sequence?, sql-statement, data-type, input-data ) > <!ELEMENT sql-statement ( #PCDATA ) > <!ELEMENT data-type ( #PCDATA ) > <!ELEMENT input-data ( row+ ) > <!ELEMENT row ( #PCDATA ) > <!ATTLIST database type NMTOKEN #REQUIRED > <!ATTLIST fkey-provider name NMTOKEN #REQUIRED > <!ATTLIST size create-new (true|false) \"true\" > <!ATTLIST table name NMTOKEN #REQUIRED > <!ATTLIST table-population name NMTOKEN #REQUIRED > <!ATTLIST shared-sequence name NMTOKEN #REQUIRED > <!ATTLIST local-sequence name NMTOKEN #REQUIRED > ]>";
            String badDataShapeStr = "<datashape> <database type=\"ORCL\"> <connection>jdbc:oracle:thin:@localhost:1521:xe</connection> </database> <population-spec> <table-population name=\"USR\"> <size>100</size> </table-population> <table-population name=\"LOC\"> <size>100</size> </table-population> <table-population name=\"INTEREST\"> <size create-new=\"false\">by-query</size> </table-population> <table-population name=\"PROFILE\"> <size create-new=\"false\">by-input-data</size> </table-population> <table-population name=\"USR_LOC\"> <relationship>MBB:MAA</relationship> <size>by-derivation</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> <fkey-provider name=\"LOC\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> <table-population name=\"APPTCONFIRM\"> <relationship>1:M</relationship> <size>100</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> </population-spec> <tables-spec> <table name=\"USR\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR&quot; (USR_ID, EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;johndoe111@yahoo.com&apos;, &apos;John Doe ${ds-current-record-id}&apos;, &apos;user&apos;, &apos;0&apos;, &apos;A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}&apos;</row> </input-data> </table> <table name=\"LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;LOC&quot; (LOC_ID, NAME) VALUES (?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;Main Residence${ds-default-signature}&apos;</row> </input-data> </table> <table name=\"USR_LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR_LOC&quot; (USR_LOC_ID, LOC_ID, USR_ID) VALUES (?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;${ds-fkey-provider}=LOC&apos;, &apos;${ds-fkey-provider}=USR&apos;</row> </input-data> </table> </tables-spec> </datashape>";
            String dataShapeConfigurationStr = dataShapeDtdStr + badDataShapeStr;
            InputStream inputStream = new ByteArrayInputStream(dataShapeConfigurationStr.getBytes("UTF-8"));
            TableMgr tableMgr = new TableMgr();
            ConfigReader.read(inputStream, tableMgr);
            assertTrue(false); //don't expect to arrive here
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assertTrue(ex.getCause().toString().contains("Error: undefined value 'MBB:MAA' found in '<relationship>' in '<table-population>' in 'USR_LOC'"));
        } catch (Exception ex) {
            assertFalse(false);
        }
    }

    @Test
    public void testReadConfigurationInputStreamNegativeUndefinedElementInDataShape() {
        try {
            String dataShapeDtdStr = "<?xml version='1.0' encoding='UTF-8'?> <!DOCTYPE datashape [ <!ELEMENT database ( connection ) > <!ELEMENT connection ( #PCDATA ) > <!ELEMENT datashape ( database, population-spec, tables-spec ) > <!ELEMENT population-spec ( table-population+ ) > <!ELEMENT tables-spec ( shared-sequence?, table+ ) > <!ELEMENT shared-sequence ( #PCDATA ) > <!ELEMENT table-population ( fkey-providers | relationship | size )* > <!ELEMENT fkey-providers ( fkey-provider+ ) > <!ELEMENT relationship ( #PCDATA ) > <!ELEMENT size ( #PCDATA ) > <!ELEMENT fkey-provider ( percent, random-select ) > <!ELEMENT percent ( #PCDATA ) > <!ELEMENT random-select ( #PCDATA ) > <!ELEMENT table ( local-sequence?, sql-statement, data-type, input-data ) > <!ELEMENT local-sequence ( #PCDATA ) > <!ELEMENT sql-statement ( #PCDATA ) > <!ELEMENT data-type ( #PCDATA ) > <!ELEMENT input-data ( row+ ) > <!ELEMENT row ( #PCDATA ) > <!ATTLIST database type NMTOKEN #REQUIRED > <!ATTLIST fkey-provider name NMTOKEN #REQUIRED > <!ATTLIST size create-new (true|false) \"true\" > <!ATTLIST table name NMTOKEN #REQUIRED record-id-type (numeric|alphanumeric) \"numeric\" > <!ATTLIST table-population name NMTOKEN #REQUIRED > <!ATTLIST shared-sequence name NMTOKEN #REQUIRED > <!ATTLIST local-sequence name NMTOKEN #REQUIRED > ]>";
            String badDataShapeStr = "<datashape> <database type=\"ORCL\"> <connection>jdbc:oracle:thin:@localhost:1521:xe</connection> </database> <population-spec> <table-population name=\"USR\"> <size>100</size> </table-population> <table-population name=\"LOC\"> <size>100</size> </table-population> <table-population name=\"INTEREST\"> <size create-new=\"false\">by-query</size> </table-population> <table-population name=\"PROFILE\"> <size create-new=\"false\">by-input-data</size> </table-population> <table-population name=\"USR_LOC\"> <relationship>M:M</relationship> <size>by-derivation</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> <fkey-provider name=\"LOC\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> <table-population name=\"APPTCONFIRM\"> <relationship>1:M</relationship> <size>100</size> <fkey-providers> <fkey-provider name=\"USR\"> <percent>10</percent> <random-select>false</random-select> </fkey-provider> </fkey-providers> </table-population> </population-spec> <tables-spec> <table name=\"USR\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR&quot; (USR_ID, EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String</data-type> <input-data> <row-bad-one>&apos;${ds-nextval}&apos;, &apos;johndoe111@yahoo.com&apos;, &apos;John Doe ${ds-current-record-id}&apos;, &apos;user&apos;, &apos;0&apos;, &apos;A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}&apos;</row-bad-one> </input-data> </table> <table name=\"LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;LOC&quot; (LOC_ID, NAME) VALUES (?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.lang.String</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;Main Residence${ds-default-signature}&apos;</row> </input-data> </table> <table name=\"USR_LOC\"> <sql-statement>INSERT INTO &quot;ACTPROJ&quot;.&quot;USR_LOC&quot; (USR_LOC_ID, LOC_ID, USR_ID) VALUES (?, ?, ?)</sql-statement> <data-type>java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal</data-type> <input-data> <row>&apos;${ds-nextval}&apos;, &apos;${ds-fkey-provider}=LOC&apos;, &apos;${ds-fkey-provider}=USR&apos;</row> </input-data> </table> </tables-spec> </datashape>";
            String dataShapeConfigurationStr = dataShapeDtdStr + badDataShapeStr;
            InputStream inputStream = new ByteArrayInputStream(dataShapeConfigurationStr.getBytes("UTF-8"));
            TableMgr tableMgr = new TableMgr();
            ConfigReader.read(inputStream, tableMgr);
            assertTrue(false); //don't expect to arrive here
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assertTrue(ex.getCause().toString().contains("Error: undefined element '<row-bad-one>' found in '<datashape>'"));
        } catch (Exception ex) {
            assertFalse(false);
        }
    }
}
