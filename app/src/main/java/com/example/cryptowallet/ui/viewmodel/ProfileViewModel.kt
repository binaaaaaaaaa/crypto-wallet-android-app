package com.example.cryptowallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Data class để chứa thông tin chi tiết của người dùng.
 */
data class UserProfile(
    val name: String,
    val email: String,
    val uid: String,
    val avatarUrl: String,
    val country: String,
    val verificationStatus: String,
    val feeLevel: String
)

/**
 * Lớp trạng thái (State) cho giao diện màn hình Hồ sơ.
 * @param userProfile Đối tượng chứa thông tin người dùng.
 * @param isCountrySelectionDialogVisible Cờ để điều khiển việc hiển thị hộp thoại chọn quốc gia.
 * @param isPasswordWarningDialogVisible Cờ để điều khiển việc hiển thị hộp thoại cảnh báo mật khẩu.
 * @param isSeedPhraseWarningDialogVisible Cờ để điều khiển việc hiển thị hộp thoại cảnh báo cụm từ khôi phục.
 */
data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isCountrySelectionDialogVisible: Boolean = false,
    val isPasswordWarningDialogVisible: Boolean = false,
    val isSeedPhraseWarningDialogVisible: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.value = ProfileUiState(
            userProfile = UserProfile(
                name = "Binaaa",
                email = "ngu***@gmail.com",
                uid = "557227760964673762",
                avatarUrl = "https://i.pravatar.cc/150", // lấy ảnh random
                country = "Việt Nam",
                verificationStatus = "Đã xác minh",
                feeLevel = "Người dùng thông thường"
            )
        )
    }

    /**
     * Xử lý khi người dùng chọn một quốc gia mới từ hộp thoại.
     * @param newCountry Quốc gia mới được chọn.
     */
    fun onCountrySelected(newCountry: String) {
        _uiState.update { currentState ->
            currentState.copy(
                userProfile = currentState.userProfile?.copy(country = newCountry),
                isCountrySelectionDialogVisible = false
            )
        }
    }

    fun showCountryDialog() {
        _uiState.update { it.copy(isCountrySelectionDialogVisible = true) }
    }

    fun hideCountryDialog() {
        _uiState.update { it.copy(isCountrySelectionDialogVisible = false) }
    }

    fun showPasswordWarningDialog() {
        _uiState.update { it.copy(isPasswordWarningDialogVisible = true) }
    }

    fun hidePasswordWarningDialog() {
        _uiState.update { it.copy(isPasswordWarningDialogVisible = false) }
    }

    fun showSeedPhraseWarningDialog() {
        _uiState.update { it.copy(isSeedPhraseWarningDialogVisible = true) }
    }

    fun hideSeedPhraseWarningDialog() {
        _uiState.update { it.copy(isSeedPhraseWarningDialogVisible = false) }
    }
}
