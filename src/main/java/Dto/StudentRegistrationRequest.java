package Dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class StudentRegistrationRequest {
	private String fullName;
    private String email;
    private String Password;
    private String level;
    private String parentName;
    private String parentPhone;
    private LocalDate birthDate;
}
