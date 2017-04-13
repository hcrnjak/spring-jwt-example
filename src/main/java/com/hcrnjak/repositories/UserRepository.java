package com.hcrnjak.repositories;

import org.springframework.data.repository.CrudRepository;

import com.hcrnjak.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByUsername(String username);
}
