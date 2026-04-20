/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.fix;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 修复应用器
 * 
 * 负责将 AI 生成的修复建议应用到实际代码文件。
 * 支持预览、应用、回滚功能。
 */
public class FixApplier {

    private static final Logger LOG = Loggers.get(FixApplier.class);

    // 存储应用记录，用于回滚
    private final Map<String, AppliedFix> appliedFixes = new ConcurrentHashMap<>();

    /**
     * 应用修复到文件
     * 
     * @param filePath 目标文件路径
     * @param originalCode 原始代码
     * @param fixedCode 修复后代码
     * @return 应用结果
     */
    public ApplyResult applyFix(Path filePath, String originalCode, String fixedCode) {
        try {
            // 验证文件存在
            if (!Files.exists(filePath)) {
                return ApplyResult.failure("File not found: " + filePath);
            }

            // 备份原始内容
            String backup = Files.readString(filePath, StandardCharsets.UTF_8);

            // 验证原始代码匹配
            if (!backup.equals(originalCode)) {
                LOG.warn("Original code mismatch, file may have been modified");
                return ApplyResult.failure("File has been modified since analysis. Please re-run analysis.");
            }

            // 写入修复后的代码
            Files.writeString(filePath, fixedCode, StandardCharsets.UTF_8, 
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            // 记录应用信息
            String fixId = generateFixId();
            AppliedFix appliedFix = new AppliedFix(
                    fixId,
                    filePath.toString(),
                    backup,
                    fixedCode,
                    Instant.now()
            );
            appliedFixes.put(fixId, appliedFix);

            LOG.info("Applied fix {} to file: {}", fixId, filePath);

            return ApplyResult.success(fixId, filePath.toString());

        } catch (IOException e) {
            LOG.error("Failed to apply fix to file: " + filePath, e);
            return ApplyResult.failure("IO error: " + e.getMessage());
        }
    }

    /**
     * 应用修复到指定行
     * 
     * @param filePath 目标文件路径
     * @param startLine 起始行号
     * @param endLine 结束行号
     * @param newCode 新代码
     * @return 应用结果
     */
    public ApplyResult applyFixToLines(Path filePath, int startLine, int endLine, String newCode) {
        try {
            if (!Files.exists(filePath)) {
                return ApplyResult.failure("File not found: " + filePath);
            }

            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            String backup = String.join("\n", lines);

            // 验证行号
            if (startLine < 1 || endLine > lines.size() || startLine > endLine) {
                return ApplyResult.failure("Invalid line range: " + startLine + "-" + endLine);
            }

            // 构建新内容
            StringBuilder newContent = new StringBuilder();

            // 添加前面的行
            for (int i = 0; i < startLine - 1; i++) {
                newContent.append(lines.get(i)).append("\n");
            }

            // 添加新代码
            newContent.append(newCode).append("\n");

            // 添加后面的行
            for (int i = endLine; i < lines.size(); i++) {
                newContent.append(lines.get(i));
                if (i < lines.size() - 1) {
                    newContent.append("\n");
                }
            }

            String fixedCode = newContent.toString();

            // 写入文件
            Files.writeString(filePath, fixedCode, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            // 记录应用信息
            String fixId = generateFixId();
            AppliedFix appliedFix = new AppliedFix(
                    fixId,
                    filePath.toString(),
                    backup,
                    fixedCode,
                    Instant.now()
            );
            appliedFixes.put(fixId, appliedFix);

            LOG.info("Applied fix {} to lines {}-{} in file: {}", fixId, startLine, endLine, filePath);

            return ApplyResult.success(fixId, filePath.toString());

        } catch (IOException e) {
            LOG.error("Failed to apply fix to lines in file: " + filePath, e);
            return ApplyResult.failure("IO error: " + e.getMessage());
        }
    }

    /**
     * 预览修复效果
     * 
     * @param originalCode 原始代码
     * @param fixedCode 修复后代码
     * @return 差异预览
     */
    public DiffPreview previewFix(String originalCode, String fixedCode) {
        String[] originalLines = originalCode.split("\n");
        String[] fixedLines = fixedCode.split("\n");

        StringBuilder diff = new StringBuilder();

        // 简单的行对行比较
        int maxLines = Math.max(originalLines.length, fixedLines.length);

        for (int i = 0; i < maxLines; i++) {
            String originalLine = i < originalLines.length ? originalLines[i] : null;
            String fixedLine = i < fixedLines.length ? fixedLines[i] : null;

            if (originalLine != null && fixedLine != null) {
                if (originalLine.equals(fixedLine)) {
                    diff.append("  ").append(i + 1).append(": ").append(originalLine).append("\n");
                } else {
                    diff.append("- ").append(i + 1).append(": ").append(originalLine).append("\n");
                    diff.append("+ ").append(i + 1).append(": ").append(fixedLine).append("\n");
                }
            } else if (originalLine != null) {
                diff.append("- ").append(i + 1).append(": ").append(originalLine).append("\n");
            } else {
                diff.append("+ ").append(i + 1).append(": ").append(fixedLine).append("\n");
            }
        }

        return new DiffPreview(diff.toString(), originalLines.length, fixedLines.length);
    }

    /**
     * 回滚已应用的修复
     * 
     * @param fixId 修复 ID
     * @return 回滚结果
     */
    public ApplyResult rollbackFix(String fixId) {
        AppliedFix appliedFix = appliedFixes.get(fixId);
        if (appliedFix == null) {
            return ApplyResult.failure("Fix not found: " + fixId);
        }

        try {
            Path filePath = Path.of(appliedFix.getFilePath());
            Files.writeString(filePath, appliedFix.getOriginalCode(), StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            appliedFixes.remove(fixId);

            LOG.info("Rolled back fix {} for file: {}", fixId, filePath);

            return ApplyResult.success(fixId, filePath.toString());

        } catch (IOException e) {
            LOG.error("Failed to rollback fix: " + fixId, e);
            return ApplyResult.failure("IO error: " + e.getMessage());
        }
    }

    /**
     * 获取已应用的修复
     */
    public AppliedFix getAppliedFix(String fixId) {
        return appliedFixes.get(fixId);
    }

    private String generateFixId() {
        return "fix-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 应用结果
     */
    public static class ApplyResult {
        private final boolean success;
        private final String fixId;
        private final String filePath;
        private final String error;

        private ApplyResult(boolean success, String fixId, String filePath, String error) {
            this.success = success;
            this.fixId = fixId;
            this.filePath = filePath;
            this.error = error;
        }

        public static ApplyResult success(String fixId, String filePath) {
            return new ApplyResult(true, fixId, filePath, null);
        }

        public static ApplyResult failure(String error) {
            return new ApplyResult(false, null, null, error);
        }

        public boolean isSuccess() { return success; }
        public String getFixId() { return fixId; }
        public String getFilePath() { return filePath; }
        public String getError() { return error; }
    }

    /**
     * 差异预览
     */
    public static class DiffPreview {
        private final String diff;
        private final int originalLines;
        private final int fixedLines;

        public DiffPreview(String diff, int originalLines, int fixedLines) {
            this.diff = diff;
            this.originalLines = originalLines;
            this.fixedLines = fixedLines;
        }

        public String getDiff() { return diff; }
        public int getOriginalLines() { return originalLines; }
        public int getFixedLines() { return fixedLines; }
    }

    /**
     * 已应用的修复记录
     */
    public static class AppliedFix {
        private final String fixId;
        private final String filePath;
        private final String originalCode;
        private final String fixedCode;
        private final Instant appliedAt;

        public AppliedFix(String fixId, String filePath, String originalCode, 
                         String fixedCode, Instant appliedAt) {
            this.fixId = fixId;
            this.filePath = filePath;
            this.originalCode = originalCode;
            this.fixedCode = fixedCode;
            this.appliedAt = appliedAt;
        }

        public String getFixId() { return fixId; }
        public String getFilePath() { return filePath; }
        public String getOriginalCode() { return originalCode; }
        public String getFixedCode() { return fixedCode; }
        public Instant getAppliedAt() { return appliedAt; }
    }
}
