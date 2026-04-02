package com.lexdata.contrats.repository;

import com.lexdata.contrats.models.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByOwnerUsername(String username);
    List<Contract> findByStatusAndDateEcheanceBeforeAndRappelEnvoyeFalse(Contract.ContractStatus status, LocalDateTime now);
}
