package com.petcloud.user.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDomainEnumTest {

    @Test
    void shouldResolveDiagnosisTaskStatusFallback() {
        assertEquals(DiagnosisTaskStatus.FAILED, DiagnosisTaskStatus.fromCode("failed"));
        assertEquals(DiagnosisTaskStatus.PROCESSING, DiagnosisTaskStatus.fromCode("unknown"));
    }

    @Test
    void shouldNormalizeOwnerTypeAndMessageType() {
        assertEquals(MediaOwnerType.DEFAULT_CODE, MediaOwnerType.normalize(null));
        assertEquals(MediaOwnerType.DIAGNOSIS.getCode(), MediaOwnerType.normalize("diagnosis"));
        assertEquals("custom_owner", MediaOwnerType.normalize("custom_owner"));
        assertEquals(PrivateMessageType.DEFAULT_CODE, PrivateMessageType.normalize("other"));
    }

    @Test
    void shouldResolveMediaAndQueryDefaults() {
        assertEquals(MediaType.VIDEO, MediaType.fromCode("video"));
        assertEquals(MediaType.IMAGE, MediaType.fromCode("other"));
        assertEquals(CommunityPostQueryType.LATEST, CommunityPostQueryType.fromCode("unknown"));
    }
}
