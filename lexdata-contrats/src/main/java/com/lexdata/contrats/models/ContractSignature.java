package com.lexdata.contrats.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_signatures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    private String signerEmail;
    private String signerName;
    private String signerRole; // ex: EMPLOYEUR, LOCATAIRE

    @Builder.Default
    private boolean signed = false;
    
    private LocalDateTime dateSignature;
    
    private String signatureIp;
    private String otpCode; // Pour simulation signature simple

    @CreationTimestamp
    private LocalDateTime dateCreation;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }
    public String getSignerEmail() { return signerEmail; }
    public void setSignerEmail(String signerEmail) { this.signerEmail = signerEmail; }
    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    public String getSignerRole() { return signerRole; }
    public void setSignerRole(String signerRole) { this.signerRole = signerRole; }
    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }
    public LocalDateTime getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDateTime dateSignature) { this.dateSignature = dateSignature; }
    public String getSignatureIp() { return signatureIp; }
    public void setSignatureIp(String signatureIp) { this.signatureIp = signatureIp; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
