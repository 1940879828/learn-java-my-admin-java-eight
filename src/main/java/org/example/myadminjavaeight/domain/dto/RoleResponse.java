package org.example.myadminjavaeight.domain.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.myadminjavaeight.enums.DataScopeEnum;

import java.time.OffsetDateTime;

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
    private OffsetDateTime createTime;
    private String remark;
}
