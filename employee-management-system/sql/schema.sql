-- Normalized schema (3NF): departments 1---* employees 1---* attendance

CREATE TABLE IF NOT EXISTS departments (
    dept_id     SERIAL PRIMARY KEY,
    dept_name   VARCHAR(100) NOT NULL UNIQUE,
    location    VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS employees (
    emp_id       SERIAL PRIMARY KEY,
    first_name   VARCHAR(50)  NOT NULL,
    last_name    VARCHAR(50)  NOT NULL,
    email        VARCHAR(120) NOT NULL UNIQUE,
    phone        VARCHAR(20),
    designation  VARCHAR(80),
    salary       NUMERIC(12,2) NOT NULL CHECK (salary >= 0),
    hire_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    dept_id      INT REFERENCES departments(dept_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS attendance (
    att_id     SERIAL PRIMARY KEY,
    emp_id     INT  NOT NULL REFERENCES employees(emp_id) ON DELETE CASCADE,
    att_date   DATE NOT NULL,
    status     VARCHAR(10) NOT NULL CHECK (status IN ('PRESENT','ABSENT','LEAVE','HALF_DAY')),
    check_in   TIME,
    check_out  TIME,
    CONSTRAINT uq_emp_date UNIQUE (emp_id, att_date)
);

CREATE INDEX IF NOT EXISTS idx_emp_dept ON employees(dept_id);
CREATE INDEX IF NOT EXISTS idx_emp_name ON employees(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_att_date ON attendance(att_date);

INSERT INTO departments (dept_name, location) VALUES
    ('Engineering', 'Bengaluru'),
    ('Human Resources', 'Mumbai'),
    ('Finance', 'Chennai'),
    ('Sales', 'Delhi')
ON CONFLICT (dept_name) DO NOTHING;
