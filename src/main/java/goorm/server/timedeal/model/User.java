package goorm.server.timedeal.model;

import goorm.server.timedeal.model.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true)
    @NotNull(message = "Login ID cannot be null")
    private String loginId;
    @Column(name = "username", nullable = false)
    @NotNull(message = "Username cannot be null")
    private String username;
    @Column(name = "password", nullable = false)
    @NotNull(message = "Password cannot be null")
    private String password;
    @Column(name = "email", unique = true, nullable = false)
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @NotNull(message = "Email cannot be null")
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    public String getUsername() {
        return username;
    }
}
