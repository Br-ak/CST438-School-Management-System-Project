import React, { useState, useEffect } from "react";
import { SERVER_URL } from "../../Constants";

// // students displays a list of open sections for a
// // use the URL /sections/open
// // the REST api returns a list of SectionDTO objects

// // the student can select a section and enroll
// // issue a POST with the URL /enrollments?secNo= &studentId=3
// // studentId=3 will be removed in assignment 7.

const CourseEnroll = (props) => {
    const [sections, setSections] = useState([]);
    const [selectedSection, setSelectedSection] = useState();
    const [enrollmentMessage, setEnrollmentMessage] = useState("");

    useEffect(() => {
        fetchOpenSections();
    }, []);

    const fetchOpenSections = async () => {
        try {
            const jwt = sessionStorage.getItem('jwt');
            const response = await fetch(`${SERVER_URL}/sections/open`,
                {
                    headers: {
                        'Authorization': jwt,
                    }
                });
            if (response.ok) {
                const data = await response.json();
                setSections(data);
                console.log(data);
            } else {
                console.error("Failed to fetch open sections:", response.statusText);
            }

        } catch (error) {
            console.error("Error while fetching open sections:", error);
        }
    };

    const enrollStudent = async () => {
        if (!selectedSection) {
            setEnrollmentMessage("Please select a section to enroll.");
            return;
        }

        try {
            const jwt = sessionStorage.getItem('jwt');
            const response = await fetch(
                `${SERVER_URL}/enrollments/sections/${selectedSection.secNo}`,
                {
                    method: "POST",
                    headers: {
                        'Authorization': jwt,
                        "Content-Type": "application/json",
                    },
                }
            );
            if (response.ok) {
                setEnrollmentMessage("Enrollment successful!");
            } else {
                console.error("Failed to enroll:", response.statusText);
            }
        } catch (error) {
            console.error("Error while enrolling:", error);
        }
    };

    return (
        <>
            <h3>Enroll in a Course</h3>
            <h4>{enrollmentMessage}</h4>
            <select onChange={(e) => setSelectedSection(JSON.parse(e.target.value))}>
                <option value="">Select a Section</option>
                {sections.map((section) => (
                    <option id={"section " + section.secNo} key={section.secNo} value={JSON.stringify(section)}>
                        {section.courseId} - Section {section.secNo}, {section.semester}{" "}
                        {section.year}
                    </option>
                ))}
            </select>
            <br />
            <button id="enrollButton" onClick={enrollStudent}>Enroll</button>
        </>
    );
};

export default CourseEnroll;
