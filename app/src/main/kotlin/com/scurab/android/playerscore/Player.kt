package com.scurab.android.playerscore

/**
 * Created by JBruchanov on 23/08/2017.
 */

typealias PlayerUpdatedListener = (Player) -> Unit

class Player {

    var name: String = ""
    var score: Int = 0
        set(value) {
            field = value
            observers.forEach { it.invoke(this) }
        }

    private val observers: HashSet<PlayerUpdatedListener> = HashSet()

    constructor(name: String, score: Int) {
        this.name = name
        this.score = score
    }

    fun addObserver(observer: PlayerUpdatedListener) {
        observers.add(observer)
    }

    fun removeObserver(observer: PlayerUpdatedListener) {
        observers.remove(observer)
    }
}