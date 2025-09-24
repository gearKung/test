package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.HotelOwner.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findById(Long userId);
}
