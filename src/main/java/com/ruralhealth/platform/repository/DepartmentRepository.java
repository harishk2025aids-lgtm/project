package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
