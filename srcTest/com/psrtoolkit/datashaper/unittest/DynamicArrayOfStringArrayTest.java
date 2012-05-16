/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.structure.DynamicArrayOfStringArray;

/**
 * @author Wan
 *
 */
public class DynamicArrayOfStringArrayTest {

    @Test
    public void testIntialization() {
        DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray();
        assertTrue(arr.length() == 100);
        assertTrue(arr.lastpos() == 0);

        DynamicArrayOfStringArray arr2 = new DynamicArrayOfStringArray(200);
        assertTrue(arr2.length() == 200);
        String[] strArr = {"777L", "888L", "999L"};
        arr2.add(strArr);
        assertTrue(arr2.lastpos() == 1);
        String[] ss = arr2.get(0);
        assertTrue(ss.length == 3);
        assertTrue(ss[0].equals("777L"));
        assertTrue(ss[1].equals("888L"));
        assertTrue(ss[2].equals("999L"));
    }

    @Test
    public void testIntializationNegative() {
        try {
            DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray(-300);
            assertTrue(false); //don't expect to arrive here
            assertTrue(arr.length() > 0);
        } catch (DataShaperException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testGet() {
        DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray(200);
        for (int i = 0; i < arr.length(); i++) {
            String[] s = new String[1];
            s[0] = Integer.toString(i + 399);
            arr.add(s);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        for (int k = 0; k < arr.length(); k++) {
            assertTrue(arr.get(k)[0].equals(Integer.toString(k + 399)));
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);
    }

    @Test
    public void testGetNegative() {
        DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray(200);
        for (int i = 0; i < arr.length(); i++) {
            String[] s = new String[1];
            s[0] = Integer.toString(i + 399);
            arr.add(s);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        try {
            arr.get(201);
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assertTrue(true);
        }

        try {
            arr.get(-888);
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assertTrue(true);
        }

    }

    @Test
    public void testSet() {
        DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray(200);
        for (int i = 0; i < arr.length(); i++) {
            String[] s = new String[1];
            s[0] = Integer.toString(i + 399);
            arr.add(s);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);
        int len = arr.length();
        for (int k = 0; k < 100; k++) {
            String[] s = new String[1];
            s[0] = Integer.toString(k + 399);
            arr.set(len + k, s);
        }

        //System.out.println("array length is: " + arr.length());
        //System.out.println("array last indexed position is: " + arr.lastpos());

        assertTrue(arr.length() == 300); //proved that the array grows
        assertTrue(arr.lastpos() == 300);
    }

    @Test
    public void testSetPositive2() {
        DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray(200);
        for (int i = 0; i < arr.length(); i++) {
            String[] s = new String[1];
            s[0] = Integer.toString(i + 399);
            arr.set(i, s);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        int len = arr.length();
        String[] s2 = new String[1];
        s2[0] = Integer.toString(99999);
        arr.set(len + 400, s2);

        //System.out.println("array length is: " + arr.length());
        //System.out.println("array last indexed position is: " + arr.lastpos());

        assertTrue(arr.length() == 700); //proved that the array grows
        assertTrue(arr.lastpos() == 601);
    }

    @Test
    public void testSetNegative() {
        DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray(200);

        try {
            String[] s = new String[1];
            s[0] = Integer.toString(99999);
            arr.set(-777, s);
            assert (false); //don't expect to arrive here
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assert (true);
        }
    }

    @Test
    public void testAdd() {
        DynamicArrayOfStringArray arr = new DynamicArrayOfStringArray(200);
        for (int i = 0; i < arr.length(); i++) {
            String[] s = new String[1];
            s[0] = Integer.toString(i + 399);
            arr.add(s);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        for (int k = 0; k < 100; k++) {
            String[] s = new String[1];
            s[0] = Integer.toString(k + 399);
            arr.add(s);
        }
        assertTrue(arr.length() == 300); //proved that the array grows
        assertTrue(arr.lastpos() == 300);
    }
}
