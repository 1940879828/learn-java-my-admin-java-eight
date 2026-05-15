package org.example.myadminjavaeight.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MenuQueryFilter extends PageRequest {

    private String keyword;
    private Integer menuType;
    private Integer visible;
    private Integer status;
    private Integer parentId;
}
