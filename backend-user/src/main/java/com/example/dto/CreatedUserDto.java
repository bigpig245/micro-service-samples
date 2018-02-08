package com.example.dto;

import com.example.utils.Constants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatedUserDto {
    @NotNull
    String login;
    String firstName;
    String lastName;
    String displayName;
    @NotNull
    @Email(regexp = Constants.EMAIL_PATTERN)
    String email;
    @NotNull
    String password;
}
