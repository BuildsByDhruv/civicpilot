package com.civicpilot.civicpilot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "eligibility_rule")
@Getter
@Setter
@NoArgsConstructor
public class EligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id")
    private Scheme scheme;

    // e.g. age, is_pregnant, occupation
    @Column(nullable = false)
    private String field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Operator operator;

    // Text, because IN lists like the 18 trades are long. Values are separated by |
    @Column(nullable = false, columnDefinition = "text")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType = RuleType.HARD;

    // Stored as-is (pmjay_pathway, beneficiary). Null for most rules.
    @Column(name = "rule_group")
    private String ruleGroup;

    @Column(name = "needs_verification", nullable = false)
    private boolean needsVerification;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "source_note", columnDefinition = "text")
    private String sourceNote;

    @Column(name = "last_verified")
    private LocalDate lastVerified;
}