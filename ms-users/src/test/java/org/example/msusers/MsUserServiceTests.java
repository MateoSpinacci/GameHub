package org.example.msusers;

import org.example.msusers.dto.entry.CreateUserDTO;
import org.example.msusers.dto.entry.PatchUserDTO;
import org.example.msusers.dto.exit.UserDTO;
import org.example.msusers.model.User;
import org.example.msusers.repository.UserRepository;
import org.example.msusers.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MsUserServiceTests {
    @Mock
    UserRepository repository;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UserService service;

    @Test
    public void createUserCorrectly() {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "Mateo",
                "mateo@example.com",
                "Example123"
        );

        UserDTO created = new UserDTO(
                1L,
                "Mateo",
                "mateo@example.com"
        );

        User userSaved = new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        );

        when(repository.existsByUsername("Mateo")).thenReturn(false);
        when(repository.existsByEmail("mateo@example.com")).thenReturn(false);
        when(encoder.encode("Example123")).thenReturn("Example123Hash");
        when(repository.save(any(User.class))).thenReturn(userSaved);

        UserDTO response = service.createUser(createUserDTO);

        assertAll(
                () -> assertEquals(created.getId(), response.getId()),
                () -> assertEquals(created.getUsername(), response.getUsername()),
                () -> assertEquals(created.getEmail(), response.getEmail())
        );

        verify(repository).save(argThat(user -> user.getPasswordHash().equals("Example123Hash")));
        verify(encoder).encode("Example123");
    }

    @Test
    public void createDuplicatedUsernameUser() {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "Mateo",
                "mateo@example.com",
                "Example123"
        );

        when(repository.existsByUsername("Mateo")).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.createUser(createUserDTO));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any(User.class));
        verify(repository, never()).existsByEmail(anyString());
        verify(encoder, never()).encode(anyString());
    }

    @Test
    public void createDuplicatedEmailUser() {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "Mateo",
                "mateo@example.com",
                "Example123"
        );

        when(repository.existsByUsername("Mateo")).thenReturn(false);
        when(repository.existsByEmail("mateo@example.com")).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.createUser(createUserDTO));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any(User.class));
        verify(encoder, never()).encode(anyString());
    }

    @Test
    public void getAllUsers() {
        List<User> userList = new ArrayList<>();
        userList.add(new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        ));
        userList.add(new User(
                2L,
                "Neymar",
                "ney10@example.com",
                "NeymarHash"
        ));

        List<UserDTO> userResponses = new ArrayList<>();
        userResponses.add(new UserDTO(
                1L,
                "Mateo",
                "mateo@example.com"
        ));
        userResponses.add(new UserDTO(
                2L,
                "Neymar",
                "ney10@example.com"
        ));

        when(repository.findAll()).thenReturn(userList);

        List<UserDTO> response = service.getAllUsers();

        assertEquals(userResponses.size(), response.size());
        assertAll(
                () -> assertEquals(userResponses.get(0).getId(), response.get(0).getId()),
                () -> assertEquals(userResponses.get(0).getUsername(), response.get(0).getUsername()),
                () -> assertEquals(userResponses.get(0).getEmail(), response.get(0).getEmail()),
                () -> assertEquals(userResponses.get(1).getId(), response.get(1).getId()),
                () -> assertEquals(userResponses.get(1).getUsername(), response.get(1).getUsername()),
                () -> assertEquals(userResponses.get(1).getEmail(), response.get(1).getEmail())
        );
        verify(repository).findAll();
    }

    @Test
    public void getEmptyUsers() {
        when(repository.findAll()).thenReturn(List.of());

        List<UserDTO> response = service.getAllUsers();

        assertTrue(response.isEmpty());
        verify(repository).findAll();
    }

    @Test
    public void getUserById() {
        User user = new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        );

        UserDTO created = new UserDTO(
                1L,
                "Mateo",
                "mateo@example.com"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO response = service.getUser(1L);

        assertAll(
                () -> assertEquals(created.getId(), response.getId()),
                () -> assertEquals(created.getUsername(), response.getUsername()),
                () -> assertEquals(created.getEmail(), response.getEmail())
        );
        verify(repository).findById(1L);
    }

    @Test
    public void getNonexistentUserById() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.getUser(1L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository).findById(1L);
    }

    @Test
    public void updateUserUsername() {
        PatchUserDTO patchUserDTO = new PatchUserDTO(
                "Thiago",
                null
        );

        User user = new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        );

        User modifiedUser = new User(
                1L,
                "Thiago",
                "mateo@example.com",
                "Example123Hash"
        );

        UserDTO created = new UserDTO(
                1L,
                "Thiago",
                "mateo@example.com"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByUsername("Thiago")).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(modifiedUser);

        UserDTO response = service.updateUser(1L, patchUserDTO);

        assertAll(
                () -> assertEquals(created.getId(), response.getId()),
                () -> assertEquals(created.getUsername(), response.getUsername()),
                () -> assertEquals(created.getEmail(), response.getEmail())
        );
        verify(repository).findById(1L);
        verify(repository).existsByUsername("Thiago");
        verify(repository).save(any(User.class));
        verify(repository, never()).existsByEmail(anyString());
    }

    @Test
    public void updateUserEmail() {
        PatchUserDTO patchUserDTO = new PatchUserDTO(
                null,
                "updatedEmail@gmail.com"
        );

        User user = new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        );

        User modifiedUser = new User(
                1L,
                "Mateo",
                "updatedEmail@gmail.com",
                "Example123Hash"
        );

        UserDTO created = new UserDTO(
                1L,
                "Mateo",
                "updatedEmail@gmail.com"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByEmail("updatedEmail@gmail.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(modifiedUser);

        UserDTO response = service.updateUser(1L, patchUserDTO);

        assertAll(
                () -> assertEquals(created.getId(), response.getId()),
                () -> assertEquals(created.getUsername(), response.getUsername()),
                () -> assertEquals(created.getEmail(), response.getEmail())
        );
        verify(repository).findById(1L);
        verify(repository).existsByEmail("updatedEmail@gmail.com");
        verify(repository).save(any(User.class));
        verify(repository, never()).existsByUsername(anyString());
    }

    @Test
    public void updateUserBothFields() {
        PatchUserDTO patchUserDTO = new PatchUserDTO(
                "Thiago",
                "updatedEmail@gmail.com"
        );

        User user = new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        );

        User modifiedUser = new User(
                1L,
                "Thiago",
                "updatedEmail@gmail.com",
                "Example123Hash"
        );

        UserDTO created = new UserDTO(
                1L,
                "Thiago",
                "updatedEmail@gmail.com"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByUsername("Thiago")).thenReturn(false);
        when(repository.existsByEmail("updatedEmail@gmail.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(modifiedUser);

        UserDTO response = service.updateUser(1L, patchUserDTO);

        assertAll(
                () -> assertEquals(created.getId(), response.getId()),
                () -> assertEquals(created.getUsername(), response.getUsername()),
                () -> assertEquals(created.getEmail(), response.getEmail())
        );
        verify(repository).findById(1L);
        verify(repository).existsByEmail("updatedEmail@gmail.com");
        verify(repository).save(any(User.class));
        verify(repository).existsByUsername("Thiago");
    }

    @Test
    public void updateUserWithBothNullFields() {
        PatchUserDTO patchUserDTO = new PatchUserDTO(
                null,
                null
        );

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.updateUser(1L, patchUserDTO));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(repository, never()).findById(1L);
        verify(repository, never()).existsByEmail(anyString());
        verify(repository, never()).existsByUsername(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void updateUserUsernameDuplicated() {
        PatchUserDTO patchUserDTO = new PatchUserDTO(
                "Thiago",
                null
        );

        User user = new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByUsername("Thiago")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.updateUser(1L, patchUserDTO));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository).findById(1L);
        verify(repository, never()).existsByEmail(anyString());
        verify(repository).existsByUsername("Thiago");
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void updateUserEmailDuplicated() {
        PatchUserDTO patchUserDTO = new PatchUserDTO(
                null,
                "updatedEmail@gmail.com"
        );

        User user = new User(
                1L,
                "Mateo",
                "mateo@example.com",
                "Example123Hash"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByEmail("updatedEmail@gmail.com")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.updateUser(1L, patchUserDTO));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository).findById(1L);
        verify(repository).existsByEmail("updatedEmail@gmail.com");
        verify(repository, never()).existsByUsername(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void updateNonexistentUser() {
        PatchUserDTO patchUserDTO = new PatchUserDTO(
                null,
                "updatedEmail@gmail.com"
        );

        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.updateUser(1L, patchUserDTO));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository).findById(1L);
        verify(repository, never()).existsByEmail(anyString());
        verify(repository, never()).existsByUsername(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void deleteUser() {
        when(repository.existsById(1L)).thenReturn(true);
        service.deleteUser(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    public void deleteNonexistentUser() {
        when(repository.existsById(1L)).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.deleteUser(1L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(1L);
    }
}
