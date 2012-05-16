Command Line DataShaper v1.0.0 - 

Volume data population based on data shape definition and db-agnostic command-line runtime engine

Copyright (C) 2010-2012 Wan Lee, wan5332@gmail.com. All rights reserved.


- Before you start using the software, please read and agree to the licensing terms and conditions stated in license.txt file.

- Download the jdbc driver from the db vendor's site and put it in /drivers folder (the build file requires /drivers to be on the classpath). The User Guide has the list of driver names.

- To build datashaper.jar, type 'ant' on the command line (note: you will need ojdbc5.jar or ojdbc6.jar in order to compile Oracle-specific integration code)

- To package the compiled binaries for distribution (as a .zip file), type 'ant zip4download'

- How to get started?

    -- Take a quick look at product overview in DataShaper Introduction.pdf
    
    -- Read Datashaper User Guide.pdf
    
    -- The /samples directory has many sample XML configuration files and their database schema DDL files. 
       Select the vendor-name sub-directory and read HowToStart.txt file.


- To run the program with the given samples on the command line (say for example you're using mysql):

    java -classpath datashaper.jar:drivers/mysql-connector-java-5.1.13-bin.jar com.psrtoolkit.datashaper.DataShaper -c samples/mysql/MYSQLExampleSimpleCreateCustomers.xml -u actproj -p actproj

