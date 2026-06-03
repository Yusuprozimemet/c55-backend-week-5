-- =============================================================
-- Library — Database Schema
-- PostgreSQL — 3NF normalised
-- Parent tables first, then children, then N:M bridges.
-- =============================================================


-- -------------------------------------------------------------
-- publishers
-- A publishing house. One publisher publishes many books (1:N).
-- -------------------------------------------------------------
CREATE TABLE publishers (
    publisher_id   SERIAL        PRIMARY KEY,
    publisher_name VARCHAR(100)  NOT NULL UNIQUE
);


-- -------------------------------------------------------------
-- books
-- A book (the abstract work, not a physical copy).
-- -------------------------------------------------------------
CREATE TABLE books (
    book_id      SERIAL        PRIMARY KEY,
    book_isbn    VARCHAR(13)   NOT NULL UNIQUE,
    book_title   VARCHAR(100)  NOT NULL,
    publisher_id INT           NOT NULL,

    FOREIGN KEY (publisher_id)
        REFERENCES publishers (publisher_id)
        ON DELETE RESTRICT
);


-- -------------------------------------------------------------
-- authors
-- A single author. Books <-> Authors is N:M (see book_authors).
-- -------------------------------------------------------------
CREATE TABLE authors (
    author_id   SERIAL        PRIMARY KEY,
    author_name VARCHAR(100)  NOT NULL UNIQUE
);


-- -------------------------------------------------------------
-- tags
-- A single tag/topic. Books <-> Tags is N:M (see book_tags).
-- -------------------------------------------------------------
CREATE TABLE tags (
    tag_id   SERIAL        PRIMARY KEY,
    tag_name VARCHAR(100)  NOT NULL UNIQUE
);


-- -------------------------------------------------------------
-- members
-- A person with a library account.
-- -------------------------------------------------------------
CREATE TABLE members (
    member_id      SERIAL        PRIMARY KEY,
    member_name    VARCHAR(100)  NOT NULL,
    member_email   VARCHAR(100)  NOT NULL UNIQUE,
    member_phone   VARCHAR(20)   NOT NULL,
    member_address VARCHAR(200)  NOT NULL
);


-- -------------------------------------------------------------
-- cards
-- A library card. A member has exactly one card (1:1),
-- enforced by UNIQUE on member_id.
-- -------------------------------------------------------------
CREATE TABLE cards (
    card_id         SERIAL        PRIMARY KEY,
    card_number     VARCHAR(20)   NOT NULL UNIQUE,
    card_issued_on  DATE          NOT NULL,
    card_expires_on DATE          NOT NULL,
    member_id       INT           NOT NULL UNIQUE,

    CONSTRAINT chk_card_dates CHECK (card_expires_on > card_issued_on),

    FOREIGN KEY (member_id)
        REFERENCES members (member_id)
        ON DELETE CASCADE
);


-- -------------------------------------------------------------
-- copies
-- A physical copy of a book. One book has many copies (1:N).
-- -------------------------------------------------------------
CREATE TABLE copies (
    copy_id      SERIAL        PRIMARY KEY,
    copy_barcode VARCHAR(20)   NOT NULL UNIQUE,
    shelf_code   VARCHAR(20)   NOT NULL,
    book_id      INT           NOT NULL,

    FOREIGN KEY (book_id)
        REFERENCES books (book_id)
        ON DELETE RESTRICT
);


-- -------------------------------------------------------------
-- loans
-- A borrowing event: one member borrows one copy.
-- Has its own data (dates + fine), so it is an entity table,
-- not a pure bridge.
-- -------------------------------------------------------------
CREATE TABLE loans (
    loan_id     SERIAL        PRIMARY KEY,
    member_id   INT           NOT NULL,
    copy_id     INT           NOT NULL,
    borrowed_at TIMESTAMP     NOT NULL,
    due_date    TIMESTAMP     NOT NULL,
    returned_at TIMESTAMP,                     -- NULL = not returned yet
    fine_eur    NUMERIC(6,2)  NOT NULL DEFAULT 0,

    CONSTRAINT chk_fine     CHECK (fine_eur >= 0),
    CONSTRAINT chk_returned CHECK (returned_at IS NULL OR returned_at >= borrowed_at),

    FOREIGN KEY (member_id)
        REFERENCES members (member_id)
        ON DELETE RESTRICT,

    FOREIGN KEY (copy_id)
        REFERENCES copies (copy_id)
        ON DELETE RESTRICT
);


-- -------------------------------------------------------------
-- book_authors
-- N:M bridge between books and authors.
-- UNIQUE (book_id, author_id) prevents duplicate pairings.
-- -------------------------------------------------------------
CREATE TABLE book_authors (
    book_author_id SERIAL  PRIMARY KEY,
    book_id        INT     NOT NULL,
    author_id      INT     NOT NULL,

    UNIQUE (book_id, author_id),

    FOREIGN KEY (book_id)
        REFERENCES books (book_id)
        ON DELETE CASCADE,

    FOREIGN KEY (author_id)
        REFERENCES authors (author_id)
        ON DELETE CASCADE
);


-- -------------------------------------------------------------
-- book_tags
-- N:M bridge between books and tags.
-- UNIQUE (book_id, tag_id) prevents duplicate pairings.
-- -------------------------------------------------------------
CREATE TABLE book_tags (
    book_tag_id SERIAL  PRIMARY KEY,
    book_id     INT     NOT NULL,
    tag_id      INT     NOT NULL,

    UNIQUE (book_id, tag_id),

    FOREIGN KEY (book_id)
        REFERENCES books (book_id)
        ON DELETE CASCADE,

    FOREIGN KEY (tag_id)
        REFERENCES tags (tag_id)
        ON DELETE CASCADE
);
