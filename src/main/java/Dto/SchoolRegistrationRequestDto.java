package Dto;

import lombok.Data;

@Data
public class SchoolRegistrationRequestDto {
    private String schoolName;
    private String ownerFullName;
    private String phone;
    private String email;
    private String wilaya;
    private String commune;
    private String address;
    private String password;
   
}
