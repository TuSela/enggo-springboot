package com.nhom12.enggo_backend.service;

import com.nhom12.enggo_backend.constant.PredefinedRole;
import com.nhom12.enggo_backend.dto.request.UserCreationRequest;
import com.nhom12.enggo_backend.dto.request.UserUpdateRequest;
import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import com.nhom12.enggo_backend.dto.response.UserResponse;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse; // Import BadgeResponse
import com.nhom12.enggo_backend.entity.identity.auth.Role;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.gamification.Badge; // Import Badge
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.mapper.UserMapper;
import com.nhom12.enggo_backend.repository.RoleRepository;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.UserBadgeRepository; // Import UserBadgeRepository
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    
    // Inject Repository của bạn vào đây
    UserBadgeRepository userBadgeRepository;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByRoleName(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName(); // Đây là username của user đang login

        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 1. Map thông tin user cơ bản sang UserResponse qua MapStruct
        UserResponse userResponse = userMapper.toUserResponse(user);

        // 2. Sử dụng hàm có sẵn của bạn: Tìm danh sách huy hiệu thẳng bằng username (name)
        var userBadges = userBadgeRepository.findAllByUser_Username(name);

        // 3. Map thủ công sang BadgeResponse và set vào userResponse
        if (userBadges != null) {
            List<BadgeResponse> badgeResponses = userBadges.stream()
                    .map(userBadge -> {
                        Badge badge = userBadge.getBadge();
                        return BadgeResponse.builder()
                                .id(badge.getId())
                                .badgeName(badge.getBadgeName())
                                .description(badge.getDescription())
                                .iconUrl(badge.getIconUrl())
                                .createdAt(badge.getCreatedAt())
                                .build();
                    })
                    .toList();
            
            userResponse.setBadges(badgeResponses);
        }

        return userResponse;
    }

    public List<UserMinimalResponse> searchUsersByUsername(String username) {
        String keyword = username == null ? "" : username.trim();

        if (keyword.isEmpty()) {
            return List.of();
        }

        return userRepository.findByUsernameContainingIgnoreCase(keyword)
                .stream()
                .map(userMapper::toUserMinimalResponse)
                .toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        if (Objects.nonNull(request.getPassword()) && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (!CollectionUtils.isEmpty(request.getRoles())) {
            var roles = roleRepository.findByRoleNameIn(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(Integer id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
    
    public boolean updateAvatar (String url){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setAvatarUrl(url);
        userRepository.save(user);
        return true;
    }
}