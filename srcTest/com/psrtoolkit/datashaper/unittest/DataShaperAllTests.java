/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
package com.psrtoolkit.datashaper.unittest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DataShapeAgentORCLTest.class,
    DataShapeAgentDB2Test.class,
    DataShapeAgentMYSQLTest.class,
    DataShapeAgentPostgreSQLTest.class,
    DataShapeAgentMSSQLServerTest.class,
    DataShapeAgentFactoryTest.class,
    IdMgrORCLTest.class,
    StringUtilTest.class,
    DataTableTest.class,
    TableMgrTest.class,
    DynamicLongArrayTest.class,
    DynamicIntArrayTest.class,
    ConfigReaderTest.class})
public class DataShaperAllTests {
}
