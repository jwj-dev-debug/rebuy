package com.yourcompany.re_buy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourcompany.re_buy.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 'Sign In' 텍스트 클릭 시 로그인 화면으로 이동
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 'Sign Up' 버튼 클릭 시
        binding.btnSignUp.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val name = binding.etRegisterName.text.toString().trim()
        val phone = binding.etRegisterPhone.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validation
        if (name.isEmpty()) {
            binding.etRegisterName.error = "이름을 입력하세요"
            binding.etRegisterName.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            binding.etRegisterPhone.error = "전화번호를 입력하세요"
            binding.etRegisterPhone.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etRegisterEmail.error = "이메일을 입력하세요"
            binding.etRegisterEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etRegisterEmail.error = "올바른 이메일 형식을 입력하세요"
            binding.etRegisterEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etRegisterPassword.error = "비밀번호를 입력하세요"
            binding.etRegisterPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etRegisterPassword.error = "비밀번호는 최소 6자 이상이어야 합니다"
            binding.etRegisterPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "비밀번호가 일치하지 않습니다"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // Disable button during registration
        binding.btnSignUp.isEnabled = false

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, save user data to Firestore
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val user = User(
                        uid = userId,
                        email = email,
                        name = name,
                        phoneNumber = phone,
                        createdAt = System.currentTimeMillis()
                    )

                    // Save to Firestore
                    firestore.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "회원가입 성공! $name 님 환영합니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Go to MainActivity
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "사용자 정보 저장 실패: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnSignUp.isEnabled = true
                        }
                } else {
                    // Registration failed
                    Toast.makeText(
                        this,
                        "회원가입 실패: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSignUp.isEnabled = true
                }
            }
    }
}