package com.lexdata.auth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "\"roles\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable { // Serializable est vital pour le cache Redis plus tard

    private static final long serialVersionUID = 1L; // Versionning de la classe

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @SequenceGenerator(name = "role_seq", sequenceName = "role_seq", allocationSize = 1)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false) // Ajout de unique=true et nullable=false
    private ERole name;

    // Description utile pour l'admin (ex: "Accès complet aux textes juridiques")
    @Column(length = 100)
    private String description;
}