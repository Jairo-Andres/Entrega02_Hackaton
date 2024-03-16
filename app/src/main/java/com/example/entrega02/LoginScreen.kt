package com.example.entrega02

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entrega02.databinding.LoginScreenBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


private const val FILE_NAME = "accounts.txt"

class LoginScreen : AppCompatActivity() {
    private lateinit var binding: LoginScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        var credentialsCorrect = false
        var isPropietary = false;
        super.onCreate(savedInstanceState)


        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var accounts : ArrayList<Account> = readAccountsFromTxtFile(this)
        var logInButton = findViewById<Button>(R.id.btnLogin)
        var usernameText = findViewById<EditText>(R.id.editTextUsername)
        var passwordText = findViewById<EditText>(R.id.editTextPassword)

        logInButton.setOnClickListener {
            val username = usernameText.text.toString()
            val password = passwordText.text.toString()
            for(account in accounts)
            {
                if(account.username == username && account.password == password)
                {
                    credentialsCorrect = true
                    isPropietary = account.propietary
                    break
                }
            }
            if(credentialsCorrect)
            {
                credentialsCorrect= false
                if(!isPropietary)
                {
                    startActivity(Intent(this, TouristScreen::class.java))

                }
                else
                {
                    isPropietary = false
                    startActivity(Intent(this, PropietaryScreen::class.java))
                }
            }
            else
            {
                Toast.makeText(this, "Credenciales incorrectas, intenta de nuevo :(", Toast.LENGTH_SHORT).show()
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
                    val premium= parts[2].toBoolean()

                    // Create an Account object and add it to the list
                    val account = Account(username, password, premium)
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

}

