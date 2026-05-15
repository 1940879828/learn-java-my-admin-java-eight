package org.example.myadminjavaeight.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoleQueryFilter extends PageRequest{
    private String keyword;
    private Integer level;
    private String dataScope;
}
