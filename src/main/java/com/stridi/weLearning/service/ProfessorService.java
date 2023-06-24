package com.stridi.weLearning.service;

import com.stridi.weLearning.entity.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ProfessorService {
    Professor getProfile();

    void changePassword(String oldPassword, String newPassword);

    Page<Professor> getProfessors(Integer page, Integer pageSize);

    Professor getProfessor(Long professorId);

    Page<Group> getGroups(Integer page, Integer pageSize);

    Group getGroup(Long groupId);

    Page<Student> getStudents(Integer page, Integer pageSize);

    Student getStudent(Long studentId);

    Page<File> getFiles(Long groupId, Integer page, Integer pageSize);

    File getFile(Long fileId);

    Page<Lesson> getDiscussions(Integer page, Integer pageSize);

    Lesson getDiscussion(Long discussionId);

    Lesson createDiscussion(String name, LocalDateTime date);

    Lesson updateDiscussion(Long discussionId, String name, LocalDateTime date);

    void deleteDiscussion(Long discussionId);

    Page<Reservation> getReservations(Integer page, Integer pageSize);

    Reservation getReservation(Long reservationId);

    void deleteReservation(Long reservationId);
}
