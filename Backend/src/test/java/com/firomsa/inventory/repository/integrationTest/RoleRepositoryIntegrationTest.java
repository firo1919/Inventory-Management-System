package com.firomsa.inventory.repository.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.repository.RoleRepository;

public class RoleRepositoryIntegrationTest extends AbstractIntegrationTestRepo {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("should find role by name")
    void shouldFindRoleByName() {
        // Arrange
        Role role = new Role();
        role.setName(Roles.EMPLOYEE);
        roleRepository.save(role);
        // Act
        var foundRole = roleRepository.findByName(Roles.EMPLOYEE);
        // Assert
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.get().getName()).isEqualTo(Roles.EMPLOYEE);

    }

}
