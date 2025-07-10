package com.example.cryptowallet.data.auth

data class User(val email: String, val passwordHash: String)

object UserDatabase {
    private val users = mutableMapOf<String, User>()

    init {
        users["user@email.com"] = User("user@email.com", "password123".hashCode().toString())
    }

    fun findUserByEmail(email: String): User? {
        return users[email.lowercase()]
    }

    fun registerUser(email: String, password: String): Boolean {
        val lowerCaseEmail = email.lowercase()
        if (users.containsKey(lowerCaseEmail)) {
            return false
        }
        users[lowerCaseEmail] = User(lowerCaseEmail, password.hashCode().toString())
        return true
    }
}
