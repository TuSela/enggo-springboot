package com.nhom12.enggo_backend.service;

import com.nhom12.enggo_backend.constant.PredefinedRole;
import com.nhom12.enggo_backend.dto.request.UserCreationRequest;
import com.nhom12.enggo_backend.dto.request.UserUpdateRequest;
import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.TopUsersResponse;
import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import com.nhom12.enggo_backend.dto.response.UserResponse;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.entity.identity.auth.Role;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.social.Friend;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.mapper.UserMapper;
import com.nhom12.enggo_backend.mapper.gamificationMapper.BadgeMapper;
import com.nhom12.enggo_backend.repository.RoleRepository;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.social.FriendRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Autowired
    FriendRepository friendRepository;
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

        // 1. Lấy thông tin người dùng hiện tại đang đăng nhập
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        // Tránh truyền null vào orElseThrow kẻo bị NullPointerException, hãy dùng Supplier Lambda hoặc Custom Exception
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // 2. Lấy danh sách bản ghi bạn bè từ FriendRepository
        List<Friend> friendList = friendRepository.findAllByUser(currentUser);
        // Lưu ý: Nếu DB của bạn lưu quan hệ 2 chiều tự động thì chỉ cần tìm theo user1.
        // Nếu lưu 1 dòng duy nhất cho mối quan hệ, bạn cần gom cả friendRepository.findAllByUser2(currentUser) nữa nhé.

        // 3. Gom tất cả ID của những người ĐÃ LÀ BẠN vào một Set để tối ưu tốc độ kiểm tra (O(1))
        Set<Integer> existingFriendIds = friendList.stream()
                .map(friend -> {
                    // Nếu mình là user1 thì người bạn là user2, ngược lại người bạn là user1
                    if (friend.getUser1().getId().equals(currentUser.getId())) {
                        return friend.getUser2().getId();
                    } else {
                        return friend.getUser1().getId();
                    }
                })
                .collect(Collectors.toSet());

        // 4. Tiến hành tìm kiếm và lọc kết quả
        return userRepository.findByUsernameContainingIgnoreCase(keyword)
                .stream()
                // LỌC: 1. Loại bỏ chính bản thân người đăng nhập
                //      2. Loại bỏ những người có ID đã nằm trong danh sách bạn bè (existingFriendIds)
                .filter(user -> !user.getUsername().equalsIgnoreCase(currentUsername)
                        && !existingFriendIds.contains(user.getId()))
                .map(userMapper::toUserMinimalResponse)
                .toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
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
    @Autowired
    private BadgeMapper badgeMapper;

    // New API to get top 4 users by elo and the current user's elo
    public com.nhom12.enggo_backend.dto.response.TopUsersResponse getTopElo() {
        // Fetch top 4 users ordered by elo descending
        List<User> topUsers = userRepository.findTop4ByOrderByEloDesc();
        List<com.nhom12.enggo_backend.dto.response.UserResponse> topResponses = topUsers.stream()
                .map(userMapper::toUserResponse)
                .toList();

        // Determine the elo of the currently authenticated user
        Integer myElo = null;
        var auth = SecurityContextHolder.getContext().getAuthentication();

        User me = null;
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            me = userRepository.findByUsername(username).orElse(null);
        }
        return com.nhom12.enggo_backend.dto.response.TopUsersResponse.builder()
                .topUsers(topResponses)
                .myRank(userMapper.toUserResponse(me))
                .build();
    }


    public PageResponse<UserResponse> getLeaderBoard(Pageable pageable) {
        Page<User> userPage = userRepository.getAllUsers(pageable);

        // 2. Map trực tiếp nội dung từ Page<User> sang Page<UserResponse>
        Page<UserResponse> responsePage = userPage.map(user -> userMapper.toUserResponse(user));

        // 3. Sử dụng chính hàm Static Factory Method `.of()` bạn vừa viết để đóng gói dữ liệu hoàn chỉnh
        return PageResponse.of(responsePage);
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
