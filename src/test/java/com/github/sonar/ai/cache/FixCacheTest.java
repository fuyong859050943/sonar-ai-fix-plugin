/*
 * SonarQube AI Fix Plugin - Tests
 * Copyright (C) 2024
 */
package com.github.sonar.ai.cache;

import com.github.sonar.ai.fix.FixSuggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复缓存测试
 */
class FixCacheTest {

    private FixCache fixCache;

    @BeforeEach
    void setUp() {
        fixCache = new FixCache(100, 60);
    }

    @Test
    @DisplayName("Should generate consistent cache key")
    void shouldGenerateConsistentKey() {
        // Given
        String issueKey = "java:S106";
        String codeSnippet = "System.out.println(\"test\");";

        // When
        String key1 = fixCache.generateKey(issueKey, codeSnippet);
        String key2 = fixCache.generateKey(issueKey, codeSnippet);

        // Then
        assertEquals(key1, key2);
        assertTrue(key1.startsWith("fix:"));
    }

    @Test
    @DisplayName("Should generate different keys for different snippets")
    void shouldGenerateDifferentKeysForDifferentSnippets() {
        // Given
        String issueKey = "java:S106";
        String snippet1 = "System.out.println(\"test1\");";
        String snippet2 = "System.out.println(\"test2\");";

        // When
        String key1 = fixCache.generateKey(issueKey, snippet1);
        String key2 = fixCache.generateKey(issueKey, snippet2);

        // Then
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Should cache and retrieve suggestion")
    void shouldCacheAndRetrieve() {
        // Given
        String key = "test-key";
        FixSuggestion suggestion = createTestSuggestion();

        // When
        fixCache.put(key, suggestion);
        Optional<FixSuggestion> retrieved = fixCache.get(key);

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(suggestion.getFixedCode(), retrieved.get().getFixedCode());
    }

    @Test
    @DisplayName("Should return empty for missing key")
    void shouldReturnEmptyForMissingKey() {
        // Given
        String key = "non-existent-key";

        // When
        Optional<FixSuggestion> retrieved = fixCache.get(key);

        // Then
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should invalidate specific key")
    void shouldInvalidateKey() {
        // Given
        String key = "test-key";
        FixSuggestion suggestion = createTestSuggestion();
        fixCache.put(key, suggestion);

        // When
        fixCache.invalidate(key);
        Optional<FixSuggestion> retrieved = fixCache.get(key);

        // Then
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should invalidate all keys")
    void shouldInvalidateAll() {
        // Given
        fixCache.put("key1", createTestSuggestion());
        fixCache.put("key2", createTestSuggestion());
        fixCache.put("key3", createTestSuggestion());

        // When
        fixCache.invalidateAll();

        // Then
        assertEquals(0, fixCache.size());
    }

    @Test
    @DisplayName("Should track cache statistics")
    void shouldTrackStats() {
        // Given
        String key = "test-key";
        FixSuggestion suggestion = createTestSuggestion();

        // When - Put then get (hit)
        fixCache.put(key, suggestion);
        fixCache.get(key);  // Hit
        fixCache.get("missing-key");  // Miss

        // Then
        String stats = fixCache.getStats();
        assertNotNull(stats);
        assertTrue(stats.contains("hitCount=1"));
        assertTrue(stats.contains("missCount=1"));
        assertTrue(fixCache.hitRate() > 0);
    }

    @Test
    @DisplayName("Should respect max size")
    void shouldRespectMaxSize() {
        // Given - Small cache
        FixCache smallCache = new FixCache(3, 60);

        // When - Add more items than max
        smallCache.put("key1", createTestSuggestion());
        smallCache.put("key2", createTestSuggestion());
        smallCache.put("key3", createTestSuggestion());
        smallCache.put("key4", createTestSuggestion());  // Should evict oldest

        // Then
        assertTrue(smallCache.size() <= 3);
    }

    private FixSuggestion createTestSuggestion() {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.setFixedCode("Fixed code");
        suggestion.setExplanation("Test explanation");
        suggestion.setSteps(Arrays.asList("Step 1", "Step 2"));
        suggestion.setSeverity("MAJOR");
        return suggestion;
    }
}
