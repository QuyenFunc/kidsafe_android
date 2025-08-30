# KidSafe Android

Ứng dụng Android KidSafe giúp phụ huynh quản lý và bảo vệ trẻ em khi sử dụng thiết bị di động.

## Tính năng

- 🔒 Kiểm soát truy cập ứng dụng
- ⏰ Quản lý thời gian sử dụng thiết bị
- 🌐 Lọc nội dung web
- 📱 Giám sát hoạt động
- 👨‍👩‍👧‍👦 Giao diện phụ huynh thân thiện

## Yêu cầu hệ thống

- Android API level 21+ (Android 5.0+)
- Java 8+
- Android Studio Arctic Fox trở lên

## Cài đặt

1. Clone repository:
```bash
git clone https://github.com/your-username/kidsafe_android.git
cd kidsafe_android
```

2. Mở project trong Android Studio

3. Cấu hình Firebase (Production):
   - Tạo Firebase project tại [Firebase Console](https://console.firebase.google.com/)
   - Tải file `google-services.json` từ Firebase Console
   - Đặt file `google-services.json` vào thư mục `app/`
   - Làm theo hướng dẫn chi tiết trong file `firebase-setup.md`
   - **Lưu ý:** Ứng dụng được cấu hình để kết nối đến Firebase production, không phải emulator

4. Build và chạy ứng dụng

## Cấu trúc dự án

```
app/
├── src/main/
│   ├── java/          # Mã nguồn Java
│   ├── res/           # Resources (layout, drawable, values)
│   └── AndroidManifest.xml
├── build.gradle.kts   # Cấu hình build
└── google-services.json  # Cấu hình Firebase
```

## Tài liệu

- [Hướng dẫn cài đặt](SETUP.md)
- [Cấu hình Firebase](firebase-setup.md)
- [Kích hoạt Firebase Auth](FIREBASE-AUTH-ENABLE.md)

## Đóng góp

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Mở Pull Request

## License

Dự án này được phân phối dưới giấy phép MIT. Xem file `LICENSE` để biết thêm chi tiết.

## Liên hệ

- Email: your-email@example.com
- Issues: [GitHub Issues](https://github.com/your-username/kidsafe_android/issues)