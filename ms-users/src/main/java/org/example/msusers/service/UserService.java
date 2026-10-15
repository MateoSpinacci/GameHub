package org.example.msusers.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.msusers.dto.entry.CreateUserDTO;
import org.example.msusers.dto.entry.PatchUserDTO;
import org.example.msusers.dto.exit.UserDTO;
import org.example.msusers.model.User;
import org.example.msusers.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserService {

    UserRepository repository;
    PasswordEncoder passwordEncoder;


    public UserDTO createUser(CreateUserDTO userDTO) {
        if (repository.existsByUsername(userDTO.getUsername()) || repository.existsByEmail(userDTO.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con el mismo nombre y/o mail"
            );
        }
        User created = repository.save(User.of(userDTO, passwordEncoder.encode(userDTO.getPassword())));
        return UserDTO.of(created);
    }

    public List<UserDTO> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(UserDTO::of)
                .toList();
    }

    public UserDTO getUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el usuario con el ID dado"
                ));

        return UserDTO.of(user);
    }

    public UserDTO updateUser(Long id, PatchUserDTO patchUserDTO) {
        if (patchUserDTO.getUsername() == null && patchUserDTO.getEmail() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe proporcionar al menos un campo para actualizar");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el usuario con el ID dado"
                ));
        if (patchUserDTO.getUsername() != null) {
            if (repository.existsByUsername(patchUserDTO.getUsername())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe un usuario con el mismo nombre"
                );
            }
            user.setUsername(patchUserDTO.getUsername());
        }
        if (patchUserDTO.getEmail() != null) {
            if (repository.existsByEmail(patchUserDTO.getEmail())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe un usuario con el mismo email"
                );
            }
            user.setEmail(patchUserDTO.getEmail());
        }
        return UserDTO.of(repository.save(user));
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe un usuario con ese ID"
            );
        }
        repository.deleteById(id);
    }
}
