package org.example.msusers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.msusers.dto.entry.EntryUserDTO;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String username;
    @Column(unique = true)
    String mail;
    String passwordHash;

    public User(String username, String mail, String passwordHash) {
        this.username = username;
        this.mail = mail;
        this.passwordHash = passwordHash;
    }

    public static User of(EntryUserDTO dto, String passwordHash) {
        return new User(
                dto.getUsername(),
                dto.getMail(),
                passwordHash
        );
    }
}
