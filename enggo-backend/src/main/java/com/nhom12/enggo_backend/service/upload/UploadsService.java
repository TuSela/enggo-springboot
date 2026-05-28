package com.nhom12.enggo_backend.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UploadsService {

    // Đã tiêm qua Constructor nhờ @RequiredArgsConstructor, không cần @Autowired ở đây nữa
    private final Cloudinary cloudinary;

    // 1. Sửa "image/svg" thành "image/svg+xml" để nhận diện đúng chuẩn quốc tế
    private static final List<String> ALLOWED_IMAGE_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/jpg", "image/svg+xml");

    // Giới hạn dung lượng (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        // 2. Kiểm tra định dạng (Chỉ nhận ảnh và SVG)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        // 3. Kiểm tra dung lượng
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // 4. Cấu hình upload lên Cloudinary hỗ trợ SVG hoàn chỉnh
        Map<String, Object> params = ObjectUtils.asMap(
                "resource_type", "image", // Bắt buộc nhận diện là ảnh công khai
                "format", "svg"           // Gợi ý cho Cloudinary giữ nguyên định dạng nếu là file SVG
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult.get("url").toString();
    }
}