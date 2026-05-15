package org.example.myadminjavaeight.domain.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.myadminjavaeight.enums.DataScopeEnum;

@Setter
@Getter
public class RoleResponse {
    private Long id;
    private String roleCode;
    private String roleName;
    private String permission;
    private Integer level;
    private DataScopeEnum dataScope;
    private String createBy;
    private Long createTime;
    private String remark;
}
