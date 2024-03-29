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
  USR_LOC_ID VARCHAR(15) NOT NULL 
, LOC_ID VARCHAR(15) NOT NULL 
, USR_ID VARCHAR(15) NOT NULL 
, LOC_NAME VARCHAR2(200 CHAR)
, LOC_TIMEZONE VARCHAR2(30 CHAR)
, ADDRESS_ID VARCHAR(15)
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
  USR_SN_ID VARCHAR(15) NOT NULL, 
  SN_ID VARCHAR(15) NOT NULL,
  USR_ID VARCHAR(15) NOT NULL, 
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
  USR_SR_ID VARCHAR(15) NOT NULL, 
  SR_ID VARCHAR(15) NOT NULL,
  USR_ID VARCHAR(15) NOT NULL, 
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
  USR_OPPTY_ID VARCHAR(15) NOT NULL, 
  OPPTY_ID VARCHAR(15) NOT NULL,
  USR_ID VARCHAR(15) NOT NULL, 
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
  USR_ID VARCHAR(15) NOT NULL 
, DESCRIPTION VARCHAR2(256 CHAR) 
, EMAIL VARCHAR2(150 CHAR) NOT NULL 
, BASE_PROFILE VARCHAR2(4000 CHAR) 
, NAME VARCHAR2(100 CHAR) NOT NULL 
, USR_TYPE VARCHAR2(30 CHAR) NOT NULL 
, SYS_TENANT_ID VARCHAR(15) NOT NULL 
, AGE NUMBER(3,0)
, TIMEZONE VARCHAR2(30 CHAR)
, TERRITORY VARCHAR2(100 CHAR)
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
  LOC_ID VARCHAR(15) NOT NULL 
, NAME VARCHAR2(200 CHAR) NOT NULL 
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
  SN_ID VARCHAR(15) NOT NULL 
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
  SR_ID VARCHAR(15) NOT NULL 
, NAME VARCHAR2(200 CHAR) NOT NULL 
, SOURCE VARCHAR2(30 CHAR) 
, CUSTOMER_ID VARCHAR(15) NOT NULL 
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
  OPPTY_ID VARCHAR(15) NOT NULL 
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
  ADDRESS_ID VARCHAR(15) NOT NULL 
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
  USRTYPE_ID VARCHAR(15) NOT NULL 
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
  ACTIVITY_ID VARCHAR(15) NOT NULL 
, USR_ID VARCHAR(15) NOT NULL
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
  USR_ID VARCHAR(15) NOT NULL 
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

COMMENT ON COLUMN ACTPROJ.USR.USR_TYPE IS 'The type of user entity; e.g. employee, customer, partner, user, group, organization, company, etc';

COMMENT ON COLUMN ACTPROJ.USR.SYS_TENANT_ID IS 'The ID that identifies the tenancy of the record entity in the system';

COMMENT ON COLUMN ACTPROJ.LOC.NAME IS 'The name of the location;  physical site name such as Sunnyvale, and  virtual site such as URL';

CREATE SEQUENCE ACTPROJ.ACTPROJ INCREMENT BY 1 START WITH 20000 MAXVALUE 99999999999 MINVALUE 0;

CREATE OR REPLACE TRIGGER trigger_usr_loc 
BEFORE INSERT ON ACTPROJ.USR_LOC 
FOR EACH ROW
WHEN (new.USR_LOC_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.USR_LOC_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_usr_sn
BEFORE INSERT ON ACTPROJ.USR_SN
FOR EACH ROW
WHEN (new.USR_SN_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.USR_SN_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_usr_sr
BEFORE INSERT ON ACTPROJ.USR_SR
FOR EACH ROW
WHEN (new.USR_SR_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.USR_SR_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_usr_oppty
BEFORE INSERT ON ACTPROJ.USR_OPPTY
FOR EACH ROW
WHEN (new.USR_OPPTY_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.USR_OPPTY_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_usr 
BEFORE INSERT ON ACTPROJ.USR 
FOR EACH ROW
WHEN (new.USR_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.USR_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_loc 
BEFORE INSERT ON ACTPROJ.LOC 
FOR EACH ROW
WHEN (new.LOC_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.LOC_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_sn
BEFORE INSERT ON ACTPROJ.SN
FOR EACH ROW
WHEN (new.SN_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.SN_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_sr
BEFORE INSERT ON ACTPROJ.SR
FOR EACH ROW
WHEN (new.SR_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.SR_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_oppty
BEFORE INSERT ON ACTPROJ.OPPTY
FOR EACH ROW
WHEN (new.OPPTY_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.OPPTY_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_address
BEFORE INSERT ON ACTPROJ.ADDRESS
FOR EACH ROW
WHEN (new.ADDRESS_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.ADDRESS_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_usertype
BEFORE INSERT ON ACTPROJ.USRTYPE
FOR EACH ROW
WHEN (new.USRTYPE_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.USRTYPE_ID
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER trigger_activity
BEFORE INSERT ON ACTPROJ.ACTIVITY
FOR EACH ROW
WHEN (new.ACTIVITY_ID IS NULL)
BEGIN
  SELECT CONCAT('DS-', TO_CHAR(ACTPROJ.NEXTVAL))
  INTO   :new.ACTIVITY_ID
  FROM   dual;
END;
/


