package com.civicpilot.civicpilot.seed;

import com.civicpilot.civicpilot.model.EligibilityRule;
import com.civicpilot.civicpilot.model.Operator;
import com.civicpilot.civicpilot.model.RuleType;
import com.civicpilot.civicpilot.model.Scheme;
import com.civicpilot.civicpilot.repository.SchemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "civicpilot.seed.enabled", havingValue = "true")
public class EligibilityRuleSeeder implements CommandLineRunner {

    private final SchemeRepository schemeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Path path = Path.of("data/eligibility.csv");
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).setTrim(true).build();

        // group CSV rows by scheme code, keeping file order
        Map<String, List<CSVRecord>> bySchemeCode = new LinkedHashMap<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            for (CSVRecord r : parser) {
                bySchemeCode.computeIfAbsent(r.get("scheme_id"), k -> new ArrayList<>()).add(r);
            }
        }

        int total = 0;
        for (Map.Entry<String, List<CSVRecord>> entry : bySchemeCode.entrySet()) {
            String code = entry.getKey();
            Scheme scheme = schemeRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalStateException(
                            "eligibility.csv refers to unknown scheme code: " + code));

            scheme.getRules().clear();   // orphanRemoval deletes the old rows

            for (CSVRecord r : entry.getValue()) {
                EligibilityRule rule = new EligibilityRule();
                rule.setScheme(scheme);
                rule.setField(r.get("field"));
                rule.setOperator(Operator.valueOf(r.get("operator").toUpperCase()));
                rule.setValue(r.get("value"));
                rule.setRuleType(RuleType.valueOf(r.get("rule_type").toUpperCase()));
                rule.setRuleGroup(blankToNull(r.get("rule_group")));
                rule.setNeedsVerification(Boolean.parseBoolean(r.get("needs_verification")));
                rule.setSourceUrl(blankToNull(r.get("source_url")));
                rule.setSourceNote(blankToNull(r.get("source_note")));
                String verified = r.get("last_verified");
                rule.setLastVerified(verified.isBlank() ? null : LocalDate.parse(verified));

                scheme.getRules().add(rule);
                total++;
            }
            schemeRepository.save(scheme);
        }
        log.info("Rule seed done: {} rules across {} schemes", total, bySchemeCode.size());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}