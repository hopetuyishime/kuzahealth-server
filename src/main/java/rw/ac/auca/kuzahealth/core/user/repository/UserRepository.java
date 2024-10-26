package rw.ac.auca.kuzahealth.core.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import rw.ac.auca.kuzahealth.core.user.entity.User;

public interface  UserRepository extends JpaRepository<User, UUID> {
boolean existsByEmail(String email);
}
