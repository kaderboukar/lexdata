package com.lexdata.contrats.services;

import com.lexdata.contrats.models.Contract;
import com.lexdata.contrats.models.ContractModel;
import com.lexdata.contrats.models.ContractSignature;
import com.lexdata.contrats.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public String generateContent(ContractModel model, Map<String, String> values) {
        String content = model.getTemplateContent();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }

    public Contract signContract(Contract contract, String signerEmail, String ip) {
        for (ContractSignature sig : contract.getSignatures()) {
            if (sig.getSignerEmail().equalsIgnoreCase(signerEmail) && !sig.isSigned()) {
                sig.setSigned(true);
                sig.setDateSignature(LocalDateTime.now());
                sig.setSignatureIp(ip);
                break;
            }
        }

        // Vérifier si toutes les signatures sont collectées
        boolean allSigned = contract.getSignatures().stream().allMatch(ContractSignature::isSigned);
        if (allSigned) {
            contract.setStatus(Contract.ContractStatus.SIGNE);
        }

        return contractRepository.save(contract);
    }
    
    public String generateOtp() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
