package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    TermRepository termRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SectionRepository sectionRepository;

    // student gets transcript showing list of all enrollments
    // studentId will be temporary until Login security is implemented
    // example URL /transcript?studentId=19803
    @GetMapping("/transcript")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(Principal principal) {
        
        User student = userRepository.findByEmail(principal.getName());

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(student.getId());
        if (enrollments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No enrollments found for student Id: " + student.getId());
        }
        List<EnrollmentDTO> elist = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            // not sure what this part covered but it threw an error and works without it
            // Enrollment grade = null;
            // if (enrollment.getGrade() !=null) {
            //     grade = enrollmentRepository.(enrollment.getGrade());
            // }
            elist.add(new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getGrade(),
                    enrollment.getUser().getId(),
                    enrollment.getUser().getName(),
                    enrollment.getUser().getEmail(),
                    enrollment.getSection().getCourse().getCourseId(),
                    enrollment.getSection().getSecId(),
                    enrollment.getSection().getSectionNo(),
                    enrollment.getSection().getBuilding(),
                    enrollment.getSection().getRoom(),
                    enrollment.getSection().getTimes(),
                    enrollment.getSection().getCourse().getCredits(),
                    enrollment.getSection().getTerm().getYear(),
                    enrollment.getSection().getTerm().getSemester()));
        }
        return elist;
    }

    // student gets a list of their enrollments for the given year, semester
    // user must be student
    // studentId will be temporary until Login security is implemented
    @GetMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getSchedule(Principal principal,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

        User student = userRepository.findByEmail(principal.getName());

        // TODO
        // hint: use enrollment repository method findByYearAndSemesterOrderByCourseId

        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester,
                student.getId());
        if (enrollments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No enrollments found for student Id: " + student.getId());
        }
        List<EnrollmentDTO> elist = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            elist.add(new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getGrade(),
                    enrollment.getUser().getId(),
                    enrollment.getUser().getName(),
                    enrollment.getUser().getEmail(),
                    enrollment.getSection().getCourse().getCourseId(),
                    enrollment.getSection().getSecId(),
                    enrollment.getSection().getSectionNo(),
                    enrollment.getSection().getBuilding(),
                    enrollment.getSection().getRoom(),
                    enrollment.getSection().getTimes(),
                    enrollment.getSection().getCourse().getCredits(),
                    enrollment.getSection().getTerm().getYear(),
                    enrollment.getSection().getTerm().getSemester()));
        }
        return elist;
    }

    // student adds enrollment into a section
    // user must be student
    // return EnrollmentDTO with enrollmentId generated by database
    @PostMapping("/enrollments/sections/{sectionNo}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(Principal principal,
            @PathVariable int sectionNo) {

        User student = userRepository.findByEmail(principal.getName());

        // TODO
        // check that the Section entity with primary key sectionNo exists
        // check that today is between addDate and addDeadline for the section
        // check that student is not already enrolled into this section
        // create a new enrollment entity and save. The enrollment grade will
        // be NULL until instructor enters final grades for the course.

        Section section = sectionRepository.findById(sectionNo).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found matching: " + sectionNo));

        // Don't think this needed anymore since the user will be logged in and have a userId
//        User student = userRepository.findById(studentId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "User not found with student Id: " + studentId));


        LocalDate today = LocalDate.now();
        LocalDate localAddDate = (section.getTerm().getAddDate()).toLocalDate();
        LocalDate localDeadline = (section.getTerm().getAddDeadline()).toLocalDate();

        if (today.isBefore(localAddDate) || today.isAfter(localDeadline)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to enroll. Enrollment period has ended for: " + sectionNo);
        }

        Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo,
                student.getId());
        if (existingEnrollment != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is already enrolled in: " + sectionNo);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setGrade(null);
        enrollment.setUser(student);
        enrollment.setSection(section);

        enrollmentRepository.save(enrollment);

        return new EnrollmentDTO(
                enrollment.getEnrollmentId(),
                null,
                student.getId(),
                student.getName(),
                student.getEmail(),
                section.getCourse().getCourseId(),
                section.getSecId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                section.getCourse().getCredits(),
                section.getTerm().getYear(),
                section.getTerm().getSemester());

    }

    // student drops a course
    // user must be student
    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {
        // TODO
        // check that today is not after the dropDeadline for section


        Enrollment e = enrollmentRepository.findById(enrollmentId).orElse(null);
        List<Grade> grades = e.getGrades();

        if (e != null) {
            LocalDate localDate = LocalDate.now();
            Date today = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dropDeadline = e.getSection().getTerm().getDropDeadline();
            if (!today.after(dropDeadline)) {
                for (Grade grade : grades){
                    int tempId = grade.getGradeId();
                    Grade tempGrade = gradeRepository.findById(tempId).orElse(null);
                    if (tempGrade != null){
                        gradeRepository.delete(tempGrade);
                    }
                }
                enrollmentRepository.delete(e);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "drop deadline has passed");
            }
        } else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enrollment not found");
        }
    }
}