package org.example.myadminjavaeight.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserQueryFilter {
    private String keyword;
    private Integer status;
    private Boolean locked;
    private Long roleId;
}
