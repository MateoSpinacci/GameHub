package org.example.msusers.dto.entry;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateUserDTO {
    @NonNull
    String username;
    @NonNull
    @Email
    String email;
    @NonNull
    String password;
}
