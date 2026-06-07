package com.nhom12.enggo_backend.service;

import com.nhom12.enggo_backend.constant.PredefinedRole;
import com.nhom12.enggo_backend.dto.request.UserCreationRequest;
import com.nhom12.enggo_backend.dto.request.UserUpdateRequest;
import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import com.nhom12.enggo_backend.dto.response.UserResponse;
import com.nhom12.enggo_backend.entity.identity.auth.Role;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.mapper.UserMapper;
import com.nhom12.enggo_backend.repository.RoleRepository;
import com.nhom12.enggo_backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

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
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
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

    private final RedisTemplate<String, Object> redisTemplate;

    // Tạo một Key chung cho phân vùng chứa danh sách online trong Redis
    private static final String REDIS_ONLINE_KEY = "enggo:users:online";
    // Phân vùng phụ để ánh xạ ngược từ sessionId sang userId khi disconnect
    private static final String REDIS_SESSION_KEY = "enggo:session:";

    // 1. Khi User kết nối thành công (Báo ON)
    public void userOnline(String userId, String sessionId) {
        // Lưu vào Hash của Redis: Key là cụm cố định, Field là userId, Value là sessionId
        redisTemplate.opsForHash().put(REDIS_ONLINE_KEY, userId, sessionId);

        // Lưu thêm 1 cặp String phụ để tí nữa khi ngắt kết nối, từ sessionId tìm ra được userId để xóa
        // Đặt thời gian tự hủy (TTL) là 1 ngày đề phòng trường hợp treo dữ liệu rác
        redisTemplate.opsForValue().set(REDIS_SESSION_KEY + sessionId, userId, 1, TimeUnit.DAYS);

        System.out.println("🟢 [REDIS] User [" + userId + "] đã Online với Session: " + sessionId);
    }

    // 2. Khi User ngắt kết nối (Báo OFF)
    public void userOffline(String sessionId) {
        String sessionKey = REDIS_SESSION_KEY + sessionId;
        // Tìm xem sessionId này là của User nào
        String userId = (String) redisTemplate.opsForValue().get(sessionKey);

        if (userId != null) {
            // Xóa User khỏi danh sách Online trong Hash
            redisTemplate.opsForHash().delete(REDIS_ONLINE_KEY, userId);
            // Xóa luôn Key phụ
            redisTemplate.delete(sessionKey);
            System.out.println("[REDIS] User [" + userId + "] đã ngắt kết nối (Offline).");
        }
    }

    // 3. Hàm kiểm tra xem một người dùng bất kỳ có đang Online hay không
    public boolean isUserOnline(String userId) {
        return redisTemplate.opsForHash().hasKey(REDIS_ONLINE_KEY, userId);
    }
}
