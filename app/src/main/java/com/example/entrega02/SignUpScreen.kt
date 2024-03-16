package com.example.entrega02

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entrega02.databinding.SignupScreenBinding
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter


private const val FILE_NAME = "accounts.txt"

class SignUpScreen : AppCompatActivity() {
    private lateinit var binding: SignupScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        var dittoAccount = false
        super.onCreate(savedInstanceState)
        binding = SignupScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var accounts : ArrayList<Account> = readAccountsFromTxtFile(this)
        var signUpButton = findViewById<Button>(R.id.btnSignUp)
        var usernameText = findViewById<EditText>(R.id.SignUpUsername)
        var passwordText = findViewById<EditText>(R.id.SignUpPassword)
        var checkPasswordText = findViewById<EditText>(R.id.checkPasswordText)
        var propietaryCheckBox = findViewById<CheckBox>(R.id.propietaryCheckbox)
        signUpButton.setOnClickListener {
            val username = usernameText.text.toString()
            val password = passwordText.text.toString()
            val checkPassword = checkPasswordText.text.toString()
            val ispropietary = propietaryCheckBox.isChecked
            for(account in accounts)
            {
                if(account.username == username)
                {
                    dittoAccount = true
                    break
                }
            }
            if(!dittoAccount)
            {
                if(password == checkPassword)
                {
                    Toast.makeText(this, "Cuenta Creada exitosamente, ya puede iniciar sesión :)", Toast.LENGTH_SHORT).show()
                    addAccountToFile(this, Account(username, password, ispropietary))

                    finish()
                }
                else
                {
                    Toast.makeText(this, "Error, los campos de las contraseñas no coinciden :(", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Error, este nombre de usuario ya está en uso :(", Toast.LENGTH_SHORT).show()
                dittoAccount = false
            }
        }
    }
    fun readAccountsFromTxtFile(context: Context): ArrayList<Account> {
        val accountList = ArrayList<Account>()

        try {
            // Open the file from the internal storage
            val file = File(context.filesDir, FILE_NAME)
            val reader = BufferedReader(FileReader(file))

            var line: String?

            // Read each line from the file
            while (reader.readLine().also { line = it } != null) {
                val parts = line?.split(" ")

                if (parts?.size == 3) {
                    val username = parts[0]
                    val password = parts[1]
                    val propietary = parts[2].toBoolean()

                    // Create an Account object and add it to the list
                    val account = Account(username, password, propietary)
                    accountList.add(account)
                }
            }

            // Close the BufferedReader when done
            reader.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return accountList
    }

    fun addAccountToFile(context: Context, newAccount: Account) {
        try {
            // Open the file for appending
            val file = File(context.filesDir, FILE_NAME)
            val writer = BufferedWriter(FileWriter(file, true))

            // Write the new account to the file
            writer.write("${newAccount.username} ${newAccount.password} ${newAccount.propietary}\n")

            // Close the BufferedWriter when done
            writer.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

