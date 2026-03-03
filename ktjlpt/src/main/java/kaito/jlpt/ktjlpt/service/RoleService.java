package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.RoleRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.RoleResponse;
import kaito.jlpt.ktjlpt.mapper.RoleMapper;
import kaito.jlpt.ktjlpt.repository.PermissionRepository;
import kaito.jlpt.ktjlpt.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;


    public RoleResponse createRole(RoleRequest roleRequest) {
        var role = roleMapper.toRole(roleRequest);
        var permissions = permissionRepository.findByNameIn(roleRequest.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);

    }

    public List<RoleResponse> getRoles() {
        var roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }


}
