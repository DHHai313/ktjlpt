package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.request.UserCreationRequest;
import kaito.jlpt.ktjlpt.dto.request.UserUpdateRequest;
import kaito.jlpt.ktjlpt.dto.response.UserResponse;
import kaito.jlpt.ktjlpt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping()
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }
    @GetMapping()
    ApiResponse<List<UserResponse>> getUsers(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        log.info("username: {}", authentication.getName());
        log.info("scope: {}", roles);
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }
    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable String userId){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }
    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId,@RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }
    @DeleteMapping("/{userId}")
    ApiResponse<Object> deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return ApiResponse.builder()
                .result("deleted")
                .build();
    }
}
