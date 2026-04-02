package com.lexdata.juridique.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Document(indexName = "textes_juridiques")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TexteJuridiqueDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "french")
    private String titre;

    @Field(type = FieldType.Keyword)
    private String referenceOfficielle;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String domaine;

    @Field(type = FieldType.Date)
    private LocalDate dateSignature;

    @Field(type = FieldType.Text, analyzer = "french")
    private String contenu;

    @Field(type = FieldType.Boolean)
    private Boolean estPublie;

    @Field(type = FieldType.Boolean)
    private Boolean estPremium;
}
