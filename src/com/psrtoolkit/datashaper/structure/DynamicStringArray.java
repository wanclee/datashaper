/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.structure;

import java.util.Arrays;

import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * Dynamic array of String. <p> The data structure should be used to store
 * alphanumeric values
 *
 * @author Wan
 *
 */
public class DynamicStringArray {

    String[] stringArr = null;
    int defaultSize = 100;
    //marker for the last indexed cell in the array;
    //it is not the length of the array
    int lastPos = 0;

    public DynamicStringArray() {
        stringArr = new String[defaultSize];
    }

    /**
     * @param size the size of array to be used in the array initialization; if
     * size < defaultSize (which is 100), the default size will be used instead
     * @exception throws DataShaperException runtime exception;
     */
    public DynamicStringArray(int size) {
        //TODO: handle size > Integer.MAX_VALUE scenario

        if (size <= 0) {
            throw new DataShaperException("Error: dynamic String array cannot be"
                    + " initialized with zeor or negative size value)");
        }

        if (size < defaultSize) {
            stringArr = new String[defaultSize];
        } else {
            stringArr = new String[size];
        }
        lastPos = 0;
    }

    /**
     * Get the element at the indexed position in the array
     *
     * @param position the zero-based integer value
     * @return the string at the indexed position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public String get(int position) {
        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic String array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }
        if (position >= stringArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index (").append(position).append(")").append(" is outside the dynamic String array boundary (").append(stringArr.length).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        return stringArr[position];
    }

    /**
     * Set into the array the value at the indexed position If the array is too
     * small, grow the array to the position value + default size and move the
     * last indexed position marker down
     *
     * @param position the zero-based integer value
     * @param value the string to be placed in the indexed position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public void set(int position, String value) {

        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic String array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        if (lastPos > stringArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(stringArr.length).append(")");
            throw new DataShaperException(message.toString());
        }

        if (position >= stringArr.length) {
            String[] temp = new String[position + defaultSize];
            System.arraycopy(stringArr, 0, temp, 0, stringArr.length);
            Arrays.fill(stringArr, null); //set all to null since temp is now holding all the references
            stringArr = temp;
            lastPos = position + 1;
        } else {
            if (position >= lastPos) {
                lastPos = position + 1;
            }
        }
        stringArr[position] = value;
    }

    /**
     * Add the value to the last unoccupied cell of the array. If the array is
     * too small, grow the array by the default size and move the last indexed
     * position marker down
     *
     * @param value the string to be added to the last unoccupied array cell
     * @exception throws DataShaperException runtime exception;
     */
    public void add(String value) {
        if (lastPos > stringArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(stringArr.length).append(")");
        }

        if (lastPos == stringArr.length) {
            String[] temp = new String[stringArr.length + defaultSize];
            System.arraycopy(stringArr, 0, temp, 0, stringArr.length);
            Arrays.fill(stringArr, null); //set all to null since temp is now holding all the references
            stringArr = temp;
        }
        stringArr[lastPos] = value;
        lastPos++; //move the last indexed position marker down
    }

    /**
     * Return the current physical length of the array
     *
     * @return the length of the array
     */
    public int length() {
        return stringArr.length;
    }

    /**
     * Return the last indexed position of the array This is the logical length
     * of the array.
     *
     * @return the last indexed position of the array; indicating the first
     * unoccupied cell position.
     */
    public int lastpos() {
        return lastPos;
    }

    /**
     * Set the content of the array to 0L
     */
    public void clear() {
        Arrays.fill(stringArr, null);
        lastPos = 0;
    }
    //public void setlastpos(int lastpos) { this.lastPos = lastpos;}
}
