package boiz.shop._2BShop.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String userName;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;
    private String fullName;
}
