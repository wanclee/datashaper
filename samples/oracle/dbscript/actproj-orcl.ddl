-- Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com

DROP TABLE ACTPROJ.USR_LOC;
DROP TABLE ACTPROJ.USR_SN;
DROP TABLE ACTPROJ.USR_SR;
DROP TABLE ACTPROJ.USR_OPPTY;
DROP TABLE ACTPROJ.ACTIVITY;
DROP TABLE ACTPROJ.PROFILE;
DROP TABLE ACTPROJ.USR;
DROP TABLE ACTPROJ.LOC;
DROP TABLE ACTPROJ.SN;
DROP TABLE ACTPROJ.SR;
DROP TABLE ACTPROJ.OPPTY;
DROP TABLE ACTPROJ.ADDRESS;
DROP TABLE ACTPROJ.USRTYPE;
DROP SEQUENCE ACTPROJ.ACTPROJ;

CREATE TABLE ACTPROJ.USR_LOC 
(
  USR_LOC_ID NUMBER(10, 0) NOT NULL 
, CREATED TIMESTAMP
, LOC_ID NUMBER(10, 0) NOT NULL 
, USR_ID NUMBER(10, 0) NOT NULL 
, LOC_NAME VARCHAR2(200 CHAR)
, LOC_TIMEZONE VARCHAR2(30 CHAR)
, ADDRESS_ID NUMBER(10, 0)
, ADDRESS_ZIPCODE VARCHAR2(15 CHAR)
, CONSTRAINT USR_LOC_PK PRIMARY KEY 
  (
    USR_LOC_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);


CREATE TABLE ACTPROJ.USR_SN 
(
  USR_SN_ID NUMBER(10, 0) NOT NULL, 
  SN_ID NUMBER(10, 0) NOT NULL,
  USR_ID NUMBER(10, 0) NOT NULL, 
  CONSTRAINT  USR_SN_PK PRIMARY KEY
  (
    USR_SN_ID
  )
  ENABLE
)
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.USR_SR
(
  USR_SR_ID NUMBER(10, 0) NOT NULL, 
  SR_ID NUMBER(10, 0) NOT NULL,
  USR_ID NUMBER(10, 0) NOT NULL, 
  CONSTRAINT  USR_SR_PK PRIMARY KEY
  (
    USR_SR_ID
  )
  ENABLE
)
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.USR_OPPTY
(
  USR_OPPTY_ID NUMBER(10, 0) NOT NULL, 
  OPPTY_ID NUMBER(10, 0) NOT NULL,
  USR_ID NUMBER(10, 0) NOT NULL, 
  CONSTRAINT  USR_OPPTY_PK PRIMARY KEY
  (
    USR_OPPTY_ID
  )
  ENABLE
)
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.USR 
(
  USR_ID NUMBER(10, 0) NOT NULL 
, CREATED DATE
, DESCRIPTION VARCHAR2(256 CHAR) 
, EMAIL VARCHAR2(150 CHAR) NOT NULL 
, BASE_PROFILE VARCHAR2(4000 CHAR) 
, NAME VARCHAR2(100 CHAR) NOT NULL 
, USR_TYPE VARCHAR2(30 CHAR) NOT NULL 
, SYS_TENANT_ID NUMBER(10, 0) NOT NULL 
, AGE NUMBER(3,0)
, TIMEZONE VARCHAR2(30 CHAR)
, TERRITORY VARCHAR2(100 CHAR)
, SPECIAL_BLOCK CLOB
, SPECIAL_BLOCK2 BLOB
, CONSTRAINT USR_PK PRIMARY KEY 
  (
    USR_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.LOC 
(
  LOC_ID NUMBER(10, 0) NOT NULL 
, CREATED DATE
, NAME VARCHAR2(200 CHAR) NOT NULL 
, TIMEZONE VARCHAR2(30 CHAR)
, CONSTRAINT LOC_PK PRIMARY KEY 
  (
    LOC_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);


CREATE TABLE ACTPROJ.SN 
(
  SN_ID NUMBER(10, 0) NOT NULL 
, NAME VARCHAR2(200 CHAR) NOT NULL 
, TYPE VARCHAR2(30 CHAR) NOT NULL 
, CONSTRAINT SN_PK PRIMARY KEY 
  (
    SN_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.SR 
(
  SR_ID NUMBER(10, 0) NOT NULL 
, NAME VARCHAR2(200 CHAR) NOT NULL 
, SOURCE VARCHAR2(30 CHAR) 
, CUSTOMER_ID NUMBER(10, 0) NOT NULL 
, CONSTRAINT SR_PK PRIMARY KEY 
  (
    SR_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.OPPTY 
(
  OPPTY_ID NUMBER(10, 0) NOT NULL 
, NAME VARCHAR2(200 CHAR) NOT NULL 
, DEALSIZE VARCHAR2(30 CHAR) 
, TERRITORY VARCHAR2(100 CHAR) 
, CONSTRAINT OPPTY_PK PRIMARY KEY 
  (
    OPPTY_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.ADDRESS 
(
  ADDRESS_ID NUMBER(10, 0) NOT NULL 
, STREET_NAME VARCHAR2(200 CHAR)  
, STREET_NUMBER VARCHAR2(30 CHAR)
, UNIT_NUMBER VARCHAR2(30 CHAR)
, BLOCK_NUMBER VARCHAR2(30 CHAR)
, CITY VARCHAR2(100 CHAR)
, STATE VARCHAR2(30 CHAR) NOT NULL
, ZIPCODE VARCHAR2(15 CHAR) NOT NULL
, COUNTRY VARCHAR2(50 CHAR) NOT NULL
, CONSTRAINT ADDRESS_PK PRIMARY KEY 
  (
    ADDRESS_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.USRTYPE 
(
  USRTYPE_ID NUMBER(10, 0) NOT NULL 
, NAME VARCHAR2(200 CHAR)  
, CONSTRAINT USRTYPE_PK PRIMARY KEY 
  (
    USRTYPE_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

CREATE TABLE ACTPROJ.ACTIVITY
(
  ACTIVITY_ID NUMBER(10, 0) NOT NULL 
, USR_ID NUMBER(10, 0) NOT NULL
, DESCRIPTION VARCHAR2(256 CHAR) NOT NULL
, EFFECTIVE_START_DATE DATE
, EFFECTIVE_END_DATE DATE
, CONSTRAINT ACTIVITY_PK PRIMARY KEY 
  (
    ACTIVITY_ID 
  )
  ENABLE 
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

-- PROFILE table does not need its own primary column since it is an extension of USR
CREATE TABLE ACTPROJ.PROFILE
(
  USR_ID NUMBER(10, 0) NOT NULL 
, DESCRIPTION VARCHAR2(256 CHAR) NOT NULL
) 
STORAGE 
( 
  BUFFER_POOL DEFAULT 
);

ALTER TABLE ACTPROJ.USR_LOC
ADD CONSTRAINT USR_LOC_LOC_FK1 FOREIGN KEY
(
  LOC_ID 
)
REFERENCES ACTPROJ.LOC
(
  LOC_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.USR_LOC
ADD CONSTRAINT USR_LOC_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES ACTPROJ.USR
(
  USR_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.USR_SN
ADD CONSTRAINT USR_SN_SN_FK1 FOREIGN KEY
(
  SN_ID 
)
REFERENCES ACTPROJ.SN
(
  SN_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.USR_SN
ADD CONSTRAINT USR_SN_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES ACTPROJ.USR
(
  USR_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.USR_SR
ADD CONSTRAINT USR_SR_SR_FK1 FOREIGN KEY
(
  SR_ID 
)
REFERENCES ACTPROJ.SR
(
  SR_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.USR_SR
ADD CONSTRAINT USR_SR_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES ACTPROJ.USR
(
  USR_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.USR_OPPTY
ADD CONSTRAINT USR_OPPTY_OPPTY_FK1 FOREIGN KEY
(
  OPPTY_ID 
)
REFERENCES ACTPROJ.OPPTY
(
  OPPTY_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.USR_OPPTY
ADD CONSTRAINT USR_OPPTY_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES ACTPROJ.USR
(
  USR_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.ACTIVITY
ADD CONSTRAINT ACTIVITY_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES ACTPROJ.USR
(
  USR_ID 
)
ENABLE;

ALTER TABLE ACTPROJ.PROFILE
ADD CONSTRAINT PROFILE_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES ACTPROJ.USR
(
  USR_ID 
)
ENABLE;

COMMENT ON COLUMN ACTPROJ.USR_LOC.LOC_ID IS 'Reference the parent LOC entity table';

COMMENT ON COLUMN ACTPROJ.USR_LOC.USR_ID IS 'Reference the parent USR entity table';

COMMENT ON COLUMN ACTPROJ.USR.EMAIL IS 'The email address of the user entity';

COMMENT ON COLUMN ACTPROJ.USR.BASE_PROFILE IS 'The profile of the user entity; contains info such as first, mid, last name, company name, address, phones, emails, DOB, gender, interests, preferences, employment, and etc.';

COMMENT ON COLUMN ACTPROJ.USR.NAME IS 'The name of the user entity';

COMMENT ON COLUMN ACTPROJ.USR.USR_TYPE IS 'The type of user entity; e.g. employee, customer, partner, user, group, organization, company, sales-rep, service-rep etc';

COMMENT ON COLUMN ACTPROJ.USR.SYS_TENANT_ID IS 'The ID that identifies the tenancy of the record entity in the system';

COMMENT ON COLUMN ACTPROJ.LOC.NAME IS 'The name of the location;  physical site name such as Sunnyvale, and  virtual site such as URL';

CREATE SEQUENCE ACTPROJ.ACTPROJ INCREMENT BY 100 START WITH 20000 MAXVALUE 99999999999 MINVALUE 0;

-- CREATE OR REPLACE TYPE DATASHAPEIDARRAY is VARRAY(100) OF NUMBER;
-- /
-- CREATE OR REPLACE 
-- PACKAGE pkg_datashaper AS
--       FUNCTION getIdBlockBySequence(
--           sequenceName IN VARCHAR2,
--           block_size IN INTEGER,
--           increment_size IN INTEGER) RETURN DATASHAPEIDARRAY;
-- END;
-- /
-- Why use this function?
--    To allow the calling program to obtain a list of new IDs up to the specified block size and use those IDs 
--    in batch insert of new records.
-- When to use this function?
--    only use this function when sequence increment is < 100 and you have no privilege to alter it.
--    If you have the privilege, alter sequence actproj increment by 100; so there will be no need 
--    to call this function from client through jdbc
-- Input Parameters:
-- 	  squenceName: the name of sequence
--    block_size: the max size the id array will be filled up to
--    increment_size: the max increment value computed by the caller based on the difference between two nextval calls.
-- Caution: The caller must compute the increment_size correctly. The function doesn't call nextval to determine 
--          the current increment as it would be wasting IDs in the process.
-- CREATE OR REPLACE 
-- PACKAGE BODY pkg_datashaper AS
--     FUNCTION getIdBlockBySequence(
--         sequenceName IN VARCHAR2,
--         block_size IN INTEGER,
--         increment_size IN INTEGER) RETURN DATASHAPEIDARRAY
--     is
--         stmt VARCHAR2(2000);
--         id_val NUMBER;
--         id_arr DATASHAPEIDARRAY := DATASHAPEIDARRAY();
--         inc_count NUMBER := 0;
--         count NUMBER := 0;
--         num NUMBER;
--     begin
--         BEGIN
--            num := block_size;
--             -- 100 is the limit
--             IF num > 100 THEN
--               -- dbms_output.put_line('Truncating block size to 100 from ''' || block_size || '''');
--               num := 100;
--             END IF;
--             FOR count IN 1 .. num
--             LOOP
--               -- get nextval
--               stmt := 'SELECT ' || sequenceName || '.NEXTVAL FROM DUAL';
--               -- dbms_output.put_line('Executing ''' || stmt || '''');
--               EXECUTE IMMEDIATE stmt into id_val;
--               id_arr.extend;
--               id_arr(id_arr.count) := id_val;
--               -- if sequence's increment size is greater than 1, fill the array with 
--               -- values up to that size before calling nextval again.
--               FOR inc_count IN 2 .. increment_size
--               LOOP
--               	id_val := id_val + 1;
--               	id_arr.extend;
--               	id_arr(id_arr.count) := id_val;
--               END LOOP;
--             END LOOP;
--         EXCEPTION
--             WHEN OTHERS THEN dbms_output.put_line(SQLERRM);
--         END;        
--             RETURN id_arr;
--     end getIdBlockBySequence;
-- END;
-- /
--#SELECT pkg_datashaper.getIdBlockBySequence('ACTPROJ',10,5) FROM DUAL;
--#select actproj.nextval from dual;
