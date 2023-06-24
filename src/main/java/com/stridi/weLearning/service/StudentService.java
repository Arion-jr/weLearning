package com.stridi.weLearning.service;

import com.stridi.weLearning.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {
    Student getProfile();

    void changePassword(String oldPassword, String newPassword);

    Page<Student> getStudents(Integer page, Integer pageSize);

    Student getStudent(Long studentId);

    Page<Group> getGroups(Integer page, Integer pageSize);

    Group getGroup(Long groupId);

    Group createGroup(Long professorId, String name);

    Group updateGroup(Long groupId, String name);

    void deleteGroup(Long groupId);

    Group joinGroup(Long groupId);

    void leaveGroup(Long groupId);

    void removeStudentFromGroup(Long groupId, Long studentId);

    Page<File> getFiles(Long groupId, Integer page, Integer pageSize);

    File getFile(Long fileId);

    File putFile(Long groupId, MultipartFile doc);

    void deleteFile(Long fileId);

    Page<Professor> getProfessors(Integer page, Integer pageSize);

    Professor getProfessor(Long professorId);

    Page<Lesson> getDiscussions(Long professorId, Integer page, Integer pageSize);

    Lesson getDiscussion(Long discussionId);

    Page<Reservation> getReservations(Long groupId, Integer page, Integer pageSize);

    Reservation getReservation(Long reservationId);

    Reservation createReservation(Long groupId, Long discussionId);

    Reservation updateReservation(Long reservationId, Long discussionId);

    void deleteReservation(Long reservationId);
}
