package Thay.Application.Employee.Backend.repository;

import Thay.Application.Employee.Backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
