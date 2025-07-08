package com.example.cryptowallet.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptowallet.data.auth.UserDatabase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true, // Bắt đầu ở trạng thái đang tải
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val authError: String? = null,
    val registrationSuccess: Boolean = false,
    val lastLoggedInEmail: String? = null
)

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()
    private val auth: FirebaseAuth = Firebase.auth

    init {
        // Lắng nghe sự thay đổi trạng thái đăng nhập từ Firebase
        auth.addAuthStateListener { firebaseAuth ->
            // Khi listener nhận được trạng thái, việc tải ban đầu đã hoàn tất
            _authState.update { it.copy(
                isAuthenticated = firebaseAuth.currentUser != null,
                isLoading = false
            )}
        }
    }

    // ... các hàm khác giữ nguyên ...
    fun onUsernameChange(username: String) {
        _authState.update { it.copy(username = username, authError = null) }
    }

    fun onEmailChange(email: String) {
        _authState.update { it.copy(email = email, emailError = null, authError = null) }
    }

    fun onPasswordChange(password: String) {
        _authState.update { it.copy(password = password, authError = null) }
    }

    fun togglePasswordVisibility() {
        _authState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun validateEmail(): Boolean {
        val email = _authState.value.email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.update { it.copy(emailError = "Định dạng email không hợp lệ") }
            return false
        }
        return true
    }

    fun login() {
        if (!validateEmail()) return

        val state = _authState.value
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            try {
                auth.signInWithEmailAndPassword(state.email, state.password).await()
                _authState.update { it.copy(lastLoggedInEmail = it.email, password = "", username = "") }
            } catch (e: Exception) {
                _authState.update { it.copy(authError = "Đăng nhập thất bại: ${e.message}") }
            } finally {
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun register(passwordConfirmation: String) {
        if (!validateEmail()) return

        val state = _authState.value
        if (state.password.length < 6) {
            _authState.update { it.copy(authError = "Mật khẩu phải có ít nhất 6 ký tự.") }
            return
        }
        if (state.password != passwordConfirmation) {
            _authState.update { it.copy(authError = "Mật khẩu xác nhận không khớp.") }
            return
        }
        if (state.username.isBlank()) {
            _authState.update { it.copy(authError = "Vui lòng nhập tên người dùng.") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            try {
                auth.createUserWithEmailAndPassword(state.email, state.password).await()
                auth.signOut()
                _authState.update { it.copy(registrationSuccess = true) }
            } catch (e: Exception) {
                _authState.update { it.copy(authError = "Đăng ký thất bại: ${e.message}") }
            } finally {
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun registrationEventConsumed() {
        _authState.update { it.copy(registrationSuccess = false, authError = null, username = "", email = "", password = "") }
    }

    fun logout() {
        val lastEmail = auth.currentUser?.email
        auth.signOut()
        _authState.update {
            it.copy(
                isAuthenticated = false,
                email = lastEmail ?: "",
                password = "",
                username = ""
            )
        }
    }

    fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, authError = null) }
            try {
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                _authState.update { it.copy(authError = "Đăng nhập bằng Google thất bại: ${e.message}") }
            } finally {
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }
}
