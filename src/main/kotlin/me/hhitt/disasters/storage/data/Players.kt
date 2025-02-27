package me.hhitt.disasters.storage.data
import org.jetbrains.exposed.sql.Table

object Players : Table() {
    val id = uuid("id")
    val wins = integer("wins").default(0)
    val defeats = integer("defeats").default(0)
    val totalPlayed = integer("total_played").default(0)

    override val primaryKey = PrimaryKey(id)
}