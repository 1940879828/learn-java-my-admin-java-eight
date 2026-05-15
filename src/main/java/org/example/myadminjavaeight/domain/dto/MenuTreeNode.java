package org.example.myadminjavaeight.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MenuTreeNode extends MenuResponse{

    private List<MenuTreeNode> children;
}
