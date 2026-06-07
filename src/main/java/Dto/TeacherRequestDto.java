package Dto;


import lombok.Data;
@Data
public class TeacherRequestDto {
    private String fullName;
    private String password;
  
    private String email;
    private String specialization;
  
    private String bio;


}
