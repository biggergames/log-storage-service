package com.biggergames.backend.logstorageservice.domain.common;

import lombok.Getter;

@Getter
public enum ResponseCode {
    /**
     * Common successful response identifier.
     */
    SUCCESS("S0000", "SUCCESS"),
    /**
     * This error occurs if an expected parameter is not sent by the client.
     * This parameter might be an expected header or a value in request body.
     * Client is expected to send correctly.
     */
    BAD_REQUEST("E0400", "BAD_REQUEST"),

    /**
     * If JWT token is not validated via backend, this error occurs.
     * It can be treated as a probable fraud attempt.
     */
    UNAUTHORIZED("E0401", "BAD_CREDENTIALS"),

    /**
     * Not used now
     */
    FORBIDDEN("E0403", "FORBIDDEN"),

    /**
     * This error occurs when the requested resource is not found.
     * E.g. if player does not exist in coin-spend.
     */
    NOT_FOUND("E0404", "NOT_FOUND"),

    /**
     * This error occurs when there is a conflict between the data
     * in backend and the data in client; e.g. UTC time differs
     * dramatically between the two systems.
     */
    CONFLICT("E0409", "CONFLICT"),

    /**
     * This error occurs when there is an active operation on a player, and it's locked for more than X secs.
     * Client may retry after sometime; but mostly requires close attention.
     */
    PLAYER_LOCKED("E0423", "LOCKED"),

    /**
     * It's used for unexpected exceptions on backend side.
     * E.g. connection problem regarding db, network etc.
     * Client may retry after sometime; but mostly requires close attention.
     */
    SYSTEM_EXCEPTION("E0500", "SYSTEM_EXCEPTION"),

    /**
     * This error occurs when custom header specified below does not exist or EMPTY.
     */
    DEVICE_ID_INVALID("E0460", "DEVICE_ID_INVALID"),

    /**
     * This error occurs when header df-platform does not exist or is invalid.
     */
    PLATFORM_INVALID("E0461", "PLATFORM_INVALID"),

    /**
     * This error occurs when custom header specified below does not exist or EMPTY.
     */
    LOGIN_TYPE_INVALID("E0462", "LOGIN_TYPE_INVALID"),

    /**
     * This error occurs when one or more of the custom headers below are invalid or missing
     */
    SOCIAL_ID_INVALID("E0463", "SOCIAL_ID_INVALID"),
    SOCIAL_ID_TYPE_INVALID("E0464", "SOCIAL_ID_TYPE_INVALID"),
    BUILD_NO_INVALID("E0465", "BUILD_NO_INVALID"),

    /**
     * Token missing occurs if authorization header is missing or empty.
     * Client is expected to sent authorization header in all API calls
     */
    TOKEN_MISSING("E0470", "TOKEN_MISSING"),

    /**
     * This error occurs during login, if client does not send a valid authorization header
     * This error may also occur during other requests, if client does not send authorization token (JWT) with Bearer prefix
     * Otherwise, it shows a possible fraud attempt.
     */
    TOKEN_INVALID("E0471", "TOKEN_INVALID"),

    /**
     * Token expired occurs if the token's expiry has passed.
     * A re-login will be sufficient to get a new JWT
     */
    TOKEN_EXPIRED("E0472", "TOKEN_EXPIRED"),

    /**
     * Token mismatch occurs if the playerId sent in URI does not match the playerId in JWT.
     * Probable fraud attempt, client not expected to receive this error.
     */
    TOKEN_MISMATCH("E0479", "TOKEN_MISMATCH"),

    /**
     * Inventory version sent by client does not match with backend's value
     * Backend will send its full inventory with this error and expect client to sync
     */
    INVENTORY_VERSION_MISMATCH("E0480", "INVENTORY_VERSION_MISMATCH"),

    INVALID_IDENTIFIER("E0481", "INVALID_IDENTIFIER"),

    NONE("S0000", "N/A");

    private final String code;
    private final String type;

    ResponseCode(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public final boolean isSuccess() {
        return this == SUCCESS;
    }
}