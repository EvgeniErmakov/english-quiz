package com.example.englishquiz.repository;

import com.example.englishquiz.domain.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {
}
