package org.example.myadminjavaeight.domain.dto;

import java.util.List;

public class UserDetailResponse {
    private List<RoleResponse> roles;
    private List<String> permissions;
    private List<MenuTreeNode> menuTree;
}
