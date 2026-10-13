package com.kubemanager.user_service.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserInterceptorTest {

    private UserInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new UserInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void preHandle_shouldPopulateUserContextFromHeaders() throws Exception {
        UUID userId = UUID.randomUUID();

        request.addHeader(HeaderConstants.USER_ID, userId.toString());
        request.addHeader(HeaderConstants.USERNAME, "test-user");
        request.addHeader(HeaderConstants.EMAIL, "test@example.com");
        request.addHeader(HeaderConstants.ROLES, "ROLE_USER, ROLE_ADMIN");
        request.addHeader(HeaderConstants.PROVIDER, "LOCAL");
        request.addHeader(HeaderConstants.REQUEST_ID, "request-123");
        request.addHeader(HeaderConstants.CORRELATION_ID, "correlation-456");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);

        UserContext context = UserContextHolder.getRequiredContext();

        assertEquals(userId, context.getUserId());
        assertEquals("test-user", context.getUsername());
        assertEquals("test@example.com", context.getEmail());
        assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"), context.getRoles());
        assertEquals("LOCAL", context.getProvider());
        assertEquals("request-123", context.getRequestId());
        assertEquals("correlation-456", context.getCorrelationId());
    }

    @Test
    void preHandle_shouldCreateEmptyRolesWhenRolesHeaderIsMissing()
            throws Exception {
        request.addHeader(
                HeaderConstants.USER_ID,
                UUID.randomUUID().toString()
        );

        assertTrue(
                interceptor.preHandle(request, response, new Object())
        );

        assertNotNull(UserContextHolder.getRequiredContext());
        assertEquals(List.of(), UserContextHolder.getRequiredContext().getRoles());
    }

    @Test
    void preHandle_shouldAllowRequestWithoutUserIdHeader() throws Exception {
        assertTrue(
                interceptor.preHandle(request, response, new Object())
        );

        assertFalse(UserContextHolder.hasContext());
    }

    @Test
    void preHandle_shouldAllowRequestWithBlankUserIdHeader() throws Exception {
        request.addHeader(HeaderConstants.USER_ID, "   ");

        assertTrue(
                interceptor.preHandle(request, response, new Object())
        );

        assertFalse(UserContextHolder.hasContext());
    }

    @Test
    void preHandle_shouldThrowForMalformedUserId() {
        request.addHeader(HeaderConstants.USER_ID, "not-a-uuid");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preHandle(request, response, new Object())
        );

        assertFalse(UserContextHolder.hasContext());
    }

    @Test
    void afterCompletion_shouldClearUserContext() throws Exception {
        UserContextHolder.set(
                UserContext.builder()
                        .userId(UUID.randomUUID())
                        .username("test-user")
                        .build()
        );

        interceptor.afterCompletion(
                request,
                response,
                new Object(),
                null
        );

        assertFalse(UserContextHolder.hasContext());
    }
}