package org.example.myadminjavaeight.service.impl;

import org.example.myadminjavaeight.domain.dto.*;
import org.example.myadminjavaeight.domain.entity.sys.SysMenu;
import org.example.myadminjavaeight.enums.MenuTypeEnum;
import org.example.myadminjavaeight.exception.ResourceNotFoundException;
import org.example.myadminjavaeight.mapper.MenuMapper;
import org.example.myadminjavaeight.mapper.RoleMapper;
import org.example.myadminjavaeight.service.MenuService;
import org.example.myadminjavaeight.utils.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;

    public MenuServiceImpl(MenuMapper menuMapper, RoleMapper roleMapper) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public void deleteById(Integer id) {
        menuMapper.deleteById(id);
    }

    @Override
    public PageResponse<MenuResponse> findByFilter(MenuQueryFilter filter) {

        List<SysMenu> menus = menuMapper.findByFilter(filter);
        long total = menuMapper.countByFilter(filter);

        List<MenuResponse> responses = menus.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        return PageResponse.of(responses, filter.getPage(), filter.getSize(), total);
    }

    @Override
    public MenuResponse findByIdDto(Integer id) {
        SysMenu menu = menuMapper.findById(id);
        if (menu == null) {
            throw new ResourceNotFoundException("Menu", id);
        }
        return convertToResponse(menu);
    }

    @Override
    public Integer createMenu(MenuCreateRequest request) {
        return 0;
    }

    @Override
    public void updateMenu(Integer id, MenuUpdateRequest request) {

    }

    @Override
    public void deleteMenu(Integer id) {

    }

    @Override
    public List<MenuTreeNode> getMenuTree() {
        return Collections.emptyList();
    }

    @Override
    public List<MenuTreeNode> getMenuTreeByUserId(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public List<RoleResponse> findRolesByMenuId(Integer menuId) {
        return Collections.emptyList();
    }

    private MenuResponse convertToResponse(SysMenu sysMenu) {
        return fillMenuFields(sysMenu, new MenuResponse());
    }

    private List<MenuTreeNode> buildMenuTree(List<SysMenu> menus, Integer parentId) {
        List<MenuTreeNode> tree = new ArrayList<>();

        for (SysMenu menu:menus) {
            boolean isMatch;

            if (parentId == null) {
                isMatch = menu.getParentId() == null || menu.getParentId() == 0;
            } else {
                isMatch = parentId.equals(menu.getParentId());
            }

            if (isMatch) {
                MenuTreeNode node = fillMenuFields(menu, new MenuTreeNode());

                List<MenuTreeNode> children = buildMenuTree(menus, menu.getId());
                node.setChildren(children.isEmpty() ? null : children);

                tree.add(node);
            }
        }

        return tree;
    }

    /** 将 SysMenu 的公共字段填充到 MenuResponse 或其子类,返回填充后的目标对象。 */
    private <T extends MenuResponse> T fillMenuFields(SysMenu menu, T target) {
        target.setId(menu.getId() != null ? menu.getId().longValue() : null);
        target.setParentId(menu.getParentId());
        target.setMenuName(menu.getMenuName());
        target.setMenuCode(menu.getMenuCode());
        target.setMenuType(menu.getMenuType() != null ? MenuTypeEnum.fromCode(menu.getMenuType()) : null);
        target.setPath(menu.getPath());
        target.setComponent(menu.getComponent());
        target.setPerms(menu.getPerms());
        target.setIcon(menu.getIcon());
        target.setSortOrder(menu.getSortOrder());
        target.setVisible(menu.getVisible() != null && menu.getVisible() == 1);
        target.setStatus(menu.getStatus());
        target.setCreateBy(menu.getCreateBy());
        target.setCreateTime(DateUtils.toEpochSeconds(menu.getCreateTime()));
        target.setRemark(menu.getRemark());

        return target;
    }
}
