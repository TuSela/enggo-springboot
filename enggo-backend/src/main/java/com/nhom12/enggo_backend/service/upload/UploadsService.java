package com.nhom12.enggo_backend.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UploadsService {
    @Autowired Cloudinary cloudinary;
    private static final List<String> ALLOWED_IMAGE_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/jpg","image/svg");

    // Giới hạn dung lượng (ví dụ 5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
        public String uploadImage(MultipartFile file) throws IOException {
            if (file == null || file.isEmpty()) {
                throw new AppException(ErrorCode.FILE_EMPTY);
            }

            // 2. Kiểm tra định dạng (Chỉ nhận ảnh)
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE);
            }

            // 3. Kiểm tra dung lượng
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new AppException(ErrorCode.FILE_TOO_LARGE);
            }

            // 4. Nếu mọi thứ ổn mới upload lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "image")); // Chốt cứng là "image" thay vì "auto"

            return uploadResult.get("url").toString();
        }
    }
