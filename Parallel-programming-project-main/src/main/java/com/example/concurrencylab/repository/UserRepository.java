package com.example.concurrencylab.repository;

import com.example.concurrencylab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}