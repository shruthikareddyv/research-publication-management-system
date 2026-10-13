-- Research Publication Management System (MySQL 8+)
-- Run this script in MySQL Workbench. It creates any missing tables and
-- includes the type-specific tables required by the publication entry form.
-- It intentionally does not DROP existing tables or data.

CREATE DATABASE IF NOT EXISTS ResearchPublicationDB;
USE ResearchPublicationDB;

CREATE TABLE IF NOT EXISTS Faculty (
    Faculty_ID INT PRIMARY KEY,
    Faculty_Name VARCHAR(50) NOT NULL,
    Department VARCHAR(50) NOT NULL,
    Designation VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Publication (
    Publication_ID VARCHAR(10) PRIMARY KEY,
    Title VARCHAR(200) NOT NULL,
    Publication_Type VARCHAR(30) NOT NULL,
    `Year` INT NOT NULL,
    Publisher VARCHAR(100) NOT NULL,
    DOI VARCHAR(100),
    URL VARCHAR(300)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Faculty_Publication (
    Faculty_ID INT NOT NULL,
    Publication_ID VARCHAR(10) NOT NULL,
    Author_Order TINYINT NOT NULL,
    PRIMARY KEY (Faculty_ID, Publication_ID),
    UNIQUE KEY uq_publication_author_order (Publication_ID, Author_Order),
    CONSTRAINT chk_author_order CHECK (Author_Order BETWEEN 1 AND 5),
    CONSTRAINT fk_fp_faculty FOREIGN KEY (Faculty_ID)
        REFERENCES Faculty(Faculty_ID) ON DELETE CASCADE,
    CONSTRAINT fk_fp_publication FOREIGN KEY (Publication_ID)
        REFERENCES Publication(Publication_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Indexing (
    Publication_ID VARCHAR(10) NOT NULL,
    Indexing_Name VARCHAR(50) NOT NULL,
    PRIMARY KEY (Publication_ID, Indexing_Name),
    CONSTRAINT fk_indexing_publication FOREIGN KEY (Publication_ID)
        REFERENCES Publication(Publication_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Member 3: publication-type-specific details. The Publication_ID is also
-- the primary key so each publication can have one corresponding detail row.
CREATE TABLE IF NOT EXISTS Journal (
    Publication_ID VARCHAR(10) PRIMARY KEY,
    Volume VARCHAR(30) NOT NULL,
    Issue VARCHAR(30) NOT NULL,
    Pages VARCHAR(50) NOT NULL,
    CONSTRAINT fk_journal_publication FOREIGN KEY (Publication_ID)
        REFERENCES Publication(Publication_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Conference (
    Publication_ID VARCHAR(10) PRIMARY KEY,
    Conference_Date DATE NOT NULL,
    CONSTRAINT fk_conference_publication FOREIGN KEY (Publication_ID)
        REFERENCES Publication(Publication_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Book (
    Publication_ID VARCHAR(10) PRIMARY KEY,
    ISBN VARCHAR(30) NOT NULL,
    Edition VARCHAR(30) NOT NULL,
    CONSTRAINT fk_book_publication FOREIGN KEY (Publication_ID)
        REFERENCES Publication(Publication_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Book_Chapter (
    Publication_ID VARCHAR(10) PRIMARY KEY,
    Book_Title VARCHAR(200) NOT NULL,
    Chapter_Number INT NOT NULL,
    Pages VARCHAR(50) NOT NULL,
    CONSTRAINT chk_chapter_number CHECK (Chapter_Number > 0),
    CONSTRAINT fk_book_chapter_publication FOREIGN KEY (Publication_ID)
        REFERENCES Publication(Publication_ID) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Primary keys and foreign keys support the core joins and lookups.
