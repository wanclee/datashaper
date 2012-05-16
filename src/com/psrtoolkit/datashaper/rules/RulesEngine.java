/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.rules;

import java.util.Map;

import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.util.StringUtil;

/**
 * Simple rules engine for parsing and executing rules specified in
 * <filter></filter> of xml configuration file. <p> Note: there are many
 * proprietary and Open Source commercial Rules engines, but there are either
 * too heavy or clumsy to be integrated into DataShaper.
 *
 * @author Wan
 *
 */
public class RulesEngine {

    /**
     * Simple parser for parsing rules specified in <filter></filter> of data
     * shape definition configuraiton xml file. The rules are tokenized and
     * pushed onto a stack for later execution <p> Support simple assignment and
     * comparative operators such as = <, > Support simple logical AND and OR
     * operation Support parenthesis ( and ) and maintain the order of
     * precedence for rules <p> e.g. AGE > 18 AND AGE < 55 e.g. ((AGE > 18) AND
     * (AGE < 55)) OR (AGE = 60) e.g. (USR_TYPE='user' AND (AGE > 18) AND (AGE <
     * 55)) OR ((AGE = 60) AND USR_TYPE='bogus'); Note: make sure to escape the
     * special characters in the xml configuration file; e.g. < is &gt; and
     * single quote is &apos;
     *

     *
     * @param filter the rule for filtering records
     * @return RulesExecutionStack an instance of Stack containing tokens from
     * the filter
     */
    public static RulesExecutionStack<String> parseRules(String filter) {
        RulesExecutionStack<String> stack = new RulesExecutionStack<String>();
        RulesExecutionStack<String> stack2 = new RulesExecutionStack<String>();

        char[] ch = filter.toCharArray();
        int indx = 0;
        String temp = null;
        String temp2 = null;
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] == '<' || ch[i] == '>' || ch[i] == '(' || ch[i] == ')' || ch[i] == '=') {
                temp = filter.substring(indx, i);
                if (temp.isEmpty() == false) {
                    stack.push(temp.trim());
                }
                stack.push(String.valueOf(ch[i]));
                indx = i + 1;
            } else if (ch[i] == '\'') {
                //traversing from the first single quote to the matching pair
                indx = indx + 1;
                int j = i + 1;
                for (; j < ch.length; j++) {
                    if (ch[j] != '\'') {
                        continue;
                    }
                    temp = filter.substring(indx, j);
                    indx = j + 1;
                    if (temp.isEmpty() == false) {
                        stack.push(temp);
                    }
                    break;
                }
                i = j;
            } else if (i == (ch.length - 1)) {
                //reaching the end, push remaining characters onto the stack
                stack.push(filter.substring(indx, ch.length).trim());
            } else if ((i - indx) >= 3) {
                temp = filter.substring(indx, i);
                //handle logical OR
                if (temp.contains(" OR") == true || temp.contains(" or") == true) {
                    temp2 = filter.substring(indx, i - 3);
                    if (temp2.isEmpty() == false) {
                        stack.push(temp2.trim());
                    }
                    stack.push("OR");
                    indx = i + 1;
                } //handle logical AND
                else if (temp.contains(" AND") == true || temp.contains(" and") == true) {
                    temp2 = filter.substring(indx, i - 4);
                    if (temp2.isEmpty() == false) {
                        stack.push(temp2.trim());
                    }
                    stack.push("AND");
                    indx = i + 1;
                }
            }
        }

        //RulesExecutionStack<String> stack3 = new RulesExecutionStack<String>();
        String ss = null;
        while (stack.isEmpty() == false) {
            ss = stack.pop();
            stack2.push(ss);
            //stack3.push(ss);
        }
        /*
         * System.out.println("filter is: " + filter); while (stack3.isEmpty()
         * == false) { System.out.println("token is: " + stack3.pop()); }
		System.out.println("************************");
         */
        stack2.setRuleName(filter);
        return stack2;
    }

    /**
     * Execute the rules that have been tokenized and pushed onto the stack <p>
     * The execution of rules is using two stacks. One of them is cloned from
     * the original stack passed in as an input argument Intermediate states are
     * kept on the second stack and pushed back to first stack at the end of
     * first stack's execution cycle
     *
     * @param stack an instance of RulesExecutionStack that contains the rule
     * tokens
     * @param filterColumnIndexesMap a lookup map to get the actual index
     * position for the filter column
     * @param inputData a String[] that contains the substituted data
     * @return true if the rules are satisfied; otherwise false
     * @throws DataShaperException
     */
    public static boolean rulesSatisfied(RulesExecutionStack<String> stack,
            Map<String, Integer> filterColumnIndexesMap,
            String[] inputData) {

        String s = null;
        String s2 = null;
        String s3 = null;
        String operandRight = null;
        String operandLeft = null;
        //String operator = null;
        boolean result = false;
        int runaway = 0;

        @SuppressWarnings("unchecked")
        RulesExecutionStack<String> stackTemp = (RulesExecutionStack<String>) stack.clone();
        RulesExecutionStack<String> stackTemp2 = new RulesExecutionStack<String>();
        //RulesExecutionStack<String> stackTemp3 = new RulesExecutionStack<String>();
        int size = stackTemp.size();
        while (size > 1) {
            while (stackTemp.isEmpty() == false) {
                //safeguard against infinity loop in case there is a bug in rules execution
                if (runaway == StringUtil.MAX_RULES_EXECUTION_RUNAWAY_LIMIT) {
                    throw new DataShaperException("Error: rules execution never seems to stop. Abort!\n"
                            + "Check rule: " + stack.getRuleName());
                }
                runaway++;

                s = stackTemp.peek();
                if (s.equals("(") == true) {
                    if (operandLeft != null) {
                        stackTemp2.push(operandLeft);
                        operandLeft = null;
                    }
                    s = stackTemp.pop();
                    s2 = stackTemp.pop();
                    s3 = stackTemp.pop();
                    //check if the state is ([true]) or ([false])
                    if ((s2.equals("[true]") || s2.equals("[false]")) && s3.equals(")")) {
                        stackTemp2.push(s2);
                    } else {
                        stackTemp.push(s3); //put back to first stack so it can be evaluated again
                        stackTemp.push(s2);
                        stackTemp2.push(s); //put the parenthesis ( in second stack and revisit later
                    }
                } else if (s.equals(")") == true) {
                    if (operandLeft != null) {
                        stackTemp2.push(operandLeft);
                        operandLeft = null;
                    }
                    s = stackTemp.pop();
                    stackTemp2.push(s);

                } else if (s.equals("AND") == true) {
                    //handle logical AND operation
                    s = stackTemp.pop();
                    s2 = stackTemp.pop();
                    //check if the state has arrived as in one of the four below:
                    //[true] AND [true]
                    //[true] AND [false]
                    //[false] AND [true]
                    //[false] AND [false]
                    if (operandLeft != null && (operandLeft.equals("[true]") || operandLeft.equals("[false]"))
                            && (s2.equals("[true]") || s2.equals("[false]"))) {
                        if (operandLeft.equals("[false]") && s2.equals("[false]")) {
                            result = false; //result of two false(s) in AND operation is still false
                        } else {
                            result = operandLeft.equals(s2);
                        }
                        stackTemp2.push(result ? "[true]" : "[false]");
                        operandLeft = null;
                    } else {
                        if (operandLeft != null) {
                            stackTemp2.push(operandLeft);
                            operandLeft = null;
                        }
                        stackTemp2.push(s); //push the AND operator to second stack and revisit later
                        if (stackTemp.size() > 1) {
                            stackTemp.push(s2); //put back to first stack so it can be evaluated again
                        } else {
                            stackTemp2.push(s2); //it is last item left in the first stack; push to second stack and revisit later
                        }
                    }
                } else if (s.equals("OR") == true) {
                    //handle logical OR operation
                    s = stackTemp.pop();
                    s2 = stackTemp.pop();
                    if (operandLeft != null && (operandLeft.equals("[true]") || operandLeft.equals("[false]"))
                            && (s2.equals("[true]") || s2.equals("[false]"))) {
                        result = (operandLeft.equals("[true]") || s2.equals("[true]"));
                        stackTemp2.push(result ? "[true]" : "[false]");
                        operandLeft = null;
                    } else {
                        if (operandLeft != null) {
                            stackTemp2.push(operandLeft);
                            operandLeft = null;
                        }
                        stackTemp2.push(s);
                        if (stackTemp.size() > 1) {
                            stackTemp.push(s2);
                        } else {
                            stackTemp2.push(s2);
                        }
                    }
                } else if (s.equals("=") == true) {
                    //handle assignment operation
                    stackTemp.pop();
                    operandRight = stackTemp.pop();
                    Integer pos = filterColumnIndexesMap.get(operandLeft);
                    if (pos == null) {
                        throw new DataShaperException("Error: the filter column name is not valid - "
                                + operandLeft + "\n" + "Check rule: " + stack.getRuleName());
                    }
                    String value = inputData[pos.intValue() - 1];
                    /*
                     * System.out.println("rule name: " + stack.getRuleName());
                     * System.out.println("operandLeft: " + operandLeft);
                     * System.out.println("pos: " + pos.intValue());
                     * System.out.println("operandRight: " + operandRight);
                     */
                    if (value == null || value.isEmpty()) {
                        result = false;
                    } else {
                        result = value.equals(operandRight);
                    }
                    stackTemp2.push(result ? "[true]" : "[false]");
                    operandLeft = null;
                } else if (s.equals(">") == true) {
                    //handle comparison (greater than) operation
                    stackTemp.pop();
                    operandRight = stackTemp.pop();
                    Integer pos = filterColumnIndexesMap.get(operandLeft);
                    if (pos == null) {
                        if (pos == null) {
                            throw new DataShaperException("Error: the filter column name is not valid - "
                                    + operandLeft + "\n" + "Check rule: " + stack.getRuleName());
                        }
                    }
                    String value = inputData[pos.intValue() - 1];
                    if (value == null || value.isEmpty()) {
                        result = false;
                    } else {
                        result = Long.parseLong(value) > Long.parseLong(operandRight);
                    }
                    stackTemp2.push(result ? "[true]" : "[false]");
                    operandLeft = null;
                } else if (s.equals("<") == true) {
                    //handle comparison (smaller than) operation
                    stackTemp.pop();
                    operandRight = stackTemp.pop();
                    Integer pos = filterColumnIndexesMap.get(operandLeft);
                    if (pos == null) {
                        if (pos == null) {
                            throw new DataShaperException("Error: the filter column name is not valid - "
                                    + operandLeft + "\n" + "Check rule: " + stack.getRuleName());
                        }
                    }
                    String value = inputData[pos.intValue() - 1];
                    if (value == null || value.isEmpty()) {
                        result = false;
                    } else {
                        result = Long.parseLong(value) < Long.parseLong(operandRight);
                    }
                    stackTemp2.push(result ? "[true]" : "[false]");
                    operandLeft = null;
                } else {
                    //catch all for none of the above
                    operandLeft = stackTemp.pop();
                }
            }
            size = stackTemp.size();
            String ss = null;
            //pushing intermediate states back to the first stack
            while (stackTemp2.isEmpty() == false) {
                ss = stackTemp2.pop();
                stackTemp.push(ss);
                //stackTemp3.push(ss);
            }
            /*
             * while (stackTemp3.isEmpty() == false) { System.out.println("token
             * is: " + stackTemp3.pop()); }
			System.out.println("************************");
             */
            size = stackTemp.size();
        }

        // The stack size must be one; the stack either contains [true] or [false] as final state
        if (stackTemp.size() != 1) {
            throw new DataShaperException("Error: rules execution failed\n"
                    + "Check rule: " + stack.getRuleName());
        }
        String finalResult = stackTemp.pop();

        //remove all references to String objects
        stackTemp.clear();
        stackTemp2.clear();

        return finalResult.equals("[true]");
    }
}
