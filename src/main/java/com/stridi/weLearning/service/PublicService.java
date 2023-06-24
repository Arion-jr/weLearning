package com.stridi.weLearning.service;

import com.stridi.weLearning.utils.object.RegisterGeneric;

public interface PublicService {
    String register(RegisterGeneric data);

    String loginStudent(String username, String password);

    String loginProfessor(String username, String password);

    void resetStudent(String username);

    void resetProfessor(String username);
}
