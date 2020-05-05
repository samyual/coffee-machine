package machine

import java.lang.NumberFormatException
import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)
    val coffeeMachine = CoffeeMachine()
    do {
        val input = scanner.nextLine().trim()
        coffeeMachine.handle(input)
    } while (coffeeMachine.isActive)
}

class CoffeeMachine {

    var isActive = true
        private set

    /**
     * You can easy add commands with lambda returning new state of coffee machine
     */
    private val actions = mapOf(
            "buy" to {
                State.ChoosingCoffee
            },
            "take" to {
                takeMoney()
                State.MainMenu
            },
            "fill" to {
                State.FillingWater
            },
            "remaining" to {
                println(resources)
                State.MainMenu
            },
            "exit" to {
                isActive = false
                State.MainMenu
            }
    )

    private val resources = Resources(water = 400, milk = 540, beans = 120, cups = 9, money = 550)

    private var currentState: State = State.MainMenu
        set(value) {
            when (value) {
                State.MainMenu -> prompt("\nWrite action (buy, fill, take, remaining, exit)")
                State.ChoosingCoffee -> prompt("\nWhat do you want to buy? ${CoffeeVariants.prompts()}, back - to main menu")
                State.FillingWater -> prompt("\nWrite how many ml of water do you want to add")
                State.FillingMilk -> prompt("Write how many ml of milk do you want to add")
                State.FillingBeans -> prompt("Write how many grams of coffee beans do you want to add")
                State.FillingCups -> prompt("Write how many disposable cups of coffee do you want to add")
            }
            field = value
        }

    // initialization of state needed to trigger of printing prompt
    init {
        currentState = State.MainMenu
    }

    /**
     * Main method of coffee machine processing input
     */
    fun handle(input: String) {
        currentState = when (currentState) {
            State.MainMenu -> chooseAction(input)
            State.ChoosingCoffee -> chooseCoffee(input)
            State.FillingWater -> addWater(input)
            State.FillingMilk -> addMilk(input)
            State.FillingBeans -> addBeans(input)
            State.FillingCups -> addCups(input)
        }
    }

    private fun chooseAction(command: String): State {
        return actions[command]?.invoke() ?: currentState
    }

    private fun chooseCoffee(input: String): State {
        return when (input) {
            in CoffeeVariants.inputs() -> {
                val coffee = CoffeeVariants.receiptFor(input.toInt())
                if (isEnoughResources(coffee)) {
                    makeCoffee(coffee)
                }
                State.MainMenu
            }
            "back" -> State.MainMenu
            else -> {
                println("Illegal choice")
                currentState
            }
        }
    }

    private fun makeCoffee(receipt: Receipt) {
        resources.water -= receipt.water
        resources.milk -= receipt.milk
        resources.beans -= receipt.beans
        resources.cups -= 1
        resources.money += receipt.price
        println("I have enough resources, making you a coffee!")
    }

    private fun isEnoughResources(receipt: Receipt): Boolean {
        if (receipt.water > resources.water) {
            println("Sorry, not enough water!")
            return false
        }
        if (receipt.milk > resources.milk) {
            println("Sorry, not enough milk!")
            return false
        }
        if (receipt.beans > resources.beans) {
            println("Sorry, not enough coffee beans!")
            return false
        }
        if (resources.cups == 0) {
            println("Sorry, not enough disposable cups!")
            return false
        }
        return true
    }

    private fun takeMoney() {
        if (resources.money != 0) {
            println("I gave you $${resources.money}")
            resources.money = 0
        } else {
            println("Cash is empty!")
        }
    }

    private fun addWater(input: String): State {
        return try {
            resources.water += input.toInt().coerceAtLeast(0)
            State.FillingMilk
        } catch (e: NumberFormatException) {
            currentState
        }
    }

    private fun addMilk(input: String): State {
        return try {
            resources.milk += input.toInt().coerceAtLeast(0)
            State.FillingBeans
        } catch (e: NumberFormatException) {
            currentState
        }
    }

    private fun addBeans(input: String): State {
        return try {
            resources.beans += input.toInt().coerceAtLeast(0)
            State.FillingCups
        } catch (e: NumberFormatException) {
            currentState
        }
    }

    private fun addCups(input: String): State {
        return try {
            resources.cups += input.toInt().coerceAtLeast(0)
            State.MainMenu
        } catch (e: NumberFormatException) {
            currentState
        }
    }

    private fun prompt(text: String) {
        print("$text: > ")
    }

    private enum class State {
        MainMenu,
        ChoosingCoffee,
        FillingWater,
        FillingMilk,
        FillingBeans,
        FillingCups
    }

    private data class Receipt(val water: Int, val milk: Int = 0, val beans: Int, val price: Int)

    /**
     * Class containing receipts of coffee.
     * It allows add new receipts without changing logic of program.
     */
    private enum class CoffeeVariants(val receipt: Receipt) {
        ESPRESSO(Receipt(water = 250, beans = 16, price = 4)),
        LATTE(Receipt(water = 350, milk = 75, beans = 20, price = 7)),
        CAPPUCCINO(Receipt(water = 200, milk = 100, beans = 12, price = 6));

        override fun toString(): String {
            return "${ordinal.inc()} - ${name.toLowerCase().replace("_", " ")}"
        }

        companion object {

            /**
             * Returns list of receipts numbers as string starting from one
             */
            fun inputs() = values().map { it.ordinal.inc().toString() }

            /**
             * Returns prompt in format "1 - espresso, 2 - latte, ..."
             */
            fun prompts() = values().joinToString(", ") { it.toString() }

            /**
             * Returns receipt for number of order
             */
            fun receiptFor(receiptNo: Int): Receipt {
                return values().first { it.ordinal.inc() == receiptNo }.receipt
            }
        }
    }

    private data class Resources(var water: Int, var milk: Int, var beans: Int, var cups: Int, var money: Int) {
        override fun toString(): String {
            return """
            \nThe coffee machine has:
            $water of water
            $milk of milk
            $beans of coffee beans
            $cups of disposable cups
            $$money of money
        """.trimIndent()
        }
    }
}
