package com.example.userservicespring.repository;

import com.example.userservicespring.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
// Наследуемся от CrudRepository
public interface UserRepository extends CrudRepository<User, Long> {
}
