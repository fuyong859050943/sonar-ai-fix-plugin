/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.fix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FixApplier 单元测试
 */
class FixApplierTest {

    private FixApplier fixApplier;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fixApplier = new FixApplier();
    }

    @Test
    void testApplyFix_Success() throws Exception {
        // 创建测试文件
        Path testFile = tempDir.resolve("Test.java");
        String originalCode = "public class Test {\n    public void test() {\n        System.out.println(\"Hello\");\n    }\n}";
        Files.writeString(testFile, originalCode);

        String fixedCode = "public class Test {\n    public void test() {\n        // Fixed\n        System.out.println(\"Hello\");\n    }\n}";

        // 应用修复
        FixApplier.ApplyResult result = fixApplier.applyFix(testFile, originalCode, fixedCode);

        // 验证
        assertTrue(result.isSuccess());
        assertNotNull(result.getFixId());
        assertEquals(testFile.toString(), result.getFilePath());

        // 验证文件内容已更新
        String actualContent = Files.readString(testFile);
        assertEquals(fixedCode, actualContent);
    }

    @Test
    void testApplyFix_FileNotFound() {
        Path nonExistentFile = tempDir.resolve("NonExistent.java");
        
        FixApplier.ApplyResult result = fixApplier.applyFix(
            nonExistentFile, 
            "original", 
            "fixed"
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("File not found"));
    }

    @Test
    void testApplyFix_CodeMismatch() throws Exception {
        Path testFile = tempDir.resolve("Test.java");
        Files.writeString(testFile, "actual content");

        FixApplier.ApplyResult result = fixApplier.applyFix(
            testFile, 
            "expected content", 
            "fixed"
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("modified"));
    }

    @Test
    void testApplyFixToLines() throws Exception {
        Path testFile = tempDir.resolve("Test.java");
        String content = "line1\nline2\nline3\nline4\nline5";
        Files.writeString(testFile, content);

        FixApplier.ApplyResult result = fixApplier.applyFixToLines(
            testFile, 
            2, 3, 
            "newLine2\nnewLine3"
        );

        assertTrue(result.isSuccess());
        
        String newContent = Files.readString(testFile);
        assertTrue(newContent.contains("line1"));
        assertTrue(newContent.contains("newLine2"));
        assertTrue(newContent.contains("newLine3"));
        assertTrue(newContent.contains("line4"));
    }

    @Test
    void testApplyFixToLines_InvalidRange() throws Exception {
        Path testFile = tempDir.resolve("Test.java");
        Files.writeString(testFile, "line1\nline2\nline3");

        FixApplier.ApplyResult result = fixApplier.applyFixToLines(
            testFile, 
            5, 10,  // 超出范围
            "newCode"
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Invalid line range"));
    }

    @Test
    void testPreviewFix() {
        String originalCode = "line1\nline2\nline3";
        String fixedCode = "line1\nmodified\nline3";

        FixApplier.DiffPreview preview = fixApplier.previewFix(originalCode, fixedCode);

        assertNotNull(preview.getDiff());
        assertEquals(3, preview.getOriginalLines());
        assertEquals(3, preview.getFixedLines());
        assertTrue(preview.getDiff().contains("-"));
        assertTrue(preview.getDiff().contains("+"));
    }

    @Test
    void testPreviewFix_AddLines() {
        String originalCode = "line1\nline2";
        String fixedCode = "line1\nline2\nline3";

        FixApplier.DiffPreview preview = fixApplier.previewFix(originalCode, fixedCode);

        assertEquals(2, preview.getOriginalLines());
        assertEquals(3, preview.getFixedLines());
    }

    @Test
    void testRollbackFix() throws Exception {
        // 先应用修复
        Path testFile = tempDir.resolve("Test.java");
        String originalCode = "original";
        String fixedCode = "fixed";
        Files.writeString(testFile, originalCode);

        FixApplier.ApplyResult applyResult = fixApplier.applyFix(testFile, originalCode, fixedCode);
        assertTrue(applyResult.isSuccess());

        // 验证已修改
        assertEquals(fixedCode, Files.readString(testFile));

        // 回滚
        FixApplier.ApplyResult rollbackResult = fixApplier.rollbackFix(applyResult.getFixId());
        assertTrue(rollbackResult.isSuccess());

        // 验证已恢复
        assertEquals(originalCode, Files.readString(testFile));
    }

    @Test
    void testRollbackFix_NotFound() {
        FixApplier.ApplyResult result = fixApplier.rollbackFix("non-existent-fix-id");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Fix not found"));
    }

    @Test
    void testGetAppliedFix() throws Exception {
        Path testFile = tempDir.resolve("Test.java");
        String originalCode = "original";
        Files.writeString(testFile, originalCode);

        FixApplier.ApplyResult result = fixApplier.applyFix(testFile, originalCode, "fixed");
        
        FixApplier.AppliedFix appliedFix = fixApplier.getAppliedFix(result.getFixId());
        
        assertNotNull(appliedFix);
        assertEquals(result.getFixId(), appliedFix.getFixId());
        assertEquals(originalCode, appliedFix.getOriginalCode());
        assertEquals("fixed", appliedFix.getFixedCode());
        assertNotNull(appliedFix.getAppliedAt());
    }
}
