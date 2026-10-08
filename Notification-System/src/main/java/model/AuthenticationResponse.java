package model;


import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private Long id;
    private String email;
    private String role;
    private String name;
}