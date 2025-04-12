# HW4 – Staff Role Implementation  
**BY:** Ganesh Kolli  
**Course:** CSE 360 – Software Engineering  
**Assignment:** Individual Homework 4

---

## Overview
This project implements a set of Staff-role user stories as part of Homework 4 based on the TP3 team submission. It includes UI development using JavaFX, backend integration with an H2 database, and test coverage using JUnit 5. Javadoc documentation has also been generated for the new and modified classes.

---

## Implemented User Stories for Staff Role

1. **View All Questions & Answers**  
   Staff can view all submitted questions and their answers using a formatted text view.

2. **View Private Feedback**  
   Staff can read feedback messages left privately by students to understand issues or suggestions.

3. **Add Note for Instructor**  
   Staff can submit notes for the instructor that are stored in a dedicated database table.

4. **Search Content by Keyword**  
   Staff can search questions and answers by keyword to find relevant content quickly.

5. **Generate Report Logic (Simulated)**  
   A placeholder functionality exists to simulate future report generation capability.

These are implemented in both the UI (`StaffHomePage.java`) and database handler (`DatabaseHelper.java`).

---

## Screencast

**Screencast Link:**  
https://asu.zoom.us/rec/share/rRWQjtolDZOKLhrOOJDHQ7nO9MoL6TuSJjczuaudC2YsJi-SFrhvsgYxs1gd2pQq.AM6EPGttHXFPOTd0
Passcode:N@#OR+3# 
---

## JUnit Tests

JUnit tests for the staff role have been written in the `StaffDatabaseHelperTest.java` file.

path : HW4/src/databasePart1/StaffDatabaseHelperTest.java

### Tests Included:

- `testGetAllQuestionsAndAnswers()`
- `testGetAllPrivateFeedback()`
- `testAddInstructorNote()`
- `testSearchContentByKeyword()`
- `testGenerateReportLogic()`

File location:  
`/src/databasePart1/StaffDatabaseHelperTest.java`

---
![image](https://github.com/user-attachments/assets/c51b3b87-450f-4a5d-a5d6-ee229c8937f1)

## Javadoc

Javadoc documentation has been generated for the following files:

- `StaffHomePage.java`
- `StaffDatabaseHelperTest.java`
- Staff-related methods in `DatabaseHelper.java`

Javadoc Output Folder:  
`/HW4/doc/index.html`

**Javadoc Link:**  
file:///C:/Users/ganesh/ASU-CSE360-SP25/HW4/doc/index.html
