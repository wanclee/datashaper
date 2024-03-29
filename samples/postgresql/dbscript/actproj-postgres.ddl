-- Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com

DROP TABLE USR_LOC;
DROP TABLE USR_SN;
DROP TABLE USR_SR;
DROP TABLE USR_OPPTY;
DROP TABLE ACTIVITY;
DROP TABLE PROFILE;
DROP TABLE USR;
DROP TABLE LOC;
DROP TABLE SN;
DROP TABLE SR;
DROP TABLE OPPTY;
DROP TABLE ADDRESS;
DROP TABLE USRTYPE;
DROP SEQUENCE ACTPROJ;


CREATE TABLE USR_LOC 
(
  USR_LOC_ID BIGINT PRIMARY KEY,
  LOC_ID BIGINT NOT NULL,
  USR_ID BIGINT NOT NULL,
  LOC_NAME VARCHAR(200),
  LOC_TIMEZONE VARCHAR(30),
  ADDRESS_ID BIGINT,
  ADDRESS_ZIPCODE VARCHAR(15)
);

CREATE TABLE USR_SN 
(
  USR_SN_ID BIGINT PRIMARY KEY,
  SN_ID BIGINT NOT NULL,
  USR_ID BIGINT NOT NULL
);

CREATE TABLE USR_SR
(
  USR_SR_ID BIGINT PRIMARY KEY,
  SR_ID BIGINT NOT NULL,
  USR_ID BIGINT NOT NULL
);

CREATE TABLE USR_OPPTY
(
  USR_OPPTY_ID BIGINT PRIMARY KEY,
  OPPTY_ID BIGINT NOT NULL,
  USR_ID BIGINT NOT NULL
);

CREATE TABLE USR 
(
  USR_ID BIGINT PRIMARY KEY,
  DESCRIPTION VARCHAR(256),
  EMAIL VARCHAR(150) NOT NULL,
  BASE_PROFILE VARCHAR(4000),
  NAME VARCHAR(100) NOT NULL,
  USR_TYPE VARCHAR(30) NOT NULL, 
  SYS_TENANT_ID BIGINT NOT NULL,
  AGE BIGINT,
  TIMEZONE VARCHAR(30),
  TERRITORY VARCHAR(100),
  SPECIAL_BLOCK TEXT,
  SPECIAL_BLOCK2 BYTEA
 );

CREATE TABLE LOC 
(
  LOC_ID BIGINT PRIMARY KEY, 
  CREATED DATE,
  NAME VARCHAR(200) NOT NULL,
  TIMEZONE VARCHAR(30)
);

CREATE TABLE SN 
(
  SN_ID BIGINT PRIMARY KEY, 
  NAME VARCHAR(200) NOT NULL,
  TYPE VARCHAR(30) NOT NULL
);

CREATE TABLE SR
(
  SR_ID BIGINT PRIMARY KEY, 
  NAME VARCHAR(200) NOT NULL,
  SOURCE VARCHAR(30),
  CUSTOMER_ID BIGINT NOT NULL
);

CREATE TABLE OPPTY
(
  OPPTY_ID BIGINT PRIMARY KEY, 
  NAME VARCHAR(200) NOT NULL,
  DEALSIZE VARCHAR(30),
  TERRITORY VARCHAR(100)
);

CREATE TABLE ADDRESS
(
  ADDRESS_ID BIGINT PRIMARY KEY, 
  STREET_NAME VARCHAR(200),
  STREET_NUMBER VARCHAR(30),
  UNIT_NUMBER VARCHAR(30),
  BLOCK_NUMBER VARCHAR(30),
  CITY VARCHAR(100),
  STATE VARCHAR(30) NOT NULL,
  ZIPCODE VARCHAR(15) NOT NULL,
  COUNTRY VARCHAR(50) NOT NULL
);

CREATE TABLE USRTYPE
(
  USRTYPE_ID BIGINT PRIMARY KEY, 
  NAME VARCHAR(200) NOT NULL
);

CREATE TABLE ACTIVITY
(
  ACTIVITY_ID BIGINT PRIMARY KEY, 
  USR_ID BIGINT NOT NULL,
  DESCRIPTION VARCHAR(256) NOT NULL,
  EFFECTIVE_START_DATE DATE,
  EFFECTIVE_END_DATE DATE
);

CREATE TABLE PROFILE
(
  USR_ID BIGINT NOT NULL,
  DESCRIPTION VARCHAR(256) NOT NULL
);

ALTER TABLE USR_LOC ADD FOREIGN KEY (LOC_ID) REFERENCES LOC;
ALTER TABLE USR_LOC ADD FOREIGN KEY (USR_ID) REFERENCES USR;
ALTER TABLE USR_SN ADD FOREIGN KEY (SN_ID) REFERENCES SN;
ALTER TABLE USR_SN ADD FOREIGN KEY (USR_ID) REFERENCES USR;
ALTER TABLE USR_SR ADD FOREIGN KEY (SR_ID) REFERENCES SR;
ALTER TABLE USR_SR ADD FOREIGN KEY (USR_ID) REFERENCES USR;
ALTER TABLE USR_OPPTY ADD FOREIGN KEY (OPPTY_ID) REFERENCES OPPTY;
ALTER TABLE USR_OPPTY ADD FOREIGN KEY (USR_ID) REFERENCES USR;
ALTER TABLE ACTIVITY ADD FOREIGN KEY (USR_ID) REFERENCES USR;
ALTER TABLE PROFILE ADD FOREIGN KEY (USR_ID) REFERENCES USR;


CREATE SEQUENCE ACTPROJ INCREMENT BY 100 START WITH 20000 MINVALUE 0 MAXVALUE 99999999999;


