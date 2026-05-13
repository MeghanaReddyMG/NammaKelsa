package com.nammakelsa.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.data.local.UserDao
import com.nammakelsa.data.local.UserEntity
import com.nammakelsa.data.mapper.toEntity
import com.nammakelsa.data.mapper.toModel
import com.nammakelsa.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Get user from local database (Room)
    fun getLocalUser(uid: String): Flow<User?> {
        return userDao.getUser(uid).map { entity ->
            entity?.toModel()
        }
    }

    suspend fun getUserSync(uid: String): User? {
        return userDao.getUser(uid).first()?.toModel()
    }

    fun saveUserLocally(user: User) {
        scope.launch {
            userDao.insertUser(user.toEntity())
        }
    }

    fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
