# 🔥 Hướng dẫn Setup Firebase cho KidSafe

## 1. Tạo Firebase Project

1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Click **"Create a project"**
3. Tên project: `kidsafe-family-control`
4. Disable Google Analytics (không cần cho demo)
5. Click **"Create project"**

## 2. Setup Realtime Database

1. Trong Firebase Console, vào **"Realtime Database"**
2. Click **"Create Database"**
3. Chọn location: **"United States (us-central1)"** 
4. Security rules: **"Start in test mode"** (cho development)
5. Click **"Done"**

### Database Rules (Development):
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

### Database Rules (Production):
```json
{
  "rules": {
    "kidsafe": {
      "families": {
        "$familyId": {
          ".read": true,
          ".write": true
        }
      }
    }
  }
}
```

## 3. Thêm Android App

1. Click icon Android trong Project Overview
2. **Package name**: `com.kidsafe.parent`
3. **App nickname**: `KidSafe Parent`
4. Click **"Register app"**
5. Download `google-services.json`
6. Đặt file vào: `android/app/google-services.json`

## 4. Database Structure

Sau khi setup, data sẽ có cấu trúc như sau:

```json
{
  "kidsafe": {
    "families": {
      "family_demo_123": {
        "blockedUrls": {
          "-N1234567890": {
            "url": "https://facebook.com",
            "addedAt": 1642123456789,
            "addedBy": "parent_android",
            "status": "active"
          },
          "-N1234567891": {
            "url": "https://youtube.com",
            "addedAt": 1642123456790,
            "addedBy": "parent_android", 
            "status": "active"
          }
        },
        "syncStatus": {
          "lastUpdated": 1642123456789,
          "updatedBy": "parent_android",
          "status": "online"
        },
        "pcStatus": {
          "lastSeen": 1642123456789,
          "status": "connected",
          "version": "1.0.0",
          "hostFileStatus": "active"
        }
      }
    }
  }
}
```

## 5. Test Database

Bạn có thể test bằng cách:

1. Vào Firebase Console → Realtime Database
2. Tạo data thủ công theo cấu trúc trên
3. Quan sát realtime updates khi chạy app

## 6. Environment Variables

Tạo file `.env` (optional):
```
FIREBASE_PROJECT_ID=kidsafe-family-control
FIREBASE_REGION=us-central1
FAMILY_ID=family_demo_123
```

## 7. Next Steps

Sau khi setup Firebase:

1. **Test Android app**: `expo start`
2. **Add PC integration**: Tích hợp Firebase vào Go app
3. **Test realtime sync**: Thêm URL từ Android, check hosts file trên PC

## 🔧 Commands để nhớ

```bash
# Install dependencies
npm install

# Start development
expo start

# Start Android
expo start --android

# Clear cache
expo start --clear

# Build APK
expo build:android
```

## 🚨 Troubleshooting

### Error: google-services.json not found
- Đảm bảo file ở đúng vị trí: `android/app/google-services.json`
- Package name phải khớp với Firebase config

### Error: Firebase not initialized
- Check internet connection
- Verify Firebase project settings
- Restart Metro bundler: `expo start --clear`

### Error: Permission denied
- Check Database rules trong Firebase Console
- Đảm bảo rules allow read/write cho development

## 📱 Demo Flow

1. **Mở app Android** → thấy danh sách trống
2. **Thêm URL** (vd: facebook.com) → push lên Firebase
3. **Check Firebase Console** → thấy data realtime
4. **PC app listen** → nhận URL và update hosts file
5. **Test chặn** → vào facebook.com trên PC → thấy trang KidSafe

Đây là cách "ăn gian thông minh" - dùng Firebase làm backend mà không cần tự code server! 🔥






















