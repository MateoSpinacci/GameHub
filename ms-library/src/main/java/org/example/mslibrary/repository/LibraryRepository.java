package org.example.mslibrary.repository;

import org.example.mslibrary.model.LibraryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends JpaRepository<LibraryEntry,Long> {
    boolean existsByGameId(Integer gameId);
}
