package org.example.mslibrary;

import jakarta.persistence.EntityManager;
import org.example.mslibrary.model.GameStatus;
import org.example.mslibrary.model.LibraryEntry;
import org.example.mslibrary.repository.LibraryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

@DataJpaTest
public class MsLibraryRepositoryTests {

    @Autowired
    LibraryRepository repository;

    @Autowired
    private EntityManager entityManager;

    private LibraryEntry createLibraryEntry() {
        LibraryEntry entry = new LibraryEntry();
        entry.setUserId("usuario-123");
        entry.setGameId(22509);
        entry.setStatus(GameStatus.PLAYING);
        entry.setPersonalRating(10);
        entry.setAddedAt(LocalDate.of(2026, 10, 22));
        return entry;
    }

    @Test
    public void findByIdAndUserIdShouldNotReturnAnotherUsersEntry() {
        LibraryEntry entry = createLibraryEntry();

        LibraryEntry savedEntry = repository.save(entry);

        Long id = savedEntry.getId();

        Optional<LibraryEntry> foundEntry = repository.findByIdAndUserId(id, "usuario-456");
        Optional<LibraryEntry> ownerEntry = repository.findByIdAndUserId(id, "usuario-123");

        assertTrue(foundEntry.isEmpty());
        assertTrue(ownerEntry.isPresent());
        assertEquals(id, ownerEntry.orElseThrow().getId());
    }

    @Test
    public void shouldNotAllowDuplicateGameForSameUser() {
        LibraryEntry entry1 = createLibraryEntry();
        LibraryEntry entry2 = createLibraryEntry();

        repository.saveAndFlush(entry1);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(entry2)
        );
    }

    @Test
    public void shouldAllowSameGameForDifferentUsers() {
        LibraryEntry entry1 = createLibraryEntry();
        LibraryEntry entry2 = createLibraryEntry();
        entry2.setUserId("usuario-456");

        LibraryEntry savedEntry1 = repository.saveAndFlush(entry1);
        LibraryEntry savedEntry2 = repository.saveAndFlush(entry2);

        Long id1 = savedEntry1.getId();
        Long id2 = savedEntry2.getId();

        entityManager.clear();

        Optional<LibraryEntry> foundEntry1 = repository.findByIdAndUserId(id1, "usuario-123");
        Optional<LibraryEntry> foundEntry2 = repository.findByIdAndUserId(id2, "usuario-456");

        assertTrue(foundEntry1.isPresent());
        assertTrue(foundEntry2.isPresent());

        assertEquals(22509, foundEntry1.orElseThrow().getGameId());
        assertEquals(22509, foundEntry2.orElseThrow().getGameId());
    }

    @Test
    public void shouldDeleteLibraryEntry() {
        LibraryEntry entry = createLibraryEntry();

        LibraryEntry savedEntry = repository.saveAndFlush(entry);

        Long id = savedEntry.getId();

        repository.delete(savedEntry);
        repository.flush();
        entityManager.clear();

        Optional<LibraryEntry> foundEntry = repository.findByIdAndUserId(id, "usuario-123");

        assertTrue(foundEntry.isEmpty());
    }
}
