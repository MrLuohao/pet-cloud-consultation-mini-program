package com.petcloud.ai.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiDomainEnumTest {

    @Test
    void shouldResolveTaskStatusAndTerminalState() {
        assertEquals(AiTaskStatus.FAILED, AiTaskStatus.fromCode("failed"));
        assertEquals(AiTaskStatus.PROCESSING, AiTaskStatus.fromCode("unknown"));
        assertTrue(AiTaskStatus.COMPLETED.isTerminal());
        assertFalse(AiTaskStatus.PROCESSING.isTerminal());
    }

    @Test
    void shouldResolveTaskTypeFallback() {
        assertEquals(AiTaskType.MEDIA_MODERATION, AiTaskType.fromCode("media_moderation"));
        assertEquals(AiTaskType.DIAGNOSIS_GENERATE, AiTaskType.fromCode("unknown"));
    }

    @Test
    void shouldResolveModerationStatusAndSubmissionRule() {
        assertEquals(MediaModerationStatus.REVIEW, MediaModerationStatus.fromCode("review"));
        assertEquals(MediaModerationStatus.REVIEW, MediaModerationStatus.fromCode("unknown"));
        assertTrue(MediaModerationStatus.PASS.isAllowedForBizSubmission());
        assertFalse(MediaModerationStatus.REVIEW.isAllowedForBizSubmission());
    }

    @Test
    void shouldResolveMediaTypeAndUploadStatusFallback() {
        assertEquals(MediaType.VIDEO, MediaType.fromCode("video"));
        assertEquals(MediaType.IMAGE, MediaType.fromCode(null));
        assertEquals(MediaUploadStatus.FAILED, MediaUploadStatus.fromCode("failed"));
        assertEquals(MediaUploadStatus.UPLOADED, MediaUploadStatus.fromCode("other"));
    }
}
