package kaito.jlpt.ktjlpt.controller;

import kaito.jlpt.ktjlpt.dto.request.PermissionRequest;
import kaito.jlpt.ktjlpt.dto.request.RoleRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.PermissionResponse;
import kaito.jlpt.ktjlpt.dto.response.RoleResponse;
import kaito.jlpt.ktjlpt.service.PermissionService;
import kaito.jlpt.ktjlpt.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest roleRequest) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.createRole(roleRequest))
                .build();
    }
    @GetMapping
    ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getRoles())
                .build();
    }

}
