package com.example.chemlearn.entity;

import com.example.chemlearn.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "users_username_key", columnNames = {"username"}),
        @UniqueConstraint(name = "users_email_key", columnNames = {"email"})
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role",  discriminatorType = DiscriminatorType.STRING)
public abstract class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 100)
    @NotNull
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "role",insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- BẮT ĐẦU ĐOẠN CODE CẦN THÊM ---

    // 1. Hàm quan trọng nhất: Khai báo quyền hạn
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Giả sử em có biến 'role' là Enum hoặc String trong class này
        // Nếu role của em là String: return List.of(new SimpleGrantedAuthority(this.role));
        if (this.role == null) {
            return List.of();
        }
        // Nếu role là Enum:
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    // 2. Mật khẩu: Spring hỏi "Mật khẩu đâu?", em chỉ vào biến password của em
    @Override
    public String getPassword() {
        return this.password;
    }

    // 3. Tên đăng nhập: Spring hỏi "Username đâu?", em chỉ vào biến username
    @Override
    public String getUsername() {
        return this.username;
    }

    // 4. Các câu hỏi thủ tục (Trả về true hết để nick luôn dùng được)
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // --- KẾT THÚC ĐOẠN CODE CẦN THÊM ---

}