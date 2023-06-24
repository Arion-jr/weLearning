package com.stridi.weLearning.service;

import com.stridi.weLearning.repository.DiscussionRepository;
import com.stridi.weLearning.utils.object.Role;
import lombok.extern.slf4j.Slf4j;
import com.stridi.weLearning.entity.Account;
import com.stridi.weLearning.entity.Discussion;
import com.stridi.weLearning.entity.FellowStudent;
import com.stridi.weLearning.entity.File;
import com.stridi.weLearning.entity.Group;
import com.stridi.weLearning.entity.Professor;
import com.stridi.weLearning.entity.Reservation;
import com.stridi.weLearning.entity.Student;
import com.stridi.weLearning.repository.AccountRepository;
import com.stridi.weLearning.repository.FellowStudentRepository;
import com.stridi.weLearning.repository.FileRepository;
import com.stridi.weLearning.repository.GroupRepository;
import com.stridi.weLearning.repository.ProfessorRepository;
import com.stridi.weLearning.repository.ReservationRepository;
import com.stridi.weLearning.repository.StudentRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@SuppressWarnings("FieldCanBeLocal")
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ComponentScan({"com.stridi.weLearning.service", "com.stridi.weLearning.configuration.mail"})
public class StudentServiceTest {

	@Autowired
	private StudentService studentService;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private ProfessorRepository professorRepository;
	@Autowired
	private DiscussionRepository discussionRepository;
	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private FellowStudentRepository fellowStudentRepository;

	@Value("${file.upload-dir}")
	private String uploadDir;

	private Student student;
	private Professor professor1;
	private final String studentUsername = "student@mail.com";
	private final String defaultPassword = "TestPassword123";
	private final String defaultPasswordSha3 = "363999f7918bb84260f481cceaed396fb046e8dc25750c5c3ae0e8088ae17b22";
	private final LocalDateTime now = LocalDateTime.now().withNano(0);

	@BeforeEach
	public void beforeEach() {
		groupRepository.deleteAll();
		studentRepository.deleteAll();
		professorRepository.deleteAll();
		accountRepository.deleteAll();
		fellowStudentRepository.deleteAll();
		fileRepository.deleteAll();
		reservationRepository.deleteAll();
		discussionRepository.deleteAll();

		String student1Mail = "student1@mail.com";
		String student2Mail = "student2@mail.com";
		String student3Mail = "student3@mail.com";
		String professor1Mail = "professor1@mail.com";
		String professor2Mail = "professor2@mail.com";

		Account account1 = accountRepository.save(new Account(professor1Mail, defaultPasswordSha3, Role.Professor));
		professor1 = professorRepository.save(new Professor(account1.getId(), "Professor 1 name", "Professor 1 surname"));
		Account account2 = accountRepository.save(new Account(professor2Mail, defaultPasswordSha3, Role.Professor));
		professorRepository.save(new Professor(account2.getId(), "Professor 2 name", "Professor 2 surname"));
		Account account3 = accountRepository.save(new Account(student1Mail, defaultPasswordSha3, Role.Student));
		studentRepository.save(new Student(account3.getId(), "Student 2 name", "Student 2 surname"));
		Account account4 = accountRepository.save(new Account(student2Mail, defaultPasswordSha3, Role.Student));
		studentRepository.save(new Student(account4.getId(), "Student 3 name", "Student 3 surname"));
		Account account5 = accountRepository.save(new Account(student3Mail, defaultPasswordSha3, Role.Student));
		studentRepository.save(new Student(account5.getId(), "Student 4 name", "Student 4 surname"));

		Account account = accountRepository.save(new Account(studentUsername, defaultPasswordSha3, Role.Student));
		student = studentRepository.save(new Student(null, account.getId(), "My name", "My surname", account));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	void getProfile() {
		Student result = studentService.getProfile();
		assertTrue(studentRepository.findById(student.getId()).isPresent());
		assertEquals(studentRepository.findById(student.getId()).get(), result);
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void changePassword() {
		studentService.changePassword(defaultPassword, defaultPassword + "4");
		assertTrue(studentRepository.findById(student.getId()).isPresent());
		assertEquals(studentRepository.findById(student.getId()).get().getAccount().getPassword(), DigestUtils.sha3_256Hex(defaultPassword + "4"));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getStudents() {
		Page<Student> studentPage = studentRepository.findAll(PageRequest.of(0, 10));
		assertEquals(studentPage, studentService.getStudents(0, 10));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getStudent() {
		Assertions.assertEquals(student, studentService.getStudent(student.getId()));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getGroups() {
		groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		assertEquals(1, groupRepository.count());
		Page<Group> groupPage = groupRepository.findAllByStudentIdAndDeletedFalse(student.getId(), PageRequest.of(0, 10));
		log.warn(String.valueOf(groupPage));
		assertEquals(1, groupPage.getNumberOfElements());
		assertEquals(groupPage, studentService.getGroups(0, 10));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getGroup() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		assertEquals(1, groupRepository.count());
		Assertions.assertEquals(group, studentService.getGroup(group.getId()));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void createGroup() {
		Group group = studentService.createGroup(professor1.getId(), "Test Group");
		assertEquals(1, groupRepository.count());
		assertEquals("Test Group", group.getName());
		assertEquals(professor1.getId(), group.getProfessorId());
		assertEquals(student.getId(), group.getAdminId());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void updateGroup() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		assertEquals(1, groupRepository.count());
		Group groupUpdated = studentService.updateGroup(group.getId(), "Test Group Updated");
		assertEquals(1, groupRepository.count());
		assertTrue(groupRepository.findById(groupUpdated.getId()).isPresent());
		assertEquals("Test Group Updated", groupRepository.findById(groupUpdated.getId()).get().getName());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void deleteGroup() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		assertEquals(1, groupRepository.count());
		assertTrue(groupRepository.findById(group.getId()).isPresent());
		assertFalse(groupRepository.findById(group.getId()).get().getDeleted());
		studentService.deleteGroup(group.getId());
		assertEquals(1, groupRepository.count());
		assertTrue(groupRepository.findById(group.getId()).isPresent());
		assertTrue(groupRepository.findById(group.getId()).get().getDeleted());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void joinGroup() {
		Account account = accountRepository.save(new Account("student100@mail.com", defaultPasswordSha3, Role.Student));
		Student admin = studentRepository.save(new Student(account.getId(), "Student name", "Student surname"));

		Group group = groupRepository.save(new Group(professor1.getId(), admin.getId(), "Test Group"));
		assertEquals(1, groupRepository.count());
		assertEquals(0, fellowStudentRepository.count());
		studentService.joinGroup(group.getId());
		assertEquals(1, groupRepository.count());
		assertEquals(1, fellowStudentRepository.count());
		assertThrows(IllegalArgumentException.class, () -> studentService.joinGroup(group.getId()), "You are already in this group");
		assertFalse(fellowStudentRepository.findAll().get(0).getDeleted());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void leaveGroup() {
		Account account = accountRepository.save(new Account("student100@mail.com", defaultPasswordSha3, Role.Student));
		Student admin = studentRepository.save(new Student(account.getId(), "Student name", "Student surname"));

		Group group = groupRepository.save(new Group(professor1.getId(), admin.getId(), "Test Group"));
		studentService.joinGroup(group.getId());
		assertEquals(1, groupRepository.count());
		assertEquals(1, fellowStudentRepository.count());
		assertFalse(fellowStudentRepository.findAll().get(0).getDeleted());
		studentService.leaveGroup(group.getId());
		assertEquals(1, groupRepository.count());
		assertEquals(1, fellowStudentRepository.count());
		assertTrue(fellowStudentRepository.findAll().get(0).getDeleted());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void removeStudentFromGroup() {
		Account account = accountRepository.save(new Account("student100@mail.com", defaultPasswordSha3, Role.Student));
		Student newStudent = studentRepository.save(new Student(account.getId(), "Student name", "Student surname"));

		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		assertEquals(1, groupRepository.count());
		assertEquals(0, fellowStudentRepository.count());
		FellowStudent fellowStudent = fellowStudentRepository.save(new FellowStudent(newStudent.getId(), group.getId()));
		assertEquals(1, fellowStudentRepository.count());
		assertTrue(fellowStudentRepository.findById(fellowStudent.getId()).isPresent());
		assertFalse(fellowStudentRepository.findById(fellowStudent.getId()).get().getDeleted());
		studentService.removeStudentFromGroup(group.getId(), newStudent.getId());
		assertEquals(1, groupRepository.count());
		assertEquals(1, fellowStudentRepository.count());
		assertTrue(fellowStudentRepository.findById(fellowStudent.getId()).isPresent());
		assertTrue(fellowStudentRepository.findById(fellowStudent.getId()).get().getDeleted());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getFiles() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		fileRepository.saveAll(List.of(
				new File(group.getId(), "Exam 03/10 - 1", UUID.randomUUID().toString()),
				new File(group.getId(), "Exam 03/10 - 2", UUID.randomUUID().toString()),
				new File(group.getId(), "Exam 03/10 - 3", UUID.randomUUID().toString()),
				new File(group.getId(), "Exam 03/10 - 4", UUID.randomUUID().toString()),
				new File(group.getId(), "Exam 03/10 - 5", UUID.randomUUID().toString())
		));
		Page<File> filePage = fileRepository.findAllByGroupIdAndDeletedFalseOrderByCreatedOnDesc(group.getId(), PageRequest.of(0, 10));
		assertEquals(filePage, studentService.getFiles(group.getId(), 0, 10));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getFile() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		File file = fileRepository.save(new File(group.getId(), "Exam 03/10", UUID.randomUUID().toString()));
		assertEquals(1, fileRepository.count());
		Assertions.assertEquals(file, studentService.getFile(file.getId()));
		assertEquals(1, fileRepository.count());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void putFile() {
		System.out.println(uploadDir);
		assertTrue(Files.isWritable(Paths.get(uploadDir).toAbsolutePath()));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void deleteFile() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		File file = fileRepository.save(new File(group.getId(), "Exam 03/10", UUID.randomUUID().toString()));
		assertEquals(1, fileRepository.count());
		assertTrue(fileRepository.findById(file.getId()).isPresent());
		assertFalse(fileRepository.findById(file.getId()).get().getDeleted());
		studentService.deleteFile(file.getId());
		assertEquals(1, fileRepository.count());
		assertTrue(fileRepository.findById(file.getId()).isPresent());
		assertTrue(fileRepository.findById(file.getId()).get().getDeleted());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getProfessors() {
		Page<Professor> professorPage = professorRepository.findAll(PageRequest.of(0, 10));
		assertEquals(professorPage, studentService.getProfessors(0, 10));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getProfessor() {
		Assertions.assertEquals(professor1, studentService.getProfessor(professor1.getId()));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getDiscussions() {
		discussionRepository.saveAll(List.of(
				new Discussion(professor1.getId(), "Exam 03/10", now.plusDays(1)),
				new Discussion(professor1.getId(), "Exam 04/10", now.plusDays(2)),
				new Discussion(professor1.getId(), "Exam 05/10", now.plusDays(3)),
				new Discussion(professor1.getId(), "Exam 06/10", now.plusDays(4)),
				new Discussion(professor1.getId(), "Exam 07/10", now.plusDays(5))
		));
		Page<Discussion> discussionPage = discussionRepository.findAllByProfessorIdAndDeletedFalse(professor1.getId(), PageRequest.of(0, 10));
		assertEquals(discussionPage, studentService.getDiscussions(professor1.getId(), 0, 10));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getDiscussion() {
		Discussion discussion = discussionRepository.save(new Discussion(professor1.getId(), "Exam 03/10", now.plusDays(1)));
		assertEquals(1, discussionRepository.count());
		Assertions.assertEquals(discussion, studentService.getDiscussion(discussion.getId()));
		assertEquals(1, discussionRepository.count());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getReservations() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		discussionRepository.saveAll(List.of(
				new Discussion(professor1.getId(), "Exam 03/10", now.plusDays(1)),
				new Discussion(professor1.getId(), "Exam 04/10", now.plusDays(2)),
				new Discussion(professor1.getId(), "Exam 05/10", now.plusDays(3)),
				new Discussion(professor1.getId(), "Exam 06/10", now.plusDays(4)),
				new Discussion(professor1.getId(), "Exam 07/10", now.plusDays(5))
		)).forEach(discussion -> reservationRepository.save(new Reservation(group.getId(), discussion.getId())));
		Page<Reservation> reservationPage = reservationRepository.findAllByGroupIdAndDeletedFalse(group.getId(), PageRequest.of(0, 10));
		assertEquals(reservationPage, studentService.getReservations(group.getId(), 0, 10));
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void getReservation() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		discussionRepository.saveAll(List.of(
				new Discussion(professor1.getId(), "Exam 03/10", now.plusDays(1)),
				new Discussion(professor1.getId(), "Exam 04/10", now.plusDays(2)),
				new Discussion(professor1.getId(), "Exam 05/10", now.plusDays(3)),
				new Discussion(professor1.getId(), "Exam 06/10", now.plusDays(4)),
				new Discussion(professor1.getId(), "Exam 07/10", now.plusDays(5))
		));
		Discussion discussion = discussionRepository.findAll().get(0);
		Reservation reservation = reservationRepository.save(new Reservation(group.getId(), discussion.getId()));
		assertEquals(1, reservationRepository.count());
		Assertions.assertEquals(reservation, studentService.getReservation(reservation.getId()));
		assertEquals(1, reservationRepository.count());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void createReservation() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		Discussion discussion = discussionRepository.save(new Discussion(professor1.getId(), "Exam 03/10", now.plusDays(1)));
		Reservation reservation = studentService.createReservation(group.getId(), discussion.getId());
		assertEquals(1, reservationRepository.count());
		assertTrue(reservationRepository.findById(reservation.getId()).isPresent());
		assertFalse(reservationRepository.findById(reservation.getId()).get().getDeleted());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void updateReservation() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		Discussion discussion = discussionRepository.save(new Discussion( professor1.getId(), "Exam 03/10", now.plusDays(1)));
		Reservation reservation = studentService.createReservation(group.getId(), discussion.getId());
		assertEquals(1, reservationRepository.count());
		Discussion newDiscussion = discussionRepository.save(new Discussion(professor1.getId(), "Exam 04/10", now.plusDays(2)));
		Reservation reservationUpdated = studentService.updateReservation(reservation.getId(), newDiscussion.getId());
		assertEquals(1, reservationRepository.count());
		assertTrue(reservationRepository.findById(reservationUpdated.getId()).isPresent());
		assertFalse(reservationRepository.findById(reservationUpdated.getId()).get().getDeleted());
	}

	@Test
	@WithMockUser(username = studentUsername, password = defaultPasswordSha3, authorities = {"Student"})
	public void deleteReservation() {
		Group group = groupRepository.save(new Group(professor1.getId(), student.getId(), "Test Group"));
		Discussion discussion = discussionRepository.save(new Discussion(professor1.getId(), "Exam 03/10", now.plusDays(1)));
		Reservation reservation = studentService.createReservation(group.getId(), discussion.getId());
		assertEquals(1, reservationRepository.count());
		studentService.deleteReservation(reservation.getId());
		assertEquals(1, reservationRepository.count());
		assertTrue(reservationRepository.findById(reservation.getId()).isPresent());
		assertTrue(reservationRepository.findById(reservation.getId()).get().getDeleted());
	}

}
