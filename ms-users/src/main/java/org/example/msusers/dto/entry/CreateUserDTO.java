package org.example.msusers.dto.entry;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateUserDTO {
    @NonNull
    @NotBlank
    String username;
    @NonNull
    @NotBlank
    @Email
    String email;
    @NonNull
    @NotBlank
    String password;
}
