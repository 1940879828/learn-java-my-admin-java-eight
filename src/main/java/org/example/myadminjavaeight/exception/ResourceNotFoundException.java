package org.example.myadminjavaeight.exception;

import org.example.myadminjavaeight.enums.ResultCode;

/** 资源不存在异常 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceType, Object id) {
        super(ResultCode.NOT_FOUND.getCode(), String.format("%s[id=%s]不存在", resourceType, id));
    }

    public ResourceNotFoundException(ResultCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }
}
