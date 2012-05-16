/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Unit test of customer String utility
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.psrtoolkit.datashaper.util.StringUtil;

/**
 * @author Doe
 *
 */
public class StringUtilTest {

    @Test
    public void testSplitInputData() {
        String inputData = "'20003', 'johndoe111@yahoo.com', 'John Doe', 'user', '0'";
        String[] dataArr = StringUtil.split(inputData, ",", "'");
        assertTrue(dataArr.length == 5);
        assertTrue(dataArr[0].compareTo("20003") == 0);
        assertTrue(dataArr[1].compareTo("johndoe111@yahoo.com") == 0);
        assertTrue(dataArr[2].compareTo("John Doe") == 0);
        assertTrue(dataArr[3].compareTo("user") == 0);
        assertTrue(dataArr[4].compareTo("0") == 0);
    }

    @Test
    public void testSplitInputDataDelimeterAppearsInsideEnclosurePair() {
        //note: extra comma in between two single quotes breaks the function
        String inputData = "'20003', 'johndoe111@yahoo.com', 'John,, ,Doe', 'user', '0'";
        String[] dataArr = StringUtil.split(inputData, ",", "'");
        assertTrue(dataArr.length == 5);
        assertTrue(dataArr[0].compareTo("20003") == 0);
        assertTrue(dataArr[1].compareTo("johndoe111@yahoo.com") == 0);
        assertTrue(dataArr[2].compareTo("John,, ,Doe") == 0);
        assertTrue(dataArr[3].compareTo("user") == 0);
        assertTrue(dataArr[4].compareTo("0") == 0);
    }

    @Test
    public void testSplitInputDataNoEnclosure() {
        String inputData = "20003, johndoe111@yahoo.com, John Doe, user, 0";
        String[] dataArr = StringUtil.split(inputData, ",", "");
        assertTrue(dataArr.length == 5);
        assertTrue(dataArr[0].compareTo("20003") == 0);
        assertTrue(dataArr[1].compareTo("johndoe111@yahoo.com") == 0);
        assertTrue(dataArr[2].compareTo("John Doe") == 0);
        assertTrue(dataArr[3].compareTo("user") == 0);
        assertTrue(dataArr[4].compareTo("0") == 0);
    }

    @Test
    public void testSplitwithNulls() {
        String inputData = "20003, johndoe111@yahoo.com, John Doe, user, 0";
        String[] dataArr = null;

        dataArr = StringUtil.split(null, null, null);
        assertTrue(dataArr.length == 0);

        dataArr = StringUtil.split(inputData, null, null);
        assertTrue(dataArr.length == 1);

        dataArr = StringUtil.split(inputData, "", null);
        assertTrue(dataArr.length == 1);
        assertTrue(dataArr[0].compareTo(inputData) == 0);

        dataArr = StringUtil.split(inputData, ",", null);
        assertTrue(dataArr.length == 5);
        assertTrue(dataArr[0].compareTo("20003") == 0);
        assertTrue(dataArr[1].compareTo("johndoe111@yahoo.com") == 0);
        assertTrue(dataArr[2].compareTo("John Doe") == 0);
        assertTrue(dataArr[3].compareTo("user") == 0);
        assertTrue(dataArr[4].compareTo("0") == 0);

        dataArr = StringUtil.split(inputData, ",", "");
        assertTrue(dataArr.length == 5);
        assertTrue(dataArr[0].compareTo("20003") == 0);
        assertTrue(dataArr[1].compareTo("johndoe111@yahoo.com") == 0);
        assertTrue(dataArr[2].compareTo("John Doe") == 0);
        assertTrue(dataArr[3].compareTo("user") == 0);
        assertTrue(dataArr[4].compareTo("0") == 0);

        String inputData2 = "'20003', 'johndoe111@yahoo.com', 'John Doe', 'user', '0'";
        dataArr = StringUtil.split(inputData2, ",", null);
        assertTrue(dataArr.length == 5);
        assertTrue(dataArr[0].compareTo("'20003'") == 0);
        assertTrue(dataArr[1].compareTo("'johndoe111@yahoo.com'") == 0);
        assertTrue(dataArr[2].compareTo("'John Doe'") == 0);
        assertTrue(dataArr[3].compareTo("'user'") == 0);
        assertTrue(dataArr[4].compareTo("'0'") == 0);

        dataArr = StringUtil.split(inputData2, ",", "");
        assertTrue(dataArr.length == 5);
        assertTrue(dataArr[0].compareTo("'20003'") == 0);
        assertTrue(dataArr[1].compareTo("'johndoe111@yahoo.com'") == 0);
        assertTrue(dataArr[2].compareTo("'John Doe'") == 0);
        assertTrue(dataArr[3].compareTo("'user'") == 0);
        assertTrue(dataArr[4].compareTo("'0'") == 0);
    }
}
