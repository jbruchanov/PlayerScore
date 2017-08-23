package com.scurab.android.playerscore

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
}
