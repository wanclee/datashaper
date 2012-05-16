/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Read and process configuration file - DataShapeConfig.xml
 */
package com.psrtoolkit.datashaper.config;

import java.io.FileReader;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.table.TableMgr;

/**
 * @author Wan
 *
 */
public class ConfigReader {

    private static DbType dbType = null;
    private static String connectionStr = "";
    private static TableMgr tableMgr = null;
    private static String sharedSequenceName = "";

    private ConfigReader() {
    }

    public static void read(String configFile, TableMgr tableMgr) {
        try {
            ConfigReader.tableMgr = tableMgr;
            XMLReader reader = XMLReaderFactory.createXMLReader();
            ConfigHandler handler = new ConfigHandler();
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            InputSource inputSrc = new InputSource(new FileReader(configFile));
            inputSrc.setEncoding("UTF-8");
            reader.parse(inputSrc);
        } catch (Exception xex) {
            StringBuilder message = new StringBuilder();
            message.append("Error: unable to read configuration xml file '").append(configFile).append("'");
            throw new DataShaperException(message.toString(), xex);
        }
    }

    public static void read(InputStream inputStream, TableMgr tableMgr) {
        try {
            ConfigReader.tableMgr = tableMgr;
            XMLReader reader = XMLReaderFactory.createXMLReader();
            ConfigHandler handler = new ConfigHandler();
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            InputSource inputSrc = new InputSource(inputStream);
            inputSrc.setEncoding("UTF-8");
            reader.parse(inputSrc);
        } catch (Exception xex) {
            StringBuilder message = new StringBuilder();
            message.append("Error: unable to read configuration xml input stream");
            throw new DataShaperException(message.toString(), xex);
        }
    }

    public static void setDbType(DbType dbType) {
        ConfigReader.dbType = dbType;
    }

    public static void setConnectionStr(String connectionStr) {
        ConfigReader.connectionStr = connectionStr;
    }

    public static void setSharedSequence(String sequenceName) {
        sharedSequenceName = sequenceName;
    }

    public static DbType getDbType() {
        return dbType;
    }

    public static String getConnectionStr() {
        return connectionStr;
    }

    public static TableMgr getTableMgr() {
        return tableMgr;
    }

    public static String getSharedSequence() {
        return sharedSequenceName;
    }
}
