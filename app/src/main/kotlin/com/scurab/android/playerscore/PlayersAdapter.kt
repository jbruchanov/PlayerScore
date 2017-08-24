package com.scurab.android.playerscore

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Created by JBruchanov on 23/08/2017.
 */
class PlayersAdapter(val input: EditText) : RecyclerView.Adapter<PlayerViewHolder>() {

    private val data: MutableList<Player> = mutableListOf()
    private lateinit var layoutInflater: LayoutInflater
    private val numberFormat : DecimalFormat
    val items : List<Player> = data
    val colors: MutableList<Int>

    var selectedIndex: Int = -1
        set(value) {
            val old = field
            field = if (value < itemCount) value else 0
            if (old != field) {
                notifyItemChanged(old)
                if (value != -1) {
                    notifyItemChanged(field)
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

    init {
        var symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = ' '
        numberFormat = DecimalFormat("###,###", symbols)

        val ta = input.resources.obtainTypedArray(R.array.colors)
        colors = mutableListOf()
        for (i in 0..ta.length() - 1) {
            colors.add(ta.getColor(i, 0))
        }
    }

    fun selectedPlayer(): Player? = if (selectedIndex != -1) data[selectedIndex] else null

    override fun getItemCount(): Int = data.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutInflater = LayoutInflater.from(recyclerView.context)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder(layoutInflater.inflate(R.layout.item_player, parent, false)).apply {
            itemView.setOnClickListener {
                selectedIndex = if (selectedIndex == adapterPosition) -1 else adapterPosition
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