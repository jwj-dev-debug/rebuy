package com.yourcompany.re_buy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 'Sign Up' 텍스트 클릭 시 회원가입 화면으로 이동
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // 'Sign In' 버튼 클릭 시
        binding.btnSignIn.setOnClickListener {
            performLogin()
        }

        // 'Go to Home' 버튼 클릭 시 (게스트 모드)
        binding.btnGoToHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validation
        if (email.isEmpty()) {
            binding.etEmail.error = "이메일을 입력하세요"
            binding.etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "올바른 이메일 형식을 입력하세요"
            binding.etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "비밀번호를 입력하세요"
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "비밀번호는 최소 6자 이상이어야 합니다"
            binding.etPassword.requestFocus()
            return
        }

        // Disable button during login
        binding.btnSignIn.isEnabled = false

        // Sign in with Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnSignIn.isEnabled = true

                if (task.isSuccessful) {
                    // Login successful
                    Toast.makeText(
                        this,
                        "로그인 성공!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Login failed
                    Toast.makeText(
                        this,
                        "로그인 실패: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}