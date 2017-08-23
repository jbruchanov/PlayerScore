package com.scurab.android.playerscore

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by JBruchanov on 23/08/2017.
 */
class PlayersAdapter : RecyclerView.Adapter<PlayerViewHolder>() {

    private val data: MutableList<Player> = mutableListOf()
    private lateinit var layoutInflater: LayoutInflater

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
        holder.score.text = player.score.toString()
        holder.itemView.isSelected = selectedIndex == position
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
        }
    }

    fun onPlayerUpdated(player: Player) {
        notifyItemChanged(data.indexOf(player))
    }
}

class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @BindView(R.id.player_name) lateinit var playerName: TextView
    @BindView(R.id.score) lateinit var score: TextView

    init {
        ButterKnife.bind(this, view)
    }
}