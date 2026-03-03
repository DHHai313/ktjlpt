package kaito.jlpt.ktjlpt.mapper;

import kaito.jlpt.ktjlpt.dto.request.RoleRequest;
import kaito.jlpt.ktjlpt.dto.response.RoleResponse;
import kaito.jlpt.ktjlpt.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest roleRequest);
    RoleResponse toRoleResponse(Role role);
}
