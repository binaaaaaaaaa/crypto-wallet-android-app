package com.example.cryptowallet.data.auth

// Một data class đơn giản để biểu diễn một người dùng
data class User(val email: String, val passwordHash: String)

/**
 * Một đối tượng singleton để giả lập một database người dùng từ xa.
 * Trong một ứng dụng thực tế, lớp này sẽ được thay thế bằng các cuộc gọi đến backend API hoặc Firebase.
 */
object UserDatabase {
    private val users = mutableMapOf<String, User>()

    init {
        // Thêm một người dùng mặc định để kiểm thử
        // Chúng ta lưu hash của mật khẩu thay vì mật khẩu gốc để tăng cường bảo mật
        users["user@email.com"] = User("user@email.com", "password123".hashCode().toString())
    }

    /**
     * Tìm một người dùng dựa trên email.
     */
    fun findUserByEmail(email: String): User? {
        return users[email.lowercase()]
    }

    /**
     * Đăng ký một người dùng mới.
     * @return `true` nếu đăng ký thành công, `false` nếu email đã tồn tại.
     */
    fun registerUser(email: String, password: String): Boolean {
        val lowerCaseEmail = email.lowercase()
        if (users.containsKey(lowerCaseEmail)) {
            return false // Người dùng đã tồn tại
        }
        users[lowerCaseEmail] = User(lowerCaseEmail, password.hashCode().toString())
        return true // Đăng ký thành công
    }
}
