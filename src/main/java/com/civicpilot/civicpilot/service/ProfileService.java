package com.civicpilot.civicpilot.service;

import com.civicpilot.civicpilot.model.FieldDefinition;
import com.civicpilot.civicpilot.model.UserProfile;
import com.civicpilot.civicpilot.repository.FieldDefinitionRepository;
import com.civicpilot.civicpilot.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final FieldDefinitionRepository fields;
    private final UserProfileRepository profiles;

    public ProfileService(FieldDefinitionRepository fields, UserProfileRepository profiles) {
        this.fields = fields;
        this.profiles = profiles;
    }

    public UserProfile create(Map<String, Object> raw) {
        if (raw == null) {
            throw new ProfileValidationException(List.of("answers is required"));
        }

        Map<String, FieldDefinition> defs = fields.findAll().stream()
                .collect(Collectors.toMap(FieldDefinition::getFieldKey, f -> f));

        List<String> errors = new ArrayList<>();
        Map<String, String> clean = new LinkedHashMap<>();

        for (Map.Entry<String, Object> e : raw.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            FieldDefinition def = defs.get(key);
            if (def == null) {
                errors.add("Unknown field: " + key);
                continue;
            }
            if (value == null) continue;                       // null = skipped question
            if (value instanceof Map || value instanceof List) {
                errors.add(key + ": must be a single value");
                continue;
            }

            String s = value.toString().trim();
            if (s.isEmpty()) continue;                         // blank = skipped question

            String problem = check(def, s);
            if (problem != null) errors.add(key + ": " + problem);
            else clean.put(key, s);
        }

        if (!errors.isEmpty()) {
            throw new ProfileValidationException(errors);
        }

        UserProfile profile = new UserProfile();
        profile.setAnswers(clean);
        return profiles.save(profile);
    }

    // Returns an error message, or null if the value is fine
    private String check(FieldDefinition def, String s) {
        return switch (def.getValueType()) {
            case BOOLEAN -> (s.equals("true") || s.equals("false")) ? null : "must be true or false";
            case NUMBER -> checkNumber(def, s);
            case CHOICE -> {
                List<String> allowed = Arrays.asList(def.getAllowedValues().split("\\|"));
                yield allowed.contains(s) ? null : "must be one of " + allowed;
            }
            case TEXT -> s.length() > 200 ? "too long (max 200 characters)" : null;
        };
    }

    private String checkNumber(FieldDefinition def, String s) {
        BigDecimal n;
        try {
            n = new BigDecimal(s);
        } catch (NumberFormatException ex) {
            return "must be a number";
        }
        if (def.getMinValue() != null && n.compareTo(BigDecimal.valueOf(def.getMinValue())) < 0) {
            return "must be at least " + def.getMinValue();
        }
        if (def.getMaxValue() != null && n.compareTo(BigDecimal.valueOf(def.getMaxValue())) > 0) {
            return "must be at most " + def.getMaxValue();
        }
        return null;
    }
}