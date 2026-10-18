package org.example.mslibrary.repository;

import org.example.mslibrary.model.LibraryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryRepository extends JpaRepository<LibraryEntry,Long> {
    boolean existsByUserIdAndGameId(String userId, Integer gameId);
    List<LibraryEntry> findByUserId(String userId);
    Optional<LibraryEntry> findByIdAndUserId(Long id, String userId);
}
