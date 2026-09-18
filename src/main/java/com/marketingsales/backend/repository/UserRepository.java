package com.marketingsales.backend.repository;

import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.constant.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:region IS NULL OR LOWER(u.region) = LOWER(:region))
              AND (:enabled IS NULL OR u.enabled = :enabled)
            """)
    Page<User> searchUsers(
            @Param("search") String search,
            @Param("role") Role role,
            @Param("region") String region,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );
}
