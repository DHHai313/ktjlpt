package kaito.jlpt.ktjlpt.mapper;

import kaito.jlpt.ktjlpt.dto.request.PermissionRequest;
import kaito.jlpt.ktjlpt.dto.request.UserCreationRequest;
import kaito.jlpt.ktjlpt.dto.request.UserUpdateRequest;
import kaito.jlpt.ktjlpt.dto.response.PermissionResponse;
import kaito.jlpt.ktjlpt.dto.response.UserResponse;
import kaito.jlpt.ktjlpt.entity.Permission;
import kaito.jlpt.ktjlpt.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
   Permission toPermission(PermissionRequest request);
   PermissionResponse toPermissionResponse(Permission permission);
}
