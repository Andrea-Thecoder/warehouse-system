package it.warehouse.administrator.model;

import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_registration")
public class UserRegistration extends AbstractAuditable {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "keycloak_user_id", unique = true)
    private String keycloakUserId;

    @Column(nullable = false)
    private String fullname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @ManyToMany
    @JoinTable(
            name = "registration_request_role",
            joinColumns = @JoinColumn(name = "registration_request_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<RoleType> requestedRoleType = new ArrayList<>();

}
