package com.project.bankomat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.bankomat.entity.enums.PermissionEnum;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(unique = true)
    private String email;
    @JsonIgnore
    private String password;
    private String username;

    @ManyToOne()
    private Bank bank;

    @Builder.Default
    private boolean accountNonExpired=true;
    @Builder.Default
    private boolean accountNonLocked=true;
    @Builder.Default
    private boolean credentialsNonExpired=true;
    @Builder.Default
    private boolean enabled=true;

    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    private Role role;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities=new LinkedHashSet<>();
        for (PermissionEnum permission : role.getPermissions()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(permission.name()));
        }
        return grantedAuthorities;
    }
}
