package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.LeaveRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByEmployeeDepartmentId(Long departmentId);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.id = :employeeId " +
           "AND l.status != com.example.leavemanagement.entity.LeaveStatus.REJECTED " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate " +
           "AND l.id != :excludeId")
    List<LeaveRequest> findOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId
    );
}
