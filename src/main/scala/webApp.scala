/**
  * Created by maier on 7/6/16.
  */

import japgolly.scalajs.react._

import scala.scalajs.js.JSApp
import org.scalajs.jquery.jQuery
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.document

//import scala.util.{Success, Failure}
object webApp extends JSApp {


  def main(): Unit = {
    jQuery(setupUI _)

  }

  def setupUI(): Unit = {

    /*import scala.reflect.runtime.universe._

    def printType[A](a: A)(implicit tag: WeakTypeTag[A]) = println(tag.tpe)


    object Callback {
      def apply[A](a: => A): Callback[A] =
        new Callback(() => a)
    }
    class Callback[A](val f: () => A) {

      def runNow(): A = f()

      def flatMap[B](g:  A => Callback[B]): Callback[B] =
        g(f())

      def map[B](g: A => B) = Callback(g(f()))
    }

    val m = math.random.toInt
    val cb2: Callback[Int] = Callback.apply(5 - 3).flatMap(y => Callback(y * 4).flatMap(y => Callback(y * 4).map(x => x + y)))

    val cb3: Callback[Int] = new Callback(() => (5 -3)).flatMap(y => new Callback(() => y + 4).flatMap(y => new Callback(() => y * 4).map(x => x + y)))

    println(cb2)
    println(cb3)*/
    ///////////////////////////////////////


    object Imports {
      type Coord = (Int, Int)
    }
    import Imports._

    trait GamePosition

    object GamePosition {

      case object Introduction extends GamePosition

      case object Playing extends GamePosition

      trait Outcome extends GamePosition

      case object Win extends Outcome

      case object Tie extends Outcome

    }
    import GamePosition._

    object Game {
      val random = new scala.util.Random(0)

      def isEmptySpot(attempt: Coord, playerOne: Player, playerTwo: Player): Boolean =
        !(playerOne.getSpots.contains(attempt) || playerTwo.getSpots.contains(attempt))

      def findSpot(playerOne: Player, playerTwo: Player): Coord = {

        def needToDefend(line: Int, isRow: Boolean): Option[Coord] = {
          val func = (c: Coord, line: Int) => if (isRow) c._1 == line else c._2 == line

          if (playerOne.getSpots.count(x => func(x, line)) == 2 &&
            !playerTwo.getSpots.exists(x => func(x, line))) {
            if (isRow) {
              Some(playerOne.getSpots.filter(x => func(x, line)).fold((line, 3))((x, y) => (line, x._2 - y._2))) //reduce{(x, y) => (x._1, x._2 + y._2)}
            } else {
              Some(playerOne.getSpots.filter(x => func(x, line)).fold((3, line))((x, y) => (x._1 - y._1, line)))
            }
          } else
            None
        }

        def defendRightDiagonal: Option[Coord] = {
          if (playerOne.getSpots.count(spot => spot._1 == 2 - spot._2) == 2 &&
            playerTwo.getSpots.count(spot => spot._1 == 2 - spot._2) == 0) {
            Some(playerOne.getSpots.filter(spot => spot._1 == 2 - spot._2).fold((3, 3))((old, current) => (old._1 - current._1, old._2 - current._2)))
          } else {
            None
          }
        }

        def defendLeftDiagonal: Option[Coord] = {
          if (playerOne.getSpots.count(spot => spot._1 == spot._2) == 2 &&
            playerTwo.getSpots.count(spot => spot._1 == spot._2) == 0) {
            Some(playerOne.getSpots.filter(spot => spot._1 == spot._2).fold((3, 3))((old, current) => (old._1 - current._1, old._2 - current._2)))
          } else {
            None
          }
        }


        needToDefend(0, isRow = true).
          getOrElse(needToDefend(1, isRow = true).
            getOrElse(needToDefend(2, isRow = true).
              getOrElse(needToDefend(0, isRow = false).
                getOrElse(needToDefend(1, isRow = false).
                  getOrElse(needToDefend(2, isRow = false).
                    getOrElse(defendRightDiagonal.
                      getOrElse(defendLeftDiagonal.
                        getOrElse(random.nextInt(3), random.nextInt(3)))))))))

      }

      def computersTurn(playerOne: Player, playerTwo: Player): Coord = {
        val spot = findSpot(playerOne, playerTwo)
        if (!(playerOne.getSpots ++ playerTwo.getSpots).contains(spot)) {
          spot
        } else {
          computersTurn(playerOne, playerTwo)
        }
      }

      def checkIfDone(currentPlayer: Player, opponent: Player): GamePosition = {
        val spots = currentPlayer.getSpots

        def isRowFilled(row: Int): Boolean =
          (0 to 2).forall(col => spots.contains((row, col)))

        def isAnyRowFilled: Boolean =
          (0 to 2).exists(isRowFilled) // or: (0 to 2).exists(i => isRowFilled(i)) or: (0 to 2) exists isRowFilled

        def isColumnFilled(col: Int): Boolean =
          (0 to 2).forall(row => spots.contains((row, col)))

        def isAnyColumnFilled: Boolean =
          (0 to 2).exists(isColumnFilled)

        def isDiagonalFilled: Boolean =
          (0 to 2).forall(a => spots.contains((a, a))) ||
            (0 to 2).forall(a => spots.contains((a, 2 - a)))

        def isTie: Boolean =
          spots.length + opponent.getSpots.length == 9

        if (isAnyRowFilled || isAnyColumnFilled || isDiagonalFilled) {
          Win
        } else if (isTie) {
          Tie
        } else
          Playing
      }
    }


    case class Player(name: String = "computer", isComp: Boolean = false, private val allSpots: List[Coord] = List()) {
      def getSpots: List[Coord] =
        allSpots

      def addNewSpot(spot: Coord): Player = {
        Player(name, isComp, spot :: allSpots)
      }
    }

    case class State(player1: Player, player2: Player, gameP: GamePosition, turnOne: Boolean) {
      def changeTurn: State = {
        State(player1, player2, gameP, turnOne = false)
      }
    }



    class Backend(self: BackendScope[_, State]) {

      def onTextChange(e: ReactEventI) = {

        if (e.target.className.equals("pOneInput")) {
          e.extract(_.target.value)(value =>
            self.modState(_.copy(player1 = Player(value))))
        } else {
          e.extract(_.target.value)(value =>
            self.modState(_.copy(player2 = Player(value))))
        }
      }


      def numPlayers(e: ReactEventI) = {
        if (e.target.value == "twoPlayer")
          self.modState(_.copy(player2 = Player(isComp = false)))
        else
          self.modState(_.copy(player2 = Player(isComp = true)))
      }

      def startGame = self.modState(_.copy(gameP = Playing))

      def restart = {
        self.modState(x => x.copy(player1 = Player(name = x.player1.name), player2 = Player(name = x.player2.name, isComp = x.player2.isComp), gameP = Introduction, turnOne = true))
      }

      def getCoord(index: Int): Coord = {
        val row = index / 3
        val col = index % 3
        (row, col)
      }

      def onBoardClicked(e: ReactEventI): Callback = {
        val index = if (e.target.className.isEmpty) 100 else e.target.className.charAt(0).asDigit

        val stateValue = self.state.map(s => (s.player1, s.player2, s.gameP))
        stateValue.flatMap { case (playerOne, playerTwo, currentGameP) =>
          if (index < 10 && !(playerOne.getSpots ++ playerTwo.getSpots).contains(getCoord(index)) && currentGameP.equals(Playing)) {
            self.modState { s =>

              if (s.turnOne) {
                val playerOneNewSpots = s.player1.addNewSpot(getCoord(index))

                if (playerTwo.isComp && playerTwo.getSpots.length < 4) {
                  val playerTwoNewSpots = s.player2.addNewSpot(Game.computersTurn(playerOneNewSpots, playerTwo))
                  s.copy(player1 = playerOneNewSpots, player2 = playerTwoNewSpots, gameP = Game.checkIfDone(playerTwoNewSpots, playerOne))
                } else {
                  s.copy(player1 = playerOneNewSpots, gameP = Game.checkIfDone(playerOneNewSpots, playerTwo), turnOne = !s.turnOne)
                }
              } else {
                val playerTwoNewSpots = s.player2.addNewSpot(getCoord(index))
                s.copy(player2 = playerTwoNewSpots, gameP = Game.checkIfDone(playerTwoNewSpots, playerOne), turnOne = !s.turnOne)
              }
            }
          } else {
            Callback.empty
          }
        }
      }

      def printSymbal(index: Int): String = {
        val coord = getCoord(index)
        if (self.state.map(s => s.player1.getSpots).runNow().contains(coord)) {
          "X"
        } else if (self.state.map(s => s.player2.getSpots).runNow().contains(coord)) {
          "O"
        } else {
          " "
        }
      }
    }


    val TickTockToe = {
      ReactComponentB[(State, Backend)]("TickTockToe")
        .render_P {
          case (s, b) =>
            <.div(
              <.table(
                ^.onClick ==> b.onBoardClicked,
                <.tbody(
                  <.tr(
                    <.td(
                      ^.cls := "0", {
                        b.printSymbal(0)
                      }),
                    <.td(
                      ^.cls := "1 middleCol", {
                        b.printSymbal(1)
                      }),
                    <.td(
                      ^.cls := "2", {
                        b.printSymbal(2)
                      })
                  ),
                  <.tr(
                    <.td(
                      ^.cls := "3 middleRow", {
                        b.printSymbal(3)
                      }),
                    <.td(
                      ^.cls := "4 middleRow middleCol", {
                        b.printSymbal(4)
                      }),
                    <.td(
                      ^.cls := "5 middleRow", {
                        b.printSymbal(5)
                      })
                  ),
                  <.tr(
                    <.td(
                      ^.cls := "6", {
                        b.printSymbal(6)
                      }),
                    <.td(
                      ^.cls := "7 middleCol", {
                        b.printSymbal(7)
                      }),
                    <.td(
                      ^.cls := "8", {
                        b.printSymbal(8)
                      }
                    )
                  )
                )
              ),
              s.gameP match {
                case Win =>
                  <.div(
                    if (s.turnOne) s.player2.name else s.player1.name,
                    " is a winner!"
                  )
                case Tie =>
                  <.div(
                    "It is a tie!"
                  )
                case _   =>
                  <.div(
                    "play !"
                  )
              }
            )
        }.build
    }


    val header = ReactComponentB[(State, Backend)]("SearchBar")
      .render_P { case (s, b) =>
        <.div(
          ^.className := "header",
          <.div(
            "number of players: ",
            <.select(
              if(s.player2.isComp) {
                ^.value :=  "onePlayer"
              } else {
                ^.value :=  "twoPlayer"
              },

              ^.name := "numberOfPlayers",
              ^.onChange ==> b.numPlayers,
              <.option(
                ^.value := "onePlayer",
                "1"
              ),
              <.option(
                ^.value := "twoPlayer",
                "2"
              )
            )
          ),
          <.div(
            "player one: ",
            <.input.text(
              ^.className := "pOneInput",
              ^.placeholder := "enter name ...",
              ^.onChange ==> b.onTextChange)
          ),
          <.div(
            s.player2.isComp match {
              case true =>
                <.div(
                  ^.className := "pTwoInput"
                )
              case _    =>
                <.div(
                  "player two: ",
                  <.input.text(
                    ^.className := "pTwoInput",
                    ^.placeholder := "enter name ...",
                    ^.onChange ==> b.onTextChange)
                )
            }
          ),
          <.button(
            ^.onClick --> b.startGame,
            "Start"),
          <.button(
            ^.onClick --> b.restart,
            "Clear")
        )
      }
      .build

    val theGame = ReactComponentB[Unit]("GameStarter")
      .initialState(State(Player(name = "human player"), Player(isComp = true), Introduction, turnOne = true))
      .backend(new Backend(_))
      .renderS { ($, s) =>
            <.div(
              header(
                (s, $.backend)
              ),
              TickTockToe((s, $.backend))
            )


      }.build

    ReactDOM.render(
      theGame(),
      document.getElementById("reactMe2"))

  }
}
