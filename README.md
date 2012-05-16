
Command Line DataShaper v1.0.0
Volume data population based on data shape definition and db-agnostic command-line runtime engine
Copyright (C) 2010-2012 Wan Lee, wan5332@gmail.com. All rights reserved.


- To build datashaper.jar, type 'ant' on the command line

- To package the binaries nicely for distribution (as a .zip file), type 'ant zip4download'

- Before you start using the software, please read and agree to the licensing terms and conditions stated in license.txt file.

- How to get started?
    -- Take a quick look at product overview in DataShaper Introduction.pdf
    -- Read Datashaper User Guide.pdf
    -- The /samples directory has many sample XML configuration files and their database schema DDL files. 
       Select the vendor-name sub-directory and read HowToStart.txt file.

- Download the jdbc driver from the db vendor's site and put it /drivers folder (or any location you desired). The User Guide has the list of driver names.

- To run the program with the given samples on the command line (say for example you're using mysql):
java -classpath datashaper.jar:drivers/mysql-connector-java-5.1.13-bin.jar com.psrtoolkit.datashaper.DataShaper -c samples/mysql/MYSQLExampleSimpleCreateCustomers.xml -u actproj -p actproj

