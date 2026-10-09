package com.insurance.claimtracker.repository;

import com.insurance.claimtracker.model.Claim;
import com.insurance.claimtracker.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByPolicyId(Long policyId);
}
