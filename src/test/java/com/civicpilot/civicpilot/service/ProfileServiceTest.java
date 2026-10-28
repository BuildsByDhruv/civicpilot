package com.civicpilot.civicpilot.service;

import com.civicpilot.civicpilot.model.ValueType;
import com.civicpilot.civicpilot.model.FieldDefinition;
import com.civicpilot.civicpilot.model.UserProfile;
import com.civicpilot.civicpilot.repository.FieldDefinitionRepository;
import com.civicpilot.civicpilot.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock FieldDefinitionRepository fields;
    @Mock UserProfileRepository profiles;

    ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService(fields, profiles);
        // save() just returns whatever it is given
        lenient().when(profiles.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // a small set of fake field definitions for every test to use
        lenient().when(fields.findAll()).thenReturn(List.of(
                field("age", ValueType.NUMBER, null, 0.0, 120.0),
                field("is_student", ValueType.BOOLEAN, null, null, null),
                field("gender", ValueType.CHOICE, "male|female|other", null, null)
        ));
    }

    // ALL guesses about FieldDefinition live here. Adjust to match your entity.
    private FieldDefinition field(String key, ValueType type,
                                  String allowed, Double min, Double max) {
        FieldDefinition f = new FieldDefinition();
        f.setFieldKey(key);
        f.setValueType(type);
        f.setAllowedValues(allowed);
        f.setMinValue(min);
        f.setMaxValue(max);
        return f;
    }
        // Runs create(), expects a validation failure, returns the error list
    private List<String> errorsFor(Map<String, Object> input) {
        ProfileValidationException ex =
                assertThrows(ProfileValidationException.class, () -> service.create(input));
        verify(profiles, never()).save(any());   // an invalid profile must never be saved
        return ex.getErrors();
    }

        @Test
    void nullValue_isTreatedAsSkippedQuestion() {
        Map<String, Object> input = new HashMap<>();   // Map.of can't hold null
        input.put("age", null);
        input.put("is_student", true);

        UserProfile saved = service.create(input);

        assertFalse(saved.getAnswers().containsKey("age"));
        assertEquals("true", saved.getAnswers().get("is_student"));
    }

    @Test
    void blankValue_isTreatedAsSkippedQuestion() {
        UserProfile saved = service.create(Map.of("age", "   "));

        assertFalse(saved.getAnswers().containsKey("age"));
    }

    @Test
    void surroundingWhitespace_isTrimmed() {
        UserProfile saved = service.create(Map.of("gender", "  female "));

        assertEquals("female", saved.getAnswers().get("gender"));
    }

    @Test
    void boundaryValues_areAccepted() {
        assertEquals("0", service.create(Map.of("age", 0)).getAnswers().get("age"));
        assertEquals("120", service.create(Map.of("age", 120)).getAnswers().get("age"));
    }

    @Test
    void emptyMap_isAcceptedAsFullySkippedProfile() {
        UserProfile saved = service.create(Map.of());

        assertTrue(saved.getAnswers().isEmpty());
    }

    @Test
    void nullBody_isRejected() {
        assertEquals(List.of("answers is required"), errorsFor(null));
    }

    @Test
    void unknownField_isRejected() {
        assertEquals(List.of("Unknown field: height"), errorsFor(Map.of("height", 170)));
    }

    @Test
    void numberAboveMax_isRejected() {
        List<String> errors = errorsFor(Map.of("age", 150));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).startsWith("age: must be at most"));
    }

    @Test
    void numberBelowMin_isRejected() {
        List<String> errors = errorsFor(Map.of("age", -5));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).startsWith("age: must be at least"));
    }

    @Test
    void nonNumericText_inNumberField_isRejected() {
        assertEquals(List.of("age: must be a number"), errorsFor(Map.of("age", "abc")));
    }

    @Test
    void invalidBoolean_isRejected() {
        assertEquals(List.of("is_student: must be true or false"),
                errorsFor(Map.of("is_student", "yes")));
    }

    @Test
    void choiceNotInAllowedValues_isRejected() {
        assertEquals(List.of("gender: must be one of [male, female, other]"),
                errorsFor(Map.of("gender", "robot")));
    }

    @Test
    void nestedValue_isRejected() {
        assertEquals(List.of("age: must be a single value"),
                errorsFor(Map.of("age", List.of(1, 2))));
    }

    @Test
    void allErrorsAreReported_notJustTheFirst() {
        List<String> errors = errorsFor(Map.of(
                "age", 150,
                "is_student", "yes",
                "height", 170
        ));
        assertEquals(3, errors.size());
    }

    @Test
    void validProfile_isSavedWithCleanedAnswers() {
        UserProfile saved = service.create(Map.of(
                "age", 25,
                "is_student", true,
                "gender", "female"
        ));

        assertEquals("25", saved.getAnswers().get("age"));
        assertEquals("true", saved.getAnswers().get("is_student"));
        assertEquals("female", saved.getAnswers().get("gender"));
    }
}