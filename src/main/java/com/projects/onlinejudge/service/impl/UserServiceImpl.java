package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.domain.User;
import com.projects.onlinejudge.dto.UserDTO;
import com.projects.onlinejudge.repository.UserRepository;
import com.projects.onlinejudge.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public boolean addUser(UserDTO userDTO) {
        User existingUser = userRepository.findUserByUserName(userDTO.getUserName());
        if (Objects.nonNull(existingUser)) {
            // raise custom exception
            return false;
        }
        User user = mapper.map(userDTO, User.class);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean removeUser(String userName) {
        return false;
    }
}
