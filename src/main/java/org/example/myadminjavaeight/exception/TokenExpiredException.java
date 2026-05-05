package org.example.myadminjavaeight.exception;

import org.example.myadminjavaeight.enums.ResultCode;

/** Token过期异常 */
public class TokenExpiredException extends BusinessException {
    public TokenExpiredException() {
        super(ResultCode.UNAUTHORIZED.getCode(), "Token已过期");
    }

    public TokenExpiredException(String message) {
        super(ResultCode.UNAUTHORIZED.getCode(), message);
    }
} 
