package com.scurab.android.playerscore

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.fabric.sdk.android.Fabric
import java.util.*


private val Players = "Players"

class MainActivity : AppCompatActivity() {

    @BindView(R.id.recycler_view) lateinit var recyclerView: RecyclerView
    @BindView(R.id.input) lateinit var input: EditText

    val gson: Gson = Gson()
    lateinit var sharedPrefs: SharedPreferences
    lateinit var adapter: PlayersAdapter
    var numberOfColors : Int = 0
    val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("PlayerScore", Context.MODE_PRIVATE)
        numberOfColors = resources.obtainTypedArray(R.array.colors).length()

        ButterKnife.bind(this)

        adapter = PlayersAdapter(input)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sharedPrefs.getString(Players, null)?.let {
            val items = gson.fromJson<List<Player>>(it, object : TypeToken<List<Player>>() {}.type)
            adapter.addAllPlayers(items)
        }

        input.setOnEditorActionListener { textView, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_GO) {
                onActionGo(textView)
            }
            true
        }
        input.filters = arrayOf(ScoreInputFilter())

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
            private var selectedPlayer : Player? = null

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removePlayer(adapter.items[viewHolder.adapterPosition])
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                selectedPlayer = adapter.selectedPlayer()
                adapter.switchPosition(viewHolder.adapterPosition, target.adapterPosition)
                selectedPlayer?.let {
                    adapter.setSelectedIndex(adapter.items.indexOf(it), false)
                }
                return true
            }
        }).attachToRecyclerView(recyclerView)
    }

    fun onActionGo(textView: TextView) {
        val selectedPlayer = adapter.selectedPlayer()
        if (selectedPlayer != null) {
            val input = textView.text.toString()
            if(input.isNotEmpty()) {
                var int = input.toIntOrNull() ?: 0
                when (int) {
                    in 1..99 -> int *= 100
                    else -> int
                }
                if (int in 1..299 || ((int / 100) * 100) != int) {
                    return
                }
                selectedPlayer.score += int
                textView.text = ""
                if (selectedPlayer.lastZeros() == 3) {
                    selectedPlayer.clear()
                }
                adapter.setSelectedIndex((adapter.selectedIndex + 1) % adapter.items.count())
                if (adapter.selectedIndex == 0) {
                    adapter.items.maxBy { p -> p.score }
                            ?.let {
                                if (it.score >= 10000) {
                                    onWinner(it)
                                    adapter.setSelectedIndex(adapter.items.indexOf(it))
                                }
                            }
                }
                recyclerView.post { recyclerView.smoothScrollToPosition(Math.max(0, adapter.selectedIndex)) }
            }
        }
    }

    fun onWinner(player: Player) {
        AlertDialog.Builder(this)
                .setTitle(R.string.winner)
                .setMessage(player.name)
                .setPositiveButton(R.string.action_ok, null)
                .setNegativeButton(R.string.action_cancel, null)
                .show()
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
        val input = EditText(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.input).toFloat())
        }
        val fl = FrameLayout(this).apply {
            var pad = resources.getDimensionPixelSize(R.dimen.gap_normal)
            setPadding(pad, pad, pad, pad)
            addView(input)
        }
        AlertDialog.Builder(this)
                .setTitle(R.string.name)
                .setView(fl)
                .setPositiveButton(R.string.action_ok) { d, i ->
                    val player = Player(input.text.toString(), 0)
                    player.clear()
                    player.colorIndex = random.nextInt(numberOfColors)
                    adapter.addPlayer(player)
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    fun onReset() {
        AlertDialog.Builder(this)
                .setTitle(R.string.ru_sure)
                .setPositiveButton(R.string.action_ok) { d, i ->
                    val player = adapter.selectedPlayer()
                    if (player != null) {
                        player.clear()
                    } else {
                        adapter.items.forEach { it.clear() }
                        adapter.setSelectedIndex(-1)
                    }
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
