package com.lexdata.contrats.repository;

import com.lexdata.contrats.models.ContractModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractModelRepository extends JpaRepository<ContractModel, Long> {
    List<ContractModel> findByCategorieAndActifTrue(String categorie);
}
