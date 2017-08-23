package com.scurab.android.playerscore

import java.util.*

/**
 * Created by JBruchanov on 23/08/2017.
 */

typealias PlayerUpdatedListener = (Player) -> Unit
private val ScoreHistoryLimit = 3

@Suppress("JoinDeclarationAndAssignment")
class Player {

    var name: String = ""
    var score: Long = 0
        set(value) {
            if (value > 0) {
                addToStack(value - field)
            }
            field = value
//            if (lastThreeScoreEquals(0)) {
//                field = 0
//            }
            observers.forEach { it.invoke(this) }
        }

    private val scoreHistory : Stack<Long> = Stack()

    @Transient
    private val observers: HashSet<PlayerUpdatedListener>

    constructor() {
        observers = HashSet()
    }

    constructor(name: String, score: Long) : this() {
        this.name = name
        this.score = score
    }

    fun addObserver(observer: PlayerUpdatedListener) {
        observers.add(observer)
    }

    fun removeObserver(observer: PlayerUpdatedListener) {
        observers.remove(observer)
    }

    private fun addToStack(value: Long) {
        scoreHistory.push(value)
//        while (scoreHistory.size > ScoreHistoryLimit) {
//            scoreHistory.removeElementAt(scoreHistory.size - 1)
//        }
    }

    fun lastThreeScoreEquals(value: Long): Boolean {
        return scoreHistory.size == ScoreHistoryLimit
                && scoreHistory.filter { it == value }.size == ScoreHistoryLimit
    }
}