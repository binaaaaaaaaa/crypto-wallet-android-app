# Đồ án Ứng dụng Ví Crypto - Kotlin & Jetpack Compose

Đây là một đồ án xây dựng ứng dụng ví tiền mã hóa (crypto wallet) cho hệ điều hành Android, sử dụng các công nghệ và kiến trúc hiện đại. Ứng dụng mô phỏng các chức năng cốt lõi của một ví crypto, từ việc quản lý tài sản, theo dõi giá, cho đến các tính năng giao dịch và bảo mật.

---

## ✨ Các tính năng chính

Xác thực người dùng:

Đăng ký và Đăng nhập bằng Email/Mật khẩu qua Firebase Authentication.

Đăng nhập bằng tài khoản Google.

Lưu trữ thông tin đăng nhập và tự động điều hướng.

Chức năng Đăng xuất an toàn.

Quản lý Tài sản (Màn hình chính):

Hiển thị tổng giá trị tài sản và PNL (lãi/lỗ) trong ngày theo thời gian thực.

Chức năng ẩn/hiện số dư để đảm bảo riêng tư.

Phân bổ tài sản trực quan giữa các tài khoản Funding và Giao dịch.

Danh sách các tài sản người dùng đang sở hữu, được cập nhật giá từ API.

Chức năng tìm kiếm và "Kéo để làm mới" (Pull to Refresh).

Chi tiết Token:

Hiển thị biểu đồ giá (sử dụng thư viện Vico) với các khoảng thời gian có thể tùy chỉnh (1 Ngày, 1 Tuần, 1 Tháng, 6 Tháng).

Hiển thị chi tiết về giá, số dư, và phân bổ của từng token.

Lịch sử giao dịch được lọc riêng cho từng loại token.

Luồng Giao dịch:

Nạp tiền: Chọn token -> Chọn mạng lưới (ERC20, TRC20,...) -> Hiển thị địa chỉ ví và mã QR.

Rút tiền: Chọn phương thức (On-chain/P2P) -> Chọn token muốn rút -> Nhập thông tin giao dịch.

Chuyển tiền: Chuyển tài sản nội bộ giữa tài khoản Funding và Giao dịch.

Chuyển đổi: Hoán đổi (swap) giữa các loại tiền mã hóa khác nhau.

Giao dịch P2P: Giao diện mua/bán với danh sách các thương gia giả lập.

Quản lý Hồ sơ & Bảo mật:

Hiển thị thông tin người dùng (tên, email, UID).

Thay đổi quốc gia.

Các tùy chọn bảo mật như xem Cụm từ khôi phục, thay đổi Mật khẩu, Email (với các hộp thoại cảnh báo an toàn).

---

## 🚀 Công nghệ & Kiến trúc
Ngôn ngữ: Kotlin

Giao diện người dùng (UI): Jetpack Compose - Bộ công cụ hiện đại để xây dựng giao diện gốc.

Kiến trúc: MVVM (Model-View-ViewModel) - Giúp phân tách rõ ràng logic, giao diện và dữ liệu, làm cho code dễ bảo trì và kiểm thử.

Bất đồng bộ: Kotlin Coroutines & Flow để xử lý các tác vụ nền và cập nhật dữ liệu một cách mượt mà.

Điều hướng: Jetpack Navigation Compose - Quản lý luồng di chuyển giữa các màn hình.

Backend & Database:

Firebase Authentication cho việc xác thực người dùng.

(Kế hoạch) Cloud Firestore để lưu trữ dữ liệu người dùng và giao dịch.

Networking:

Retrofit để thực hiện các cuộc gọi API.

Gson để phân tích dữ liệu JSON.

Hiển thị hình ảnh: Coil - Thư viện tải và hiển thị hình ảnh hiệu quả.

Biểu đồ: Vico - Thư viện vẽ biểu đồ hiện đại và linh hoạt cho Compose.

---

## 🛠️ Hướng dẫn Cài đặt
Clone repository:
```bash
git clone https://github.com/binaaaaaaaaa/crypto-wallet-android-app.git
```
Thiết lập Firebase:

Tạo một dự án mới trên Firebase Console.

Thêm một ứng dụng Android với package name là com.example.cryptowallet.

Tải về file google-services.json và đặt nó vào thư mục app/ của dự án.

Trong mục Authentication, kích hoạt các phương thức đăng nhập Email/Password và Google.

Mở dự án:

Mở dự án bằng Android Studio.

Đợi Gradle đồng bộ và tải về tất cả các dependencies.

Chạy ứng dụng:

Nhấn nút "Run" để cài đặt và chạy ứng dụng trên máy ảo hoặc thiết bị thật.

Cảm ơn bạn đã xem qua đồ án này!
