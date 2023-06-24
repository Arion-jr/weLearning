package com.stridi.weLearning.service.impl;

import com.stridi.weLearning.service.ProfessorService;
import com.stridi.weLearning.service.jwt.JWTAuthenticationService;
import com.stridi.weLearning.service.jwt.JWTService;
import com.stridi.weLearning.utils.object.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.stridi.weLearning.entity.Account;
import com.stridi.weLearning.entity.Lesson;
import com.stridi.weLearning.entity.File;
import com.stridi.weLearning.entity.Group;
import com.stridi.weLearning.entity.Professor;
import com.stridi.weLearning.entity.Reservation;
import com.stridi.weLearning.entity.Student;
import com.stridi.weLearning.repository.AccountRepository;
import com.stridi.weLearning.repository.DiscussionRepository;
import com.stridi.weLearning.repository.FileRepository;
import com.stridi.weLearning.repository.GroupRepository;
import com.stridi.weLearning.repository.ProfessorRepository;
import com.stridi.weLearning.repository.ReservationRepository;
import com.stridi.weLearning.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProfessorServiceImpl implements ProfessorService {
	private final FileRepository fileRepository;
	private final GroupRepository groupRepository;
	private final StudentRepository studentRepository;
	private final AccountRepository accountRepository;
	private final ProfessorRepository professorRepository;
	private final DiscussionRepository discussionRepository;
	private final ReservationRepository reservationRepository;

	private final JWTAuthenticationService jwtAuthenticationService;

	private Professor getProfessor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			User user = ((User) authentication.getPrincipal());

			if (!user.getAuthorities().contains(new SimpleGrantedAuthority(Role.Professor.name()))) {
				throw new JWTService.TokenVerificationException();
			}

			Account account = accountRepository.findByRoleAndUsername(Role.Professor, user.getUsername())
					.orElseThrow(JWTService.TokenVerificationException::new);
			return professorRepository.findByAccountId(account.getId())
					.orElseThrow(JWTService.TokenVerificationException::new);
		}
		throw new JWTService.TokenVerificationException();
	}

	@Override
	public Professor getProfile() {
		return getProfessor();
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		log.warn(getProfessor().toString());
		String username = getProfessor().getAccount().getUsername();
		jwtAuthenticationService.changePassword(Role.Professor, username, oldPassword, newPassword);
	}

	@Override
	public Page<Professor> getProfessors(Integer page, Integer pageSize) {
		if (page == null || page < 0) page = 0;
		if (pageSize == null || pageSize < 1) pageSize = 1;
		else if (pageSize > 50) pageSize = 50;
		return professorRepository.findAll(PageRequest.of(page, pageSize));
	}

	@Override
	public Professor getProfessor(Long professorId) {
		return professorRepository.findById(professorId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid professorId"));
	}

	@Override
	public Page<Group> getGroups(Integer page, Integer pageSize) {
		if (page == null || page < 0) page = 0;
		if (pageSize == null || pageSize < 1) pageSize = 1;
		else if (pageSize > 50) pageSize = 50;
		Professor professor = getProfessor();
		return groupRepository.findAllByProfessorIdAndDeletedFalse(professor.getId(), PageRequest.of(page, pageSize));
	}

	@Override
	public Group getGroup(Long groupId) {
		return groupRepository.findById(groupId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid groupId"));
	}

	@Override
	public Page<Student> getStudents(Integer page, Integer pageSize) {
		if (page == null || page < 0) page = 0;
		if (pageSize == null || pageSize < 1) pageSize = 1;
		else if (pageSize > 50) pageSize = 50;
		return studentRepository.findAll(PageRequest.of(page, pageSize));
	}

	@Override
	public Student getStudent(Long studentId) {
		return studentRepository.findById(studentId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid studentId"));
	}

	@Override
	public Page<File> getFiles(Long groupId, Integer page, Integer pageSize) {
		if (page == null || page < 0) page = 0;
		if (pageSize == null || pageSize < 1) pageSize = 1;
		else if (pageSize > 50) pageSize = 50;
		return fileRepository.findAllByGroupIdAndDeletedFalseOrderByCreatedOnDesc(groupId, PageRequest.of(page, pageSize));
	}

	@Override
	public File getFile(Long fileId) {
		return fileRepository.findById(fileId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid fileId"));
	}

	@Override
	public Page<Lesson> getDiscussions(Integer page, Integer pageSize) {
		if (page == null || page < 0) page = 0;
		if (pageSize == null || pageSize < 1) pageSize = 1;
		else if (pageSize > 50) pageSize = 50;
		Professor professor = getProfessor();
		return discussionRepository.findAllByProfessorIdAndDeletedFalse(professor.getId(), PageRequest.of(page, pageSize));
	}

	@Override
	public Lesson getDiscussion(Long discussionId) {
		return discussionRepository.findById(discussionId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid discussionId"));
	}

	@Override
	public Lesson createDiscussion(String name, LocalDateTime date) {
		Professor professor = getProfessor();

		if (LocalDateTime.now().isAfter(date)) {
			throw new IllegalArgumentException("Unable to create a discussion with an earlier date than the current one");
		}

		return discussionRepository.save(new Lesson(professor.getId(), name, date));
	}

	@Override
	public Lesson updateDiscussion(Long discussionId, String name, LocalDateTime date) {
		Professor professor = getProfessor();

		Lesson discussion = discussionRepository.findByIdAndDeletedFalse(discussionId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid discussionId"));

		if (!discussion.getProfessorId().equals(professor.getId())) {
			throw new IllegalArgumentException("You are not the professor of this discussion");
		}

		if (LocalDateTime.now().isAfter(discussion.getDate())) {
			throw new IllegalArgumentException("Unable to update a past discussion");
		}

		if (LocalDateTime.now().isAfter(date)) {
			throw new IllegalArgumentException("Unable to update the date with an earlier than the current one");
		}

		discussion.setName(name);
		discussion.setDate(date);
		return discussionRepository.save(discussion);
	}

	@Override
	public void deleteDiscussion(Long discussionId) {
		Professor professor = getProfessor();

		Lesson discussion = discussionRepository.findByIdAndDeletedFalse(discussionId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid reservationId"));

		if (!discussion.getProfessorId().equals(professor.getId())) {
			throw new IllegalArgumentException("You are not the professor of this discussion");
		}

		if (LocalDateTime.now().isAfter(discussion.getDate())) {
			throw new IllegalArgumentException("Unable to delete a past discussion");
		}

		discussion.setDeleted(true);
		discussionRepository.save(discussion);
		List<Reservation> reservations = reservationRepository.findAllByDiscussionIdAndDeletedFalse(discussionId);
		reservations.forEach(reservation -> reservation.setDeleted(true));
		reservationRepository.saveAll(reservations);
	}

	@Override
	public Page<Reservation> getReservations(Integer page, Integer pageSize) {
		if (page == null || page < 0) page = 0;
		if (pageSize == null || pageSize < 1) pageSize = 1;
		else if (pageSize > 50) pageSize = 50;
		Professor professor = getProfessor();
		return reservationRepository.findAllByDiscussion_ProfessorIdAndDeletedFalse(professor.getId(), PageRequest.of(page, pageSize));
	}

	@Override
	public Reservation getReservation(Long reservationId) {
		return reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid reservationId"));
	}

	@Override
	public void deleteReservation(Long reservationId) {
		Professor professor = getProfessor();

		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid reservationId"));

		Lesson discussion = discussionRepository.findById(reservation.getDiscussionId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid reservationId"));

		if (!discussion.getProfessorId().equals(professor.getId())) {
			throw new IllegalArgumentException("You are not the professor of this discussion");
		}

		reservation.setDeleted(true);
		reservationRepository.save(reservation);
	}
}
