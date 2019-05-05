package com.scurab.android.playerscore

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Created by JBruchanov on 23/08/2017.
 */

private val INDEX_BELOW_3k = 0
private val INDEX_BELOW_7k = 1
private val INDEX_BELOW_10k = 2
private val INDEX_ABOVE_10k = 3

class PlayersAdapter(val input: EditText) : RecyclerView.Adapter<PlayerViewHolder>() {

    private val data: MutableList<Player> = mutableListOf()
    private lateinit var layoutInflater: LayoutInflater
    private val numberFormat : DecimalFormat
    val items : List<Player> = data
    val colors: MutableList<Int>
    var scoreColors : Array<Int>

    var selectedIndex: Int = -1
    private set

    fun setSelectedIndex(value: Int, notify : Boolean = true){
        val old = selectedIndex
        selectedIndex = if (value < itemCount) value else -1
        if (notify) {
            if (old != selectedIndex) {
                notifyItemChanged(old)
                if (value != -1) {
                    notifyItemChanged(value)
                }
            }
            if (old == -1 && value != -1) {
                input.requestFocus()
                val imm = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            } else if (old != -1 && value == -1) {
                val imm = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(input.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
            }
        }
    }

    init {
        var symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = ' '
        numberFormat = DecimalFormat("###,###", symbols)

        val res = input.resources
        val ta = res.obtainTypedArray(R.array.colors)
        colors = mutableListOf()
        for (i in 0..ta.length() - 1) {
            colors.add(ta.getColor(i, 0))
        }

        scoreColors =
                arrayOf(res.getColor(R.color.score_low),
                        res.getColor(R.color.score_mid),
                        res.getColor(R.color.score_mid2),
                        res.getColor(R.color.score_winning))
    }

    fun selectedPlayer(): Player? = if (selectedIndex != -1) data[selectedIndex] else null

    override fun getItemCount(): Int = data.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutInflater = LayoutInflater.from(recyclerView.context)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = data[position]
        holder.playerName.text = player.name
        holder.score.text = numberFormat.format(player.score)
        holder.itemView.isSelected = selectedIndex == position
        holder.background.setColor(colors[player.colorIndex])
        val zeros = player.lastZeros()
        when (zeros) {
            2 -> {
                holder.point1.visibility = View.VISIBLE; holder.point2.visibility = View.VISIBLE
            }
            1 -> {
                holder.point1.visibility = View.VISIBLE; holder.point2.visibility = View.GONE
            }
            else -> {
                holder.point1.visibility = View.GONE; holder.point2.visibility = View.GONE
            }
        }
        val scoreIndex = when (player.score) {
            in Long.MIN_VALUE..3000 -> INDEX_BELOW_3k
            in 3000..7000 -> INDEX_BELOW_7k
            in 7000..9999 -> INDEX_BELOW_10k
            else -> INDEX_ABOVE_10k
        }
        holder.score.setTextColor(scoreColors[scoreIndex])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder(layoutInflater.inflate(R.layout.item_player, parent, false)).apply {
            itemView.setOnClickListener {
                setSelectedIndex(if (selectedIndex == adapterPosition) -1 else adapterPosition)
            }
        }
    }

    fun addPlayer(player: Player) {
        data.add(player)
        player.addObserver(this::onPlayerUpdated)
        notifyItemInserted(itemCount)
    }

    fun addAllPlayers(players: Iterable<Player>) {
        val start = itemCount
        data.addAll(players)
        players.forEach { it.addObserver(this::onPlayerUpdated) }
        notifyItemRangeInserted(start, players.count())
    }

    fun removePlayer(player: Player) {
        val index = data.indexOf(player)
        if (index != -1) {
            data.remove(player)
            notifyItemRemoved(index)
            if (index == selectedIndex) {
                selectedIndex = -1
            }
        }
    }

    fun onPlayerUpdated(player: Player) {
        notifyItemChanged(data.indexOf(player))
    }

    fun switchPosition(pos1: Int, pos2: Int) {
        val selectedPlayer = selectedPlayer()
        val o1 = data[pos1]
        val o2 = data[pos2]
        data[pos1] = o2
        data[pos2] = o1
        notifyItemMoved(pos1, pos2)
        if (selectedPlayer != null) {
            selectedIndex = data.indexOf(selectedPlayer)
        }
    }
}

class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @BindView(R.id.avatar) lateinit var avatar: ImageView
    @BindView(R.id.player_name) lateinit var playerName: TextView
    @BindView(R.id.score) lateinit var score: TextView
    @BindView(R.id.point1) lateinit var point1: View
    @BindView(R.id.point2) lateinit var point2: View
    var background: GradientDrawable

    init {
        ButterKnife.bind(this, view)
        background = (avatar.drawable as? LayerDrawable)?.findDrawableByLayerId(R.id.background) as GradientDrawable
    }
}