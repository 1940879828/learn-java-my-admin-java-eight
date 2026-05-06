package org.example.myadminjavaeight.exception;

import org.example.myadminjavaeight.enums.ResultCode;

/** 用户名已存在异常 */
public class UsernameExistsException extends BusinessException {

  public UsernameExistsException() {
    super(ResultCode.USERNAME_EXISTS.getCode(), ResultCode.USERNAME_EXISTS.getMessage());
  }

  public UsernameExistsException(String message) {
    super(ResultCode.USERNAME_EXISTS.getCode(), message);
  }
}
