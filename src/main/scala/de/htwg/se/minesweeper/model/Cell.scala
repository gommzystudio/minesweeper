package de.htwg.se.minesweeper.model

case class Cell(isRevealed: Boolean, isBomb: Boolean, isFlagged: Boolean = false, nearbyBombs: Int = 0) {
	override def toString: String = {
		val bombChar = "☒"
		val revealedChar = "☐"
		val hiddenChar = "#"
		val flaggedChar = "⚑"

		if isRevealed then
			if isBomb then bombChar
			else if nearbyBombs == 0 then revealedChar else nearbyBombs.toString
		else if isFlagged then flaggedChar
		else hiddenChar
	}

	def asRevealed: Cell = copy(isRevealed = true)
	def asFlagToggled: Cell = copy(isFlagged = !isFlagged)
}