package com.yoshione.fingen.appsForDevice

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.yoshione.fingen.AddPatternsActivity
import com.yoshione.fingen.DBHelper
import com.yoshione.fingen.R

class ListAppAdapter internal constructor(
    var ctx: Context,
    var objects: ArrayList<AppsInfo>
) :
    BaseAdapter() {

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
            R.layout.string_in_list_view, parent, false
        )
        val appInfo = getProduct(position)

        setViews(view, appInfo.name, appInfo.package_name, appInfo.icon)

        setCheckBox(view, appInfo.isActive, position)

        setHandlerButton(view, position)

        return view
    }

    private fun setHandlerButton(view: View, position: Int) {
        val addPattern = view.findViewById<View>(R.id.AddPatterns) as Button
        addPattern.setOnClickListener(onClickMyButton)
        addPattern.tag = position
    }

    private fun setViews(view: View, name: String, package_name: String, icon: Drawable) {
        (view.findViewById<View>(R.id.titleNotification) as TextView).text = name
        (view.findViewById<View>(R.id.notificationText) as TextView).text = package_name
        (view.findViewById<View>(R.id.ivImage) as ImageView).setImageDrawable(icon)
    }

    private fun setCheckBox(view: View, isActive: Boolean, position: Int) {
        val cbBuy = view.findViewById<View>(R.id.cbBox) as CheckBox
//         присваиваем чекбоксу обработчик
        cbBuy.setOnCheckedChangeListener(myCheckChangeList)
//         пишем позицию
        cbBuy.tag = position
        cbBuy.isChecked = isActive
    }


    // товар по позиции
    private fun getProduct(position: Int): AppsInfo {
        return getItem(position) as AppsInfo
    }

    // обработчик для чекбоксов
    private val myCheckChangeList =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            val db = DBHelper(ctx)
            val dbh = db.writableDatabase
            val position = buttonView.tag as Int
            objects[position].isActive = isChecked
            try {
                // изменяем базу данных

                val app_db =
                    dbh.query(
                        "apps",
                        null,
                        "package_name=?",
                        arrayOf(objects[position].package_name),
                        null,
                        null,
                        null
                    )
                val values = ContentValues()
                values.put("package_name", objects[position].package_name)
                values.put("isActive", if (isChecked) 1 else 0)
                if (app_db.count > 0) {
                    dbh.update(
                        "apps",
                        values,
                        "package_name=?",
                        arrayOf(objects[position].package_name)
                    )
                } else {
                    dbh.insert("apps", null, values)
                }
                app_db.close()
            } catch (e: Exception) {
            }
            finally {
                dbh.close()
                db.close()
            }

        }

    private val onClickMyButton = View.OnClickListener { view ->
        val intent = Intent(ctx, AddPatternsActivity::class.java)
        intent.putExtra("package_name", getProduct(view.tag as Int).package_name)
        ctx.startActivity(intent)
    }
}