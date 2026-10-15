package com.civicpilot.civicpilot.seed;

import org.springframework.core.annotation.Order;
import com.civicpilot.civicpilot.model.ApplyMode;
import com.civicpilot.civicpilot.model.Scheme;
import com.civicpilot.civicpilot.model.SchemeLevel;
import com.civicpilot.civicpilot.repository.SchemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Order(1)
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "civicpilot.seed.enabled", havingValue = "true")
public class SchemeSeeder implements CommandLineRunner {

    private final SchemeRepository schemeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Path path = Path.of("data/schemes.csv");
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).setTrim(true).build();

        int created = 0, updated = 0;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            for (CSVRecord r : parser) {
                String code = r.get("code");
                Scheme scheme = schemeRepository.findByCode(code).orElse(null);
                boolean isNew = (scheme == null);
                if (isNew) {
                    scheme = new Scheme();
                    scheme.setCode(code);
                }

                scheme.setName(r.get("name"));
                scheme.setCategory(blankToNull(r.get("category")));
                scheme.setMinistry(blankToNull(r.get("ministry")));
                scheme.setLevel(SchemeLevel.valueOf(r.get("level").toUpperCase()));
                scheme.setState(blankToNull(r.get("state")));
                scheme.setApplyMode(ApplyMode.valueOf(r.get("apply_mode").toUpperCase()));
                scheme.setSourceUrl(blankToNull(r.get("official_link")));
                String verified = r.get("last_verified");
                scheme.setLastVerified(verified.isBlank() ? null : LocalDate.parse(verified));

                schemeRepository.save(scheme);
                if (isNew) created++; else updated++;
            }
        }
        log.info("Scheme seed done: {} created, {} updated", created, updated);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
