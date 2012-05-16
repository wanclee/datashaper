/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
package com.psrtoolkit.datashaper.structure;

import java.sql.Clob;
import java.util.Arrays;

import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * Dynamic array of java.sql.Clob. <p> The data structure should be used to
 * store java.sql.Clob object instances
 *
 * @author Wan
 *
 */
public class DynamicClobArray {

    Clob[] clobArr = null;
    int defaultSize = 100;
    //marker for the last indexed cell in the array;
    //it is not the length of the array
    int lastPos = 0;

    public DynamicClobArray() {
        clobArr = new Clob[defaultSize];
    }

    /**
     * @param size the size of array to be used in the array initialization; if
     * size < defaultSize (which is 100), the default size will be used instead
     * @exception throws DataShaperException runtime exception;
     */
    public DynamicClobArray(int size) {
        //TODO: handle size > Integer.MAX_VALUE scenario

        if (size <= 0) {
            throw new DataShaperException("Error: dynamic Clob array cannot be"
                    + " initialized with zeor or negative size value)");
        }

        if (size < defaultSize) {
            clobArr = new Clob[defaultSize];
        } else {
            clobArr = new Clob[size];
        }
        lastPos = 0;
    }

    /**
     * Get the element at the indexed position in the array
     *
     * @param position the zero-based integer value
     * @return the clob object instance at the indexed position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public Clob get(int position) {
        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic Clob array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }
        if (position >= clobArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index (").append(position).append(")").append(" is outside the dynamic Clob array boundary (").append(clobArr.length).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        return clobArr[position];
    }

    /**
     * Set into the array the value at the indexed position If the array is too
     * small, grow the array to the position value + default size and move the
     * last indexed position marker down
     *
     * @param position the zero-based integer value
     * @param value the clob object instance to be placed in the indexed
     * position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public void set(int position, Clob value) {

        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic Clob array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        if (lastPos > clobArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(clobArr.length).append(")");
            throw new DataShaperException(message.toString());
        }

        if (position >= clobArr.length) {
            Clob[] temp = new Clob[position + defaultSize];
            System.arraycopy(clobArr, 0, temp, 0, clobArr.length);
            Arrays.fill(clobArr, null); //set all to null since temp is now holding all the references
            clobArr = temp;
            lastPos = position + 1;
        } else {
            if (position >= lastPos) {
                lastPos = position + 1;
            }
        }
        clobArr[position] = value;
    }

    /**
     * Add the value to the last unoccupied cell of the array. If the array is
     * too small, grow the array by the default size and move the last indexed
     * position marker down
     *
     * @param value the clob object instance to be added to the last unoccupied
     * array cell
     * @exception throws DataShaperException runtime exception;
     */
    public void add(Clob value) {
        if (lastPos > clobArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(clobArr.length).append(")");
        }

        if (lastPos == clobArr.length) {
            Clob[] temp = new Clob[clobArr.length + defaultSize];
            System.arraycopy(clobArr, 0, temp, 0, clobArr.length);
            Arrays.fill(clobArr, null); //set all to null since temp is now holding all the references
            clobArr = temp;
        }
        clobArr[lastPos] = value;
        lastPos++; //move the last indexed position marker down
    }

    /**
     * Return the current physical length of the array
     *
     * @return the length of the array
     */
    public int length() {
        return clobArr.length;
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
        Arrays.fill(clobArr, null);
        lastPos = 0;
    }
    //public void setlastpos(int lastpos) { this.lastPos = lastpos;}
}
