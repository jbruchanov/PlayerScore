package com.scurab.android.playerscore

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val Players = "Players"

class MainActivity : AppCompatActivity() {

    @BindView(R.id.recycler_view) lateinit var recyclerView: RecyclerView
    @BindView(R.id.input) lateinit var input: EditText

    val gson: Gson = Gson()
    lateinit var sharedPrefs: SharedPreferences
    lateinit var adapter: PlayersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("PlayerScore", Context.MODE_PRIVATE)

        ButterKnife.bind(this)

        adapter = PlayersAdapter(input)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sharedPrefs.getString(Players, null)?.let {
            val items = gson.fromJson<List<Player>>(it, object : TypeToken<List<Player>>() {}.type)
            adapter.addAllPlayers(items)
        }

        input.setOnEditorActionListener { textView, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_GO) {
                val selectedPlayer = adapter.selectedPlayer()
                if (selectedPlayer != null) {
                    val int = textView.text.toString().toIntOrNull() ?: 0
                    selectedPlayer.score += int
                    adapter.selectedIndex++
                    recyclerView.smoothScrollToPosition(adapter.selectedIndex)
                    textView.text = ""
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> onAddPlayer()
            R.id.delete -> onDeletePlayer()
            R.id.reset -> onReset()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onAddPlayer() {
        val input = EditText(this)
        AlertDialog.Builder(this)
                .setTitle(R.string.name)
                .setView(input)
                .setPositiveButton(R.string.action_ok) { d, i ->
                    adapter.addPlayer(Player(input.text.toString(), 0))
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    fun onReset() {
        AlertDialog.Builder(this)
                .setTitle(R.string.ru_sure)
                .setPositiveButton(R.string.action_ok) { d, i ->
                    adapter.items.forEach { it.score = 0 }
                    adapter.selectedIndex = 0
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()

    }

    fun onDeletePlayer() {
        adapter.selectedPlayer()?.let {
            AlertDialog.Builder(this)
                    .setTitle(R.string.ru_sure)
                    .setPositiveButton(R.string.action_ok) { d, i ->
                        adapter.removePlayer(it)
                    }
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
        }
    }

    override fun onPause() {
        super.onPause()
        sharedPrefs.edit().putString(Players, gson.toJson(adapter.items)).apply()
    }
}
