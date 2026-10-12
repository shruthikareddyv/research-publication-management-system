-- Research Publication Management System
-- Clean database setup: table definitions only, no sample data and no DROP TABLE statements.
-- Run this script in MySQL Workbench using an account with permission to create databases/tables.

CREATE DATABASE IF NOT EXISTS ResearchPublicationDB;
USE ResearchPublicationDB;

CREATE TABLE IF NOT EXISTS Faculty (
    Faculty_ID INT PRIMARY KEY,
    Faculty_Name VARCHAR(50),
    Department VARCHAR(50),
    Designation VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS Publication (
    Publication_ID VARCHAR(10) PRIMARY KEY,
    Title VARCHAR(200),
    Publication_Type VARCHAR(30),
    `Year` INT,
    Publisher VARCHAR(100),
    DOI VARCHAR(100),
    URL VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS Faculty_Publication (
    Faculty_ID INT,
    Publication_ID VARCHAR(10),
    Author_Order INT,
    PRIMARY KEY (Faculty_ID, Publication_ID),
    FOREIGN KEY (Faculty_ID) REFERENCES Faculty(Faculty_ID),
    FOREIGN KEY (Publication_ID) REFERENCES Publication(Publication_ID)
);

CREATE TABLE IF NOT EXISTS Indexing (
    Publication_ID VARCHAR(10),
    Indexing_Name VARCHAR(50),
    PRIMARY KEY (Publication_ID, Indexing_Name),
    FOREIGN KEY (Publication_ID) REFERENCES Publication(Publication_ID)
);
