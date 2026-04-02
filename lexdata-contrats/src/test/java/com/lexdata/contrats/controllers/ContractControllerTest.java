package com.lexdata.contrats.controllers;

import com.lexdata.contrats.models.Contract;
import com.lexdata.contrats.models.ContractModel;
import com.lexdata.contrats.repository.ContractModelRepository;
import com.lexdata.contrats.repository.ContractRepository;
import com.lexdata.contrats.services.ContractService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractControllerTest {

    @Mock
    private ContractModelRepository modelRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    @Test
    void getModels_ShouldReturnList() {
        // Arrange
        when(modelRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ContractModel> result = contractController.getModels(null);

        // Assert
        assertNotNull(result);
        verify(modelRepository).findAll();
    }

    @Test
    void addSigner_ShouldUpdateContractStatus() {
        // Arrange
        Long contractId = 1L;
        Contract contract = new Contract();
        contract.setId(contractId);
        contract.setStatus(Contract.ContractStatus.BROUILLON);
        contract.setSignatures(new java.util.ArrayList<>());

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractService.generateOtp()).thenReturn("123456");
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<Contract> response = contractController.addSigner(contractId, "test@test.com", "Signer Name");

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(Contract.ContractStatus.EN_ATTENTE_SIGNATURE, response.getBody().getStatus());
        assertEquals(1, response.getBody().getSignatures().size());
        verify(contractRepository).save(contract);
    }
}
