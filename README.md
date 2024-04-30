
# CST438 School Management System Project

Group project for CST438: Software Engineering

You can see our Software Requirements Specification Document here:
[SRS Doc Link](src/Software-Requirements-Specification-(SRS).pdf)

Using Agile techniques of BDD (Behavior Driven Design), we created a school management application for students, instructors, and admins to enroll manage and create courses and grades. We implemented a REST api using Spring Boot Java paired with a front end for the service using React and JavaScript. We tested the application using Mockmvc for the backend and Selenium for the frontend. Later we implemented JWT security into the application to allow authorized users to access pages and make requests, specific to their JWT token, to the backend.

The default branch V1.0secure, the most up to date branch, is with the implementation of JWT security. The other branches are various stages of our project decided by our professor.

## Demonstrations
### Administrator Section
Administrators have the ability to create and manage courses, users, sections, and other key functionalities.

![Admin Demo](src/admin.gif)

### Instructor Section
Instructors can add and manage assignments, manage classes, and update scores and final grades.

![Instructor Demo](src/Instructor.gif)

### Student Section
Students can enroll and drop courses, view their grades and assignments, and view transcript and enrollment pages.

![Student Demo](src/Student.gif)
