# Time Capsule — 技术栈

## 语言
- Java

## UI
- XML 布局
- ViewBinding

## 架构
- MVVM
- ViewModel
- LiveData

## 后端 / 云服务
- Firebase Authentication（注册 / 登录）
- Firestore（存胶囊数据：标题、正文、解锁时间、隐私设置、位置、创建时间）
- Firebase Storage（存图片 / 视频）
- FCM + Cloud Functions（解锁推送通知）

## 图片加载
- Picasso

## 定位
- FusedLocationProviderClient（获取坐标）
- Geocoder（坐标转地名）
- 或：跳过定位 API，改为用户手动输入位置文字

## 页面跳转
- 手动 Intent
