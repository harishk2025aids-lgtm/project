package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByDepartment_DepartmentIdAndFiscalYear(Long departmentId, Integer fiscalYear);
    List<Budget> findByFiscalYearAndFiscalMonth(Integer fiscalYear, Integer fiscalMonth);
}
