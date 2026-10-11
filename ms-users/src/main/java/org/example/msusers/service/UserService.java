package org.example.msusers.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.msusers.dto.entry.EntryUserDTO;
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


    public UserDTO createUser(EntryUserDTO userDTO) {
        if (repository.existsByUsername(userDTO.getUsername()) || repository.existsByMail(userDTO.getMail())) {
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
}
