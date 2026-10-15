package com.civicpilot.civicpilot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "field_definition")
@Getter
@Setter
@NoArgsConstructor
public class FieldDefinition {

    // The key itself is the primary key, e.g. "age", "is_pregnant"
    @Id
    @Column(name = "field_key")
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    private ValueType valueType;

    // The question shown to the user in the intake form
    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    private String questionText;

    // Only for CHOICE fields, values separated by |, e.g. "urban|rural"
    @Column(name = "allowed_values", columnDefinition = "text")
    private String allowedValues;
}