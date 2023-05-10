package com.example.airsignal_app.view.test

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.view.SegmentedProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestDesignActivity : AppCompatActivity() {
    private val testItem = arrayListOf<AdapterModel.TestItem>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_design)


        val createBtn: Button = findViewById(R.id.testCreateBtn)
        val textSizeEt: EditText = findViewById(R.id.testTextSize)
        val textColorEt: EditText = findViewById(R.id.testTextColor)
        val fontSpinner: Spinner = findViewById(R.id.testFont)
        val textValueEt: EditText = findViewById(R.id.testTextValue)
        val clearBtn: Button = findViewById(R.id.testClearBtn)
        setSpinner(fontSpinner)

        val adapter = TestAdapter(this, testItem)
        val testRv = findViewById<RecyclerView>(R.id.testRv)
        testRv.adapter = adapter

        createBtn.setOnClickListener {
            textValueEt.text?.toString()?.let { value ->
                textSizeEt.text?.toString()?.let { size ->
                    textColorEt.text?.toString()?.let { color ->
                        fontSpinner.selectedItem?.toString()?.let { font ->
                            addItem(value, size, color, font)
                        }
                    }
                }
            }

            adapter.notifyItemInserted(testItem.size)
        }

        clearBtn.setOnClickListener {
            testItem.clear()
            adapter.notifyDataSetChanged()
        }

        adapter.setOnItemClickListener(object : TestAdapter.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    testItem.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
            }
        })
    }

    private fun addItem(value: String, size: String, color: String, font: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val item = AdapterModel.TestItem(font, size, color, value)
            testItem.add(item)
        }
    }

    private fun setSpinner(spinner: Spinner) {
        val items = resources.getStringArray(R.array.font)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,items)
        spinner.adapter = adapter
    }
}