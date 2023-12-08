package de.htwg.se.minesweeper.controller

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import de.htwg.se.minesweeper.model._
import de.htwg.se.minesweeper.observer._

class FieldController(val undos_val: Int, val factory: FieldFactory) extends Observable[Event] {
	private[controller] var field: Field = factory.createField()
	private[controller] var state: FieldControllerState = FirstMoveFieldControllerState(this)
	var undos = undos_val

	private[controller] def changeState(newState: FieldControllerState): Unit = {
		state = newState
	}

	def setup(): Unit = {
		undos = undos_val
		state = FirstMoveFieldControllerState(this)
		undoStack = List.empty
		redoStack = List.empty
		field = factory.createField()
		notifyObservers(SetupEvent(field))
	}

	def reveal(x: Int, y: Int): Try[Unit] = execute(RevealCommand(this, x, y))

	def flag(x: Int, y: Int): Try[Unit] = execute(FlagCommand(this, x, y))

	private[controller] def flag_impl(x: Int, y: Int): Try[Unit] = {
		field.withToggledFlag(x, y) match {
			case Success(newField) => {
				field = newField
				Try(notifyObservers(FieldUpdatedEvent(field)))
			}
			case Failure(exception) => Failure(exception)
		}
	}

	def exit(): Unit = {
		notifyObservers(ExitEvent())
	}

	private[controller] var undoStack: List[Command] = List.empty
	private[controller] var redoStack: List[Command] = List.empty

	private def execute(command: Command): Try[Unit] = {
		undoStack = command :: undoStack
		redoStack = List.empty
		command.execute() match {
			case Success(_) => Success(())
			case Failure(exception) => {
				undoStack = undoStack.tail
				Failure(exception)
			}
		}
	}

	def undo(): Try[Unit] = {
		undoStack match {
			case Nil => Failure(new NoSuchElementException("Nothing to undo!"))
			case head :: tail => {
				if undos <= 0 then return Failure(new RuntimeException("No more undo's left!"))
				head.undo()
				undoStack = tail
				redoStack = head :: redoStack
				undos -= 1
				Success(())
			}
		}
	}

	def redo(): Try[Unit] = {
		redoStack match {
			case Nil => Failure(new NoSuchElementException("Nothing to redo!"))
			case head :: tail => {
				head.redo()
				redoStack = tail
				undoStack = head :: undoStack
				Success(())
			}
		}
	}

	def cantRedo: Boolean = redoStack.isEmpty
}