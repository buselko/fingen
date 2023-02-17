package com.yoshione.fingen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.yoshione.fingen.balance.BalanceRequest
import com.yoshione.fingen.balance.CreateBalanceResponse
import com.yoshione.fingen.balance.GetBalanceResponse
import com.yoshione.fingen.database.GetDataFromDb
import org.json.JSONObject

class BalanceActivity : AppCompatActivity() {

    private var textBalance: TextView? = null
    var email: String = ""
    var action: String = ""

    private val actionWithBalance = arrayOf<String?>("Пополнение", "Списание", "Создание")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)
        textBalance = findViewById<TextView>(R.id.textBalance)

        setButtonAddPattern()

        setButtonBack()

        actionWithBalance()

        checkBalance()
    }

    private fun checkBalance() {
        if (intent.getStringExtra("balance") == null) {
            val thread = Thread(Runnable {
                try {
                    getBalance()
                } catch (e: Exception) {
                }
            })
            thread.start()
        } else {
            textBalance!!.text = intent.getStringExtra("balance")
        }
    }

    private fun setButtonAddPattern() {
        val buttonAdd = findViewById<Button>(R.id.buttonCreateBalance)
        buttonAdd.setOnClickListener(OnClickListener {
            createBalance()
        })
    }

    private fun setButtonBack() {
        val buttonBack = findViewById<Button>(R.id.buttonHomeBalance)
        buttonBack.setOnClickListener(OnClickListener {
            val intent = Intent(this, ActivityMenuPro::class.java)
            startActivity(intent)
        })
    }

    private fun getBalance() {

        email = GetDataFromDb(this).getData(
            this, "cookies", "email"
        ).toString()

        val url =
            getString(R.string.server_url) +
                    getString(R.string.prefixApi) +
                    getString(R.string.endpoint_get_balance)

        val params = HashMap<String, String>()
        params["email"] = email
        val jsonObject = JSONObject(params as Map<*, *>)

        val response = GetBalanceResponse()
        BalanceRequest().newRequest(this, url, "POST", jsonObject, response)
    }

    private fun createBalance() {
        val url =
            getString(R.string.server_url) +
                    getString(R.string.prefixApi) +
                    getString(R.string.endpoint_create_balance)

        val summa = findViewById<EditText>(R.id.editTextCount).text
        val currency = findViewById<EditText>(R.id.editTextCurrency).text.toString()
        if (summa == null && currency == "") {
            val selection = findViewById<TextView>(R.id.textBalance)
            selection.text = "Заполните все поля"

        } else if (!actionWithBalance.contains(action)) {
            val selection = findViewById<TextView>(R.id.textBalance)
            selection.text = "Выберите действие"

        } else {
            replaceAction()

            val params = HashMap<String, String>()
            params["email"] = email
            params["summa"] = summa.toString()
            params["currency"] = currency
            params["operation"] = action
            val jsonObject = JSONObject(params as Map<*, *>)


            val response = CreateBalanceResponse()
            BalanceRequest().newRequest(this, url, "POST", jsonObject, response)

        }
    }

    private fun replaceAction() {
        action = when (action) {
            "Пополнение" -> {
                "up"
            }
            "Списание" -> {
                "down"
            }
            else -> {
                "create"
            }
        }
    }

    private fun actionWithBalance() {
        val spinner = findViewById<Spinner>(R.id.selectOperationBalance)
        // Адаптер для спиннера
        val adapter = ArrayAdapter(
            this, R.layout.line_in_spinner,
            actionWithBalance
        )
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(R.layout.line_in_spinner)
        // Применяем адаптер к элементу spinner
        spinner.adapter = adapter

        val itemSelectedListener: AdapterView.OnItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    // Получаем выбранный объект
                    val item = parent.getItemAtPosition(position) as String
                    action = item
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        spinner.onItemSelectedListener = itemSelectedListener
    }


}