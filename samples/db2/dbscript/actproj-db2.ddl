# Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com

DROP TABLE USR_LOC
DROP TABLE USR_SN
DROP TABLE USR_SR
DROP TABLE USR_OPPTY
DROP TABLE ACTIVITY
DROP TABLE PROFILE
DROP TABLE USR
DROP TABLE LOC
DROP TABLE SN
DROP TABLE SR
DROP TABLE OPPTY
DROP TABLE ADDRESS
DROP TABLE USRTYPE
DROP SEQUENCE ACTPROJ

CREATE TABLE USR_LOC 
(
  USR_LOC_ID BIGINT NOT NULL, 
  LOC_ID BIGINT NOT NULL,
  USR_ID BIGINT NOT NULL, 
  LOC_NAME VARCHAR(200),
  LOC_TIMEZONE VARCHAR(30),
  ADDRESS_ID BIGINT,
  ADDRESS_ZIPCODE VARCHAR(15),
  PRIMARY KEY (USR_LOC_ID)
)

CREATE TABLE USR_SN 
(
  USR_SN_ID BIGINT NOT NULL, 
  SN_ID BIGINT NOT NULL,
  USR_ID BIGINT NOT NULL, 
  PRIMARY KEY (USR_SN_ID)
)

CREATE TABLE USR_SR 
(
  USR_SR_ID BIGINT NOT NULL, 
  SR_ID BIGINT NOT NULL,
  USR_ID BIGINT NOT NULL, 
  PRIMARY KEY (USR_SR_ID)
)

CREATE TABLE USR_OPPTY 
(
  USR_OPPTY_ID BIGINT NOT NULL, 
  USR_ID BIGINT NOT NULL,
  OPPTY_ID BIGINT NOT NULL, 
  PRIMARY KEY (USR_OPPTY_ID)
)

CREATE TABLE USR 
(
  USR_ID BIGINT NOT NULL 
, DESCRIPTION VARCHAR(256) 
, EMAIL VARCHAR(150) NOT NULL 
, BASE_PROFILE VARCHAR(400) 
, NAME VARCHAR(100) NOT NULL 
, USR_TYPE VARCHAR(30) NOT NULL 
, SYS_TENANT_ID BIGINT NOT NULL 
, AGE SMALLINT
, TIMEZONE VARCHAR(30)
, TERRITORY VARCHAR(100)
, SPECIAL_BLOCK CLOB(2000000) 
, SPECIAL_BLOCK2 BLOB(2M)
, PRIMARY KEY (USR_ID)
)

CREATE TABLE LOC 
(
  LOC_ID BIGINT NOT NULL 
, CREATED DATE
, NAME VARCHAR(200) NOT NULL 
, TIMEZONE VARCHAR(30)
, PRIMARY KEY (LOC_ID)
)

CREATE TABLE SN 
(
  SN_ID BIGINT NOT NULL 
, NAME VARCHAR(200) NOT NULL 
, TYPE VARCHAR(30) NOT NULL 
, PRIMARY KEY (SN_ID)
)

CREATE TABLE SR 
(
  SR_ID BIGINT NOT NULL 
, NAME VARCHAR(200) NOT NULL 
, SOURCE VARCHAR(30)
, CUSTOMER_ID BIGINT
, PRIMARY KEY (SR_ID)
)

CREATE TABLE OPPTY 
(
  OPPTY_ID BIGINT NOT NULL 
, NAME VARCHAR(200) NOT NULL 
, DEALSIZE VARCHAR(30) 
, TERRITORY VARCHAR(100) 
, PRIMARY KEY (OPPTY_ID)
)

CREATE TABLE ADDRESS 
(
  ADDRESS_ID BIGINT NOT NULL 
, STREET_NAME VARCHAR(200)  
, STREET_NUMBER VARCHAR(30)
, UNIT_NUMBER VARCHAR(30)
, BLOCK_NUMBER VARCHAR(30)
, CITY VARCHAR(100)
, STATE VARCHAR(30) NOT NULL
, ZIPCODE VARCHAR(15) NOT NULL
, COUNTRY VARCHAR(50) NOT NULL
, PRIMARY KEY (ADDRESS_ID)
) 

CREATE TABLE USRTYPE 
(
  USRTYPE_ID BIGINT NOT NULL  
, NAME VARCHAR(200)  
, PRIMARY KEY (USRTYPE_ID)
)

CREATE TABLE ACTIVITY
(
  ACTIVITY_ID BIGINT NOT NULL 
, USR_ID BIGINT NOT NULL
, DESCRIPTION VARCHAR(256) NOT NULL
, EFFECTIVE_START_DATE DATE
, EFFECTIVE_END_DATE DATE
, PRIMARY KEY (ACTIVITY_ID)
)

#PROFILE table does not need its own primary column since it is an extension of USR
CREATE TABLE PROFILE
(
  USR_ID BIGINT NOT NULL 
, DESCRIPTION VARCHAR(256) NOT NULL
)

ALTER TABLE USR_LOC
ADD CONSTRAINT USR_LOC_LOC_FK1 FOREIGN KEY
(
  LOC_ID 
)
REFERENCES LOC
(
  LOC_ID 
)

ALTER TABLE USR_LOC
ADD CONSTRAINT USR_LOC_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES USR
(
  USR_ID 
)

ALTER TABLE USR_SN
ADD CONSTRAINT USR_SN_SN_FK1 FOREIGN KEY
(
  SN_ID 
)
REFERENCES SN
(
  SN_ID 
)

ALTER TABLE USR_SN
ADD CONSTRAINT USR_SN_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES USR
(
  USR_ID 
)

ALTER TABLE USR_SR
ADD CONSTRAINT USR_SR_SR_FK1 FOREIGN KEY
(
  SR_ID 
)
REFERENCES SR
(
  SR_ID 
)

ALTER TABLE USR_SR
ADD CONSTRAINT USR_SR_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES USR
(
  USR_ID 
)

ALTER TABLE USR_OPPTY
ADD CONSTRAINT USR_OPPTY_OPPTY_FK1 FOREIGN KEY
(
  OPPTY_ID 
)
REFERENCES OPPTY
(
  OPPTY_ID 
)

ALTER TABLE USR_OPPTY
ADD CONSTRAINT USR_OPPTY_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES USR
(
  USR_ID 
)

ALTER TABLE ACTIVITY
ADD CONSTRAINT ACTIVITY_FK1 FOREIGN KEY
(
  ACTIVITY_ID 
)
REFERENCES USR
(
  ACTIVITY_ID 
)

ALTER TABLE PROFILE
ADD CONSTRAINT PROFILE_USR_FK1 FOREIGN KEY
(
  USR_ID 
)
REFERENCES USR
(
  USR_ID 
)

CREATE SEQUENCE ACTPROJ AS BIGINT START WITH 20000 INCREMENT BY 100 MINVALUE 0 MAXVALUE 99999999999 NO CACHE


#joined lines to make DB2 CLP happy
CREATE TABLE USR_LOC ( USR_LOC_ID BIGINT NOT NULL,  LOC_ID BIGINT NOT NULL, USR_ID BIGINT NOT NULL,  LOC_NAME VARCHAR(200), LOC_TIMEZONE VARCHAR(30), ADDRESS_ID BIGINT, ADDRESS_ZIPCODE VARCHAR(15), PRIMARY KEY (USR_LOC_ID) )

CREATE TABLE USR_SN ( USR_SN_ID BIGINT NOT NULL,  SN_ID BIGINT NOT NULL, USR_ID BIGINT NOT NULL,  PRIMARY KEY (USR_SN_ID) )

CREATE TABLE USR_SR ( USR_SR_ID BIGINT NOT NULL,  SR_ID BIGINT NOT NULL, USR_ID BIGINT NOT NULL,  PRIMARY KEY (USR_SR_ID) )

CREATE TABLE USR_OPPTY ( USR_OPPTY_ID BIGINT NOT NULL,  USR_ID BIGINT NOT NULL, OPPTY_ID BIGINT NOT NULL,  PRIMARY KEY (USR_OPPTY_ID) )

#CREATE TABLE USR ( USR_ID BIGINT NOT NULL  , DESCRIPTION VARCHAR(256)  , EMAIL VARCHAR(150) NOT NULL  , BASE_PROFILE VARCHAR(400)  , NAME VARCHAR(100) NOT NULL  , USR_TYPE VARCHAR(30) NOT NULL  , SYS_TENANT_ID BIGINT NOT NULL  , AGE SMALLINT , TIMEZONE VARCHAR(30) , TERRITORY VARCHAR(100) , PRIMARY KEY (USR_ID) )
#CREATE TABLE USR ( USR_ID BIGINT NOT NULL  , DESCRIPTION VARCHAR(256)  , EMAIL VARCHAR(150) NOT NULL  , BASE_PROFILE VARCHAR(400)  , NAME VARCHAR(100) NOT NULL  , USR_TYPE VARCHAR(30) NOT NULL  , SYS_TENANT_ID BIGINT NOT NULL  , AGE SMALLINT , TIMEZONE VARCHAR(30) , TERRITORY VARCHAR(100) , SPECIAL_BLOCK CLOB(2000000)  , PRIMARY KEY (USR_ID) )
CREATE TABLE USR ( USR_ID BIGINT NOT NULL  , DESCRIPTION VARCHAR(256)  , EMAIL VARCHAR(150) NOT NULL  , BASE_PROFILE VARCHAR(400)  , NAME VARCHAR(100) NOT NULL  , USR_TYPE VARCHAR(30) NOT NULL  , SYS_TENANT_ID BIGINT NOT NULL  , AGE SMALLINT , TIMEZONE VARCHAR(30) , TERRITORY VARCHAR(100) , SPECIAL_BLOCK CLOB(2000000)  , SPECIAL_BLOCK2 BLOB(2M) , PRIMARY KEY (USR_ID) )

CREATE TABLE LOC ( LOC_ID BIGINT NOT NULL  , CREATED DATE , NAME VARCHAR(200) NOT NULL  , TIMEZONE VARCHAR(30) , PRIMARY KEY (LOC_ID) )

CREATE TABLE SN ( SN_ID BIGINT NOT NULL  , NAME VARCHAR(200) NOT NULL  , TYPE VARCHAR(30) NOT NULL  , PRIMARY KEY (SN_ID) )

CREATE TABLE SR ( SR_ID BIGINT NOT NULL  , NAME VARCHAR(200) NOT NULL  , SOURCE VARCHAR(30) , CUSTOMER_ID BIGINT , PRIMARY KEY (SR_ID) )

CREATE TABLE OPPTY ( OPPTY_ID BIGINT NOT NULL  , NAME VARCHAR(200) NOT NULL  , DEALSIZE VARCHAR(30)  , TERRITORY VARCHAR(100)  , PRIMARY KEY (OPPTY_ID) )

CREATE TABLE ADDRESS ( ADDRESS_ID BIGINT NOT NULL  , STREET_NAME VARCHAR(200)   , STREET_NUMBER VARCHAR(30) , UNIT_NUMBER VARCHAR(30) , BLOCK_NUMBER VARCHAR(30) , CITY VARCHAR(100) , STATE VARCHAR(30) NOT NULL , ZIPCODE VARCHAR(15) NOT NULL , COUNTRY VARCHAR(50) NOT NULL , PRIMARY KEY (ADDRESS_ID) )

CREATE TABLE USRTYPE ( USRTYPE_ID BIGINT NOT NULL   , NAME VARCHAR(200)   , PRIMARY KEY (USRTYPE_ID) )

CREATE TABLE ACTIVITY ( ACTIVITY_ID BIGINT NOT NULL  , USR_ID BIGINT NOT NULL , DESCRIPTION VARCHAR(256) NOT NULL , EFFECTIVE_START_DATE DATE , EFFECTIVE_END_DATE DATE , PRIMARY KEY (ACTIVITY_ID) )

CREATE TABLE PROFILE ( USR_ID BIGINT NOT NULL  , DESCRIPTION VARCHAR(256) NOT NULL )

ALTER TABLE USR_LOC ADD CONSTRAINT USR_LOC_LOC_FK1 FOREIGN KEY ( LOC_ID  ) REFERENCES LOC ( LOC_ID  )

ALTER TABLE USR_LOC ADD CONSTRAINT USR_LOC_USR_FK1 FOREIGN KEY ( USR_ID  ) REFERENCES USR ( USR_ID  )

ALTER TABLE USR_SN ADD CONSTRAINT USR_SN_SN_FK1 FOREIGN KEY ( SN_ID  ) REFERENCES SN ( SN_ID  )

ALTER TABLE USR_SN ADD CONSTRAINT USR_SN_USR_FK1 FOREIGN KEY ( USR_ID  ) REFERENCES USR ( USR_ID  )

ALTER TABLE USR_SR ADD CONSTRAINT USR_SR_SR_FK1 FOREIGN KEY ( SR_ID  ) REFERENCES SR ( SR_ID  )

ALTER TABLE USR_SR ADD CONSTRAINT USR_SR_USR_FK1 FOREIGN KEY ( USR_ID  ) REFERENCES USR ( USR_ID  )

ALTER TABLE USR_OPPTY ADD CONSTRAINT USR_OPPTY_OPPTY_FK1 FOREIGN KEY ( OPPTY_ID  ) REFERENCES OPPTY ( OPPTY_ID  )

ALTER TABLE USR_OPPTY ADD CONSTRAINT USR_OPPTY_USR_FK1 FOREIGN KEY ( USR_ID  ) REFERENCES USR ( USR_ID  )

ALTER TABLE PROFILE ADD CONSTRAINT PROFILE_USR_FK1 FOREIGN KEY ( USR_ID  ) REFERENCES USR ( USR_ID  )

CREATE SEQUENCE ACTPROJ AS BIGINT START WITH 20000 INCREMENT BY 100 MINVALUE 0 MAXVALUE 99999999999 NO CACHE
