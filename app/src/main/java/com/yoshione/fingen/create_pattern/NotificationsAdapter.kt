package com.yoshione.fingen.create_pattern

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.yoshione.fingen.R

class NotificationsAdapter internal constructor(
    private var ctx: Context,
    var objects: ArrayList<NotificationInfo>
) :
    BaseAdapter() {
    private val actionWithBalance = arrayOf<String?>("Пополнение", "Списание")

    // кол-во элементов
    override fun getCount(): Int {
        return objects.size
    }

    // элемент по позиции
    override fun getItem(position: Int): Any {
        return objects[position]
    }

    // id по позиции
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // пункт списка
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // if convertView is null then inflate the layout
        val view = convertView ?: LayoutInflater.from(ctx).inflate(
            R.layout.string_in_line_notification, parent, false
        )
        val appInfo = getProduct(position)

        setViews(view, appInfo.title!!, appInfo.packageName!!, appInfo.text!!)

        setCheckBox(view, position, appInfo.isActive)

        recycleView(view, position)

        return view
    }

    private fun setCheckBox(view: View, position: Int, isActive: Boolean) {
        val cbBuy = view.findViewById<View>(R.id.cbBox) as CheckBox
//         присваиваем чекбоксу обработчик
        cbBuy.setOnCheckedChangeListener(myCheckChangeList)
//         пишем позицию
        cbBuy.tag = position
        cbBuy.isChecked = isActive
    }

    private fun setViews(view: View, title: String, packageName: String, text: String) {

        (view.findViewById<View>(R.id.titleNotification) as TextView).text = title
        (view.findViewById<View>(R.id.packageNameNotification) as TextView).text =
            packageName
        (view.findViewById<View>(R.id.notificationText) as TextView).text = text
    }

    fun getAction(position: Int): String? {
        return objects[position].action
    }

    // товар по позиции
    private fun getProduct(position: Int): NotificationInfo {
        return getItem(position) as NotificationInfo
    }

    // обработчик для чекбоксов
    private val myCheckChangeList =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            val position = buttonView.tag as Int
            objects[position].isActive = isChecked
//            getProduct(buttonView.tag as Int).isActive = isChecked
        }

    private fun recycleView(view: View, positionSpinner: Int): Spinner? {
        val spinner = view.findViewById<Spinner>(R.id.selectAction)
        // Адаптер для спиннера
        val adapter = ArrayAdapter(
            ctx, R.layout.line_notification,
            actionWithBalance
        )

        adapter.setDropDownViewResource(R.layout.line_in_spinner)

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
                    if (item == "Пополнение") {
                        objects[positionSpinner].action = "up"
                    } else {
                        objects[positionSpinner].action = "down"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {


                }            }
        spinner.onItemSelectedListener = itemSelectedListener
        return spinner
    }
}

