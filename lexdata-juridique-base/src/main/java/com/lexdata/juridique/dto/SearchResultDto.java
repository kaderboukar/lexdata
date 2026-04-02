package com.lexdata.juridique.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private Long id;
    private String titre;
    private String referenceOfficielle;
    private String domaine;
    private String type;
    private List<String> highlights;
}
