package com.projects.onlinejudge.service;

import com.projects.onlinejudge.dto.UserDTO;

public interface UserService {

    public boolean addUser(UserDTO userDTO);

    public boolean removeUser(String userName);
}
