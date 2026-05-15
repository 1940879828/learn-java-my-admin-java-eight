package org.example.myadminjavaeight.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponse {
    private Long id;
    private String username;
    private String nickName;
    private String email;
    private String phone;
    private Integer status;
    private Boolean locked;
    private String createBy;
    private Long createTime;
    private String remark;
}
