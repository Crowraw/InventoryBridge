package de.crowraw.data

import com.zaxxer.hikari.HikariDataSource
import de.crowraw.lib.wrapper.InventoryWrapper
import org.bukkit.entity.Player
import java.sql.SQLException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

/*
   _____                                      
 / ____|                                     
| |     _ __ _____      ___ __ __ ___      __
| |    | '__/ _ \ \ /\ / / '__/ _` \ \ /\ / /
| |____| | | (_) \ V  V /| | | (_| |\ V  V / 
 \_____|_|  \___/ \_/\_/ |_|  \__,_| \_/\_/  
    
    
    Crowraw#9875 for any questions
    Date: 13.10.2021
    
    
    
 */class DataSourceHelper(private val dataSource: HikariDataSource) {
    private val executorScheduler = Executors.newFixedThreadPool(5)
    fun savePlayersLvL(player: Player) {
        executorScheduler.execute {
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO INVENTORIES (XPLVL,UUID,XPASINT) " +
                        "VALUES (?,?,?) ON DUPLICATE KEY UPDATE `XPLVL` = ?,`XPASINT` = ?")
                    .use { stmt ->
                        stmt.setFloat(1, player.exp)
                        stmt.setString(2, player.uniqueId.toString())
                        stmt.setInt(3, player.level)
                        stmt.setFloat(4, player.exp)
                        stmt.setInt(5, player.level)
                        stmt.executeUpdate()
                    }
            }

        }
    }
    fun getPlayersLvlAsInt(player: Player): CompletableFuture<Int> {
        val completableFuture = CompletableFuture<Int>()
        executorScheduler.execute {
            dataSource.connection.use { con ->
                con.prepareStatement("SELECT `XPASINT` FROM INVENTORIES WHERE `UUID` = ?").use { prepared ->
                    prepared.setString(1, player.uniqueId.toString())
                    prepared.executeQuery().use {
                        while (it.next()) {
                            val result = it.getInt("XPASINT")
                            completableFuture.complete(result)
                        }
                    }

                }
            }

        }
        return completableFuture
    }
    fun getPlayersFloat(player: Player): CompletableFuture<Float> {
        val completableFuture = CompletableFuture<Float>()
        executorScheduler.execute {
            dataSource.connection.use { con ->
                con.prepareStatement("SELECT `XPLVL` FROM INVENTORIES WHERE `UUID` = ?").use { prepared ->
                    prepared.setString(1, player.uniqueId.toString())
                    prepared.executeQuery().use {
                        while (it.next()) {
                            val result = it.getFloat("XPLVL")
                            completableFuture.complete(result)
                        }
                    }

                }
            }

        }
        return completableFuture
    }

    fun getEnderChest(player: Player): CompletableFuture<InventoryWrapper> {
        val completableFuture = CompletableFuture<InventoryWrapper>()
        executorScheduler.execute {
            dataSource.connection.use { con ->
                con.prepareStatement("SELECT `ENDERINV` FROM INVENTORIES WHERE `UUID` = ?").use { statement ->
                    statement.setString(1, player.uniqueId.toString())
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val result = resultSet.getString("ENDERINV")
                            //deserialize inv again
                            if (result != null) {
                                completableFuture.complete(InventoryWrapper.deserialize(result))
                            }

                        }
                    }
                }

            }
        }

        return completableFuture

    }

    fun getPlayersInv(player: Player): CompletableFuture<InventoryWrapper> {
        val completableFuture = CompletableFuture<InventoryWrapper>()

        executorScheduler.execute {
            dataSource.connection.use { con ->
                con.prepareStatement("SELECT `INV` FROM INVENTORIES WHERE `UUID` = ?").use { statement ->
                    statement.setString(1, player.uniqueId.toString())
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val result = resultSet.getString("INV")
                            //deserialize inv again
                            if (result != null) {
                                completableFuture.complete(InventoryWrapper.deserialize(result))
                            }

                        }
                    }
                }

            }
        }

        return completableFuture
    }

    fun setSync(player: Player, sync: Boolean) {
        executorScheduler.execute {
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO INVENTORIES (SYNC,UUID) VALUES (?,?) ON DUPLICATE KEY UPDATE `SYNC` = ?")
                    .use { stmt ->
                        stmt.setBoolean(1, sync)
                        stmt.setString(2, player.uniqueId.toString())
                        stmt.setBoolean(3, sync)
                        stmt.executeUpdate()
                    }
            }

        }
    }

    fun saveEnderInvInDataBase(player: Player) {
        executorScheduler.execute {
            dataSource.connection.use { connection ->
                //new values in db
                connection.prepareStatement("INSERT INTO INVENTORIES (ENDERINV,UUID) VALUES (?,?) ON DUPLICATE KEY UPDATE `ENDERINV` = ?")
                    .use { preparedStatement ->
                        val serializedObj = InventoryWrapper.serializeDirect(player.enderChest)
                        preparedStatement.setString(1, serializedObj)
                        preparedStatement.setString(2, player.uniqueId.toString())
                        preparedStatement.setString(3, serializedObj)
                        preparedStatement.executeUpdate()
                    }
            }
        }
    }

    fun shouldSync(player: Player): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        executorScheduler.execute {
            dataSource.connection.use { con ->
                con.prepareStatement("SELECT `SYNC` FROM INVENTORIES WHERE `UUID` = ?").use { statement ->
                    statement.setString(1, player.uniqueId.toString())
                    statement.executeQuery().use { resultSet ->
                        //when first joining the server sync should be enabled
                        while (resultSet.next()) {
                            val result = resultSet.getBoolean("SYNC")
                            completableFuture.complete(result)
                            return@execute
                        }
                        completableFuture.complete(true)
                    }
                }
            }

        }
        return completableFuture
    }

    //should be only called if it is wanted (worlds)
    fun savePlayerInvInDatabase(player: Player) {
        executorScheduler.execute {
            dataSource.connection.use { connection ->
                //new values in db
                connection.prepareStatement("INSERT INTO INVENTORIES (INV,UUID) VALUES (?,?) ON DUPLICATE KEY UPDATE `INV` = ?")
                    .use { preparedStatement ->
                        val serializedObj = InventoryWrapper.serializeDirect(player.inventory)
                        preparedStatement.setString(1, serializedObj)
                        preparedStatement.setString(2, player.uniqueId.toString())
                        preparedStatement.setString(3, serializedObj)

                        preparedStatement.executeUpdate()
                    }
            }
        }
    }


    fun shutDown() {
        executorScheduler.shutdown()
    }
}