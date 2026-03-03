package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.UserCreationRequest;
import kaito.jlpt.ktjlpt.dto.request.UserUpdateRequest;
import kaito.jlpt.ktjlpt.dto.response.UserResponse;
import kaito.jlpt.ktjlpt.entity.User;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.enums.Provider;
import kaito.jlpt.ktjlpt.enums.Role;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.mapper.UserMapper;
import kaito.jlpt.ktjlpt.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

//    @Autowired
     UserRepository userRepository;
//    @Autowired
     UserMapper userMapper;


    public UserResponse createUser(UserCreationRequest request){
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        } else if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = userMapper.toUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
             //user.setRole(Role.USER);
             user.setActive(true);
             user.setProvider(Provider.LOCAL);

        return userMapper.toUserResponse(userRepository.save(user));
    }
    //@PreAuthorize("hasAuthority('VIEW')")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers(){
        log.info("getUsers");
        List<UserResponse> userResponse = userMapper.toUserResponse(userRepository.findAll());
        return userResponse;
    }
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found")));
    }
    public UserResponse updateUser(String userId,UserUpdateRequest request){
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
//        user.setFullName(request.getFullName());
//        user.setCurrentLevel(request.getCurrentLevel());
//        user.setAvatarUrl(request.getAvatarUrl());
        userMapper.updateUser(user,request);
        return userMapper.toUserResponse(userRepository.save(user));
    }
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
         User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
         return userMapper.toUserResponse(user);
    }
}
