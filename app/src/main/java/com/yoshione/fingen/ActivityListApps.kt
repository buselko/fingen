package com.yoshione.fingen

import android.Manifest.permission.QUERY_ALL_PACKAGES
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yoshione.fingen.appsForDevice.AppsInfo
import com.yoshione.fingen.appsForDevice.ListAppAdapter


class ActivityListApps : AppCompatActivity() {

    private var listAppAdapter: ListAppAdapter? = null

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val lvMain = findViewById<View>(R.id.listNotificationForPattern) as ListView
        // создаем адаптер
        val thread = Thread(Runnable {
            val products = fillData()
            listAppAdapter = ListAppAdapter(this, products)

            // настраиваем список
            lvMain.post(Runnable {
                lvMain.adapter = listAppAdapter
            })
        })
        thread.start()
        setSearchView(this)
    }

    // генерируем данные для адаптера
    @SuppressLint("Range")
    private fun fillData(): ArrayList<AppsInfo> {
//
        var products = ArrayList<AppsInfo>()
        val db = DBHelper(this)
        val dbh = db.writableDatabase
// получаем данные приложений
        val pack: PackageManager = packageManager

        if (ContextCompat.checkSelfPermission(
                this,
                QUERY_ALL_PACKAGES
            ) != PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(this, arrayOf(QUERY_ALL_PACKAGES), 1)
            } else {
            }

        } else {
        }
        val installedApps = pack.getInstalledApplications(GET_META_DATA)
        try {
            for (i in installedApps.indices) {
                val app = installedApps[i]
                val name = app.loadLabel(pack).toString()
                val packageName = app.packageName
                val icon = app.loadIcon(pack)
                var isActiveApp = false
                val cursor = dbh.rawQuery(
                    "SELECT isActive FROM apps WHERE package_name = '$packageName'",
                    null
                )
                if (cursor.moveToFirst()) {
                    isActiveApp = cursor.getInt(cursor.getColumnIndex("isActive")) == 1
                }
                cursor.close()
                if (!packageName.startsWith("com.android")) {
                    products.add(AppsInfo(name, packageName, icon, isActiveApp))
                } else if (packageName == "com.android.messaging") {
                    products.add(AppsInfo(name, packageName, icon, isActiveApp))
                }
            }
            return products
        } catch (e: Exception) {
        } finally {
            dbh.close()
            db.close()
        }
        return products

    }


    fun buttonHome(view: View) {
        val intent = Intent(this, ActivityMenuPro::class.java)
        startActivity(intent)
    }

    // listener search view
    fun setSearchView(ctx: Context): Boolean {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setIconifiedByDefault(false)

        searchView.queryHint = "Search"

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return try {
                        val lvMain = findViewById<View>(R.id.listNotificationForPattern) as ListView
                        val r = listAppAdapter!!.objects.filter { it.name.contains(newText, true) }
                        lvMain.adapter = ListAppAdapter(ctx, ArrayList(r))
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            })
        return true
    }
}