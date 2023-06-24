package com.stridi.weLearning.service.impl;

import com.stridi.weLearning.service.PublicService;
import com.stridi.weLearning.service.authentication.ProfessorAuthenticationService;
import com.stridi.weLearning.service.authentication.StudentAuthenticationService;
import com.stridi.weLearning.utils.object.RegisterGeneric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PublicServiceImpl implements PublicService {
	private final StudentAuthenticationService studentAuthenticationService;
	private final ProfessorAuthenticationService professorAuthenticationService;

	@Override
	public String register(RegisterGeneric data) {
		return studentAuthenticationService.register(data);
	}


	@Override
	public String loginStudent(String username, String password) {
		return studentAuthenticationService.login(username, DigestUtils.sha3_256Hex(password));
	}

	@Override
	public String loginProfessor(String username, String password) {
		return professorAuthenticationService.login(username, DigestUtils.sha3_256Hex(password));
	}


	@Override
	public void resetStudent(String username) {
		studentAuthenticationService.reset(username);
	}

	@Override
	public void resetProfessor(String username) {
		professorAuthenticationService.reset(username);
	}
}
