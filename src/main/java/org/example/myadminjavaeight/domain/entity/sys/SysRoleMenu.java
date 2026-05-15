package org.example.myadminjavaeight.domain.entity.sys;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class SysRoleMenu {

    /** ID */
    private Integer id;
    /** 角色ID */
    private Integer roleId;
    /** 菜单ID */
    private Integer menuId;
    /** 创建时间 */
    private Date createTime;
}
