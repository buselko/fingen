package com.yoshione.fingen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.yoshione.fingen.database.GetDataFromDb
import com.yoshione.fingen.patterns.AddPatternRequest
import com.yoshione.fingen.patterns.AddPatternResponse
import org.json.JSONObject


class AddPatternsActivity : AppCompatActivity() {
    private var actionWithBalance = arrayOf<String?>("Пополнение", "Списание")
    private var packageName: String? = null
    var email = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        email = GetDataFromDb(this).getData(this, "cookies", "email").toString()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patterns)
        val title = intent.getStringExtra("package_name")
        packageName = title
        val selection = findViewById<TextView>(R.id.textViewSuccessesAddTemplate)

        val spinner = createSpinner(selection, title)

        val createTemplates = findViewById<View>(R.id.createTemplates)
        createTemplates.setOnClickListener {
            val intent = Intent(this, CreatePatternActivity::class.java)
            intent.putExtra("package_name", packageName)
            startActivity(intent)
        }
    }

    fun sendPatterns(view: View) {
        val patternText = findViewById<EditText>(R.id.editTextTextMultiLine)
        var action = findViewById<Spinner>(R.id.spinner).selectedItem.toString()

        if (patternText.text.toString() == "") {
            val selection = findViewById<TextView>(R.id.textViewSuccessesAddTemplate)
            selection.text = "Не введен шаблон"

        } else if (action != "Пополнение" && action != "Списание") {
            val selection = findViewById<TextView>(R.id.textViewSuccessesAddTemplate)
            selection.text = "Не выбрана операция"

        } else {
            // отправляем запрос на сервер для записи и обработки паттерна
            val url =
                getString(R.string.server_url) + getString(R.string.prefixApi) + getString(R.string.endpoint_addPattern)
            if (action == "Пополнение") {
                action = "up"
            } else {
                action = "down"
            }

            val params = HashMap<String, String>()
            // get email
            params["email"] = email
            params["pattern"] = patternText.text.toString()
            params["package_name"] = packageName.toString()
            params["action"] = action
            val jsonObject = JSONObject(params as Map<*, *>)

            val response = AddPatternResponse()

            val thread = Thread(Runnable {
                AddPatternRequest().newRequest(this, url, "POST", jsonObject, response)
            })
            thread.start()

            val selection = findViewById<TextView>(R.id.textViewSuccessesAddTemplate)
            selection.text = "Отправил шаблон на $action"
            patternText.text = null
        }
    }

    fun buttonBackOnAddPatterns(view: View) {
        val intent = Intent(this, ActivityListApps::class.java)
        startActivity(intent)
    }

    private fun createSpinner(selection: TextView, title: String?): Spinner? {
        val spinner = findViewById<Spinner>(R.id.spinner)
        // Адаптер для спиннера
        val adapter = ArrayAdapter(
            this, R.layout.line_in_spinner,
            actionWithBalance
        )
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(R.layout.line_in_spinner)
        // Применяем адаптер к элементу spinner
        spinner.adapter = adapter
        selectItemSpinner(spinner, selection, title)
        return spinner
    }

    private fun selectItemSpinner(spinner: Spinner, selection: TextView, title: String?) {
        val itemSelectedListener: AdapterView.OnItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val item = parent.getItemAtPosition(position) as String
                    selection.text = title + "\n" + item + "\n"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        spinner.onItemSelectedListener = itemSelectedListener
    }
}
