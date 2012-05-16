/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Unchecked runtime exception implementation class;
 */
package com.psrtoolkit.datashaper.exception;

/**
 * Unchecked runtime exception implementation class; Used as a wrapper for both
 * checked and unchecked exceptions encountered during the DataShaper runs. <p>
 * Note: choosing unchecked exception approach is to keep API definition and its
 * usage in the client code simple.
 *
 * @author Wan
 *
 */
@SuppressWarnings("serial")
public class DataShaperException extends RuntimeException {

    public DataShaperException() {
    }

    public DataShaperException(String message) {
        super(message);
    }

    public DataShaperException(String message, Exception ex) {
        super(message, ex);
    }
}
