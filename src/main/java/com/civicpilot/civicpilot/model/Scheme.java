package com.civicpilot.civicpilot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

@Entity
@Table(name = "scheme")
@Getter
@Setter
@NoArgsConstructor
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Matches scheme_id in your CSV, e.g. PMVISH
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String ministry;
    private String category;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchemeLevel level;

    // Only for state schemes, e.g. Haryana. Null for central ones.
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_mode", nullable = false)
    private ApplyMode applyMode;

    // Nullable: some schemes have no official link
    @Column(name = "source_url")
    private String sourceUrl;

    // Nullable: not every scheme has been verified yet
    @Column(name = "last_verified")
    private LocalDate lastVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchemeStatus status = SchemeStatus.DRAFT;

        @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EligibilityRule> rules = new ArrayList<>();

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequiredDocument> documents = new ArrayList<>();
}