package com.lexdata.user.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String telephone;
}
