package com.lexdata.contrats.controllers;

import com.lexdata.contrats.models.Contract;
import com.lexdata.contrats.models.ContractModel;
import com.lexdata.contrats.models.ContractSignature;
import com.lexdata.contrats.payload.ContractInstanceRequest;
import com.lexdata.contrats.repository.ContractModelRepository;
import com.lexdata.contrats.repository.ContractRepository;
import com.lexdata.contrats.services.ContractService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contrats")
public class ContractController {

    private final ContractModelRepository modelRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;

    public ContractController(ContractModelRepository modelRepository, ContractRepository contractRepository, ContractService contractService) {
        this.modelRepository = modelRepository;
        this.contractRepository = contractRepository;
        this.contractService = contractService;
    }

    // --- MODELES ---

    @GetMapping("/models")
    public List<ContractModel> getModels(@RequestParam(required = false) String categorie) {
        if (categorie != null) {
            return modelRepository.findByCategorieAndActifTrue(categorie);
        }
        return modelRepository.findAll();
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<ContractModel> getModel(@PathVariable Long id) {
        return modelRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- INSTANCES ---

    @PostMapping("/instances")
    public ResponseEntity<Contract> createInstance(@Valid @RequestBody ContractInstanceRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        ContractModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new RuntimeException("Modèle non trouvé"));

        String generatedContent = contractService.generateContent(model, request.getFieldValues());

        Contract contract = new Contract();
        contract.setOwnerUsername(username);
        contract.setModel(model);
        contract.setTitre(request.getTitre());
        contract.setGeneratedContent(generatedContent);
        contract.setFieldValues(request.getFieldValues());
        contract.setDateEcheance(request.getDateEcheance());
        contract.setStatus(Contract.ContractStatus.BROUILLON);

        return ResponseEntity.ok(contractRepository.save(contract));
    }

    @GetMapping("/instances")
    public List<Contract> getMyContracts() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Utilisation de la méthode castée si nécessaire, mais Spring Data JPA gère bien.
        return contractRepository.findByOwnerUsername(username);
    }

    // --- SIGNATURES ---

    @PostMapping("/instances/{id}/signatures")
    public ResponseEntity<Contract> addSigner(@PathVariable Long id, @RequestParam String email, @RequestParam String name) {
        return contractRepository.findById(id)
                .map(contract -> {
                    ContractSignature sig = new ContractSignature();
                    sig.setContract(contract);
                    sig.setSignerEmail(email);
                    sig.setSignerName(name);
                    sig.setOtpCode(contractService.generateOtp());
                    
                    contract.getSignatures().add(sig);
                    contract.setStatus(Contract.ContractStatus.EN_ATTENTE_SIGNATURE);
                    return ResponseEntity.ok(contractRepository.save(contract));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/signatures/{sigId}/sign")
    public ResponseEntity<String> sign(@PathVariable Long sigId, @RequestParam String otp, HttpServletRequest request) {
        // En prod, on récupère le contrat via sigId et on vérifie l'OTP
        // Ici simulation simplifiée
        return ResponseEntity.ok("Signature enregistrée avec succès (Simulé).");
    }
}
