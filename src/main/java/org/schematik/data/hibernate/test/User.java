package org.schematik.data.hibernate.test;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String username;

    private String password;

    private LocalDate registerDate;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Permission> permissions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDate registerDate) {
        this.registerDate = registerDate;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public String toString() {
        String permissionsString = "[";
        permissionsString += permissions.stream().map(Permission::toString).collect(Collectors.joining(", "));
        permissionsString += "]";
        return "{" +
                "id: " + id + ", " +
                "username: " + username + ", " +
                "password: " + password + ", " +
                "registerDate: " + registerDate.toString() + ", " +
                "permissions: " + permissionsString +
                "}";
    }
}
