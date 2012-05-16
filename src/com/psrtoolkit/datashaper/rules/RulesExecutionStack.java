/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.rules;

import java.util.Stack;

/**
 * @author Wan
 *
 */
@SuppressWarnings("serial")
public class RulesExecutionStack<E> extends Stack<E> {

    private String ruleName = "";

    public RulesExecutionStack() {
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }
}
