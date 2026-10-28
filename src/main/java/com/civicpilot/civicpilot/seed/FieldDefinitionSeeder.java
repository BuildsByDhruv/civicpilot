package com.civicpilot.civicpilot.seed;   // use the same package as SchemeSeeder

import com.civicpilot.civicpilot.model.FieldDefinition;
import com.civicpilot.civicpilot.model.ValueType;
import com.civicpilot.civicpilot.repository.FieldDefinitionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "civicpilot.seed.enabled", havingValue = "true")
public class FieldDefinitionSeeder implements CommandLineRunner {

    private final FieldDefinitionRepository repo;

    public FieldDefinitionSeeder(FieldDefinitionRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> lines = Files.readAllLines(Path.of("data/fields.csv"));
        int count = 0;
        for (String line : lines.subList(1, lines.size())) {   // skip header
            if (line.isBlank()) continue;
            List<String> c = parseCsvLine(line);

            FieldDefinition f = new FieldDefinition();
            f.setFieldKey(c.get(0));
            f.setValueType(ValueType.valueOf(c.get(1)));
            f.setAllowedValues(c.get(2).isEmpty() ? null : c.get(2));
            f.setQuestionText(c.get(3));
            f.setMinValue(c.get(4).isEmpty() ? null : Double.valueOf(c.get(4)));
            f.setMaxValue(c.get(5).isEmpty() ? null : Double.valueOf(c.get(5)));

            repo.save(f);   // id is field_key, so re-running updates instead of duplicating
            count++;
        }
        System.out.println("FieldDefinitionSeeder: loaded " + count + " fields");
    }

    // Handles quoted values with commas inside, e.g. "Hello, world"
    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); i++;          // escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString()); cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }
}