package com.civicpilot.civicpilot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "required_document")
@Getter
@Setter
@NoArgsConstructor
public class RequiredDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id")
    private Scheme scheme;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean mandatory = true;
}