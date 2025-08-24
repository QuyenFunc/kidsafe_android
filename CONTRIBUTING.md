# Đóng góp cho KidSafe Android

Cảm ơn bạn đã quan tâm đến việc đóng góp cho dự án KidSafe Android! 

## Quy trình đóng góp

### 1. Báo cáo lỗi

Trước khi báo cáo lỗi, vui lòng:
- Kiểm tra xem lỗi đã được báo cáo chưa trong [Issues](https://github.com/your-username/kidsafe_android/issues)
- Sử dụng template issue được cung cấp
- Cung cấp thông tin chi tiết về lỗi

### 2. Đề xuất tính năng

- Mở một issue với label "enhancement"
- Mô tả rõ tính năng bạn muốn
- Giải thích lý do tại sao tính năng này hữu ích

### 3. Phát triển code

#### Chuẩn bị môi trường

1. Fork repository
2. Clone fork của bạn:
```bash
git clone https://github.com/your-username/kidsafe_android.git
```
3. Tạo branch mới:
```bash
git checkout -b feature/ten-tinh-nang-moi
```

#### Coding Standards

- Sử dụng Java coding conventions
- Tuân thủ Android development best practices
- Viết comment cho code phức tạp
- Đặt tên biến và method rõ ràng

#### Testing

- Viết unit tests cho logic mới
- Đảm bảo tất cả tests pass
- Test trên nhiều API levels khác nhau

### 4. Pull Request

1. Đảm bảo code của bạn tuân thủ coding standards
2. Cập nhật documentation nếu cần
3. Viết commit message rõ ràng
4. Tạo pull request với:
   - Tiêu đề mô tả ngắn gọn
   - Mô tả chi tiết thay đổi
   - Link đến issue liên quan (nếu có)

## Commit Message Format

```
type(scope): subject

body

footer
```

### Các loại commit:

- **feat**: Tính năng mới
- **fix**: Sửa lỗi
- **docs**: Cập nhật documentation
- **style**: Thay đổi formatting, không ảnh hưởng logic
- **refactor**: Refactor code
- **test**: Thêm hoặc sửa tests
- **chore**: Maintenance tasks

### Ví dụ:

```
feat(auth): add biometric authentication

- Implement fingerprint authentication
- Add fallback to PIN authentication
- Update login flow

Closes #123
```

## Code Review

Tất cả pull requests sẽ được review:
- Kiểm tra logic và performance
- Đảm bảo tuân thủ coding standards
- Kiểm tra security và privacy
- Verify tests coverage

## Câu hỏi?

Nếu bạn có câu hỏi, hãy:
- Mở issue với label "question"
- Gửi email đến: your-email@example.com

Cảm ơn bạn đã đóng góp! 🚀
