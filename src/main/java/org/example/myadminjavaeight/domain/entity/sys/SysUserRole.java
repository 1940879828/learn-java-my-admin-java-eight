package org.example.myadminjavaeight.domain.entity.sys;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class SysUserRole {

    /** ID */
    private Integer id;
    /** 用户ID */
    private Long userId;
    /** 角色ID */
    private Integer roleId;
    /** 创建时间 */
    private Date createTime;
}
