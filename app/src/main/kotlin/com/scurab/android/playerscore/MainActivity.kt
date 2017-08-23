package com.scurab.android.playerscore

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

class MainActivity : AppCompatActivity() {

    @BindView(R.id.recycler_view) lateinit var recyclerView: RecyclerView
    @BindView(R.id.input) lateinit var input: EditText

    lateinit var adapter: PlayersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        adapter = PlayersAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.addPlayer(Player("Name", 0))
        adapter.addPlayer(Player("X", 0))
        adapter.addPlayer(Player("Y", 0))
        adapter.addPlayer(Player("Z", 0))

        input.setOnEditorActionListener { textView, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_GO) {
                val selectedPlayer = adapter.selectedPlayer()
                if(selectedPlayer != null) {
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
            R.id.reset-> onReset()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onAddPlayer() {

    }

    fun onReset() {
        AlertDialog.Builder(this)
                .setTitle(R.string.ru_sure)
                .setPositiveButton(R.string.action_ok) { d, i ->
                    adapter.items.forEach { it.score = 0 }
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
}
