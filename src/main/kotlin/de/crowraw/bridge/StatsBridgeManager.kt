package de.crowraw.bridge

import de.crowraw.InventoryBridge
import de.crowraw.settings.Settings
import org.bukkit.entity.Player

/*
   _____                                      
 / ____|                                     
| |     _ __ _____      ___ __ __ ___      __
| |    | '__/ _ \ \ /\ / / '__/ _` \ \ /\ / /
| |____| | | (_) \ V  V /| | | (_| |\ V  V / 
 \_____|_|  \___/ \_/\_/ |_|  \__,_| \_/\_/  
    
    
    Crowraw#9875 for any questions
    Date: 13.10.2021
    
    
    
 */class StatsBridgeManager(private val inventoryBridge: InventoryBridge, private val settings: Settings) {


    //will load the inventory from mysql
    private fun loadInventory(player: Player) {
        val inventoryCopy = inventoryBridge.dataSourceHelper.getPlayersInv(player)

        player.inventory.clear()
        inventoryCopy.thenApply {

            player.inventory.helmet = it.helmet
            player.inventory.chestplate = it.chestplate
            player.inventory.leggings = it.leggings
            player.inventory.boots = it.boots

            for (i in 0..it.contents.size) {
                if (it.contents[i] == null) {
                    continue
                }
                player.inventory.setItem(i, it.contents[i])
            }

        }

    }

    private fun saveData(player: Player) {
        inventoryBridge.dataSourceHelper.shouldSync(player).thenApply {
            if (it) {
                if (settings.saveXP) {
                    saveXP(player)
                }
                if (settings.syncEnderChest) {
                    saveEnderChest(player)
                }
                saveInventory(player)
            }

        }

    }

    fun loadData(player: Player) {
        inventoryBridge.dataSourceHelper.shouldSync(player).thenApply {
            if (it) {
                if (settings.saveXP) {
                    loadXP(player)
                }
                if (settings.syncEnderChest) {
                    player.enderChest.clear()
                    loadEnderChest(player)
                }
                loadInventory(player)


            }

        }
    }

    private fun loadEnderChest(player: Player) {
        inventoryBridge.dataSourceHelper.getEnderChest(player).thenApply {
            player.enderChest.clear()
            for (i in 0 until it.contents.size) {
                val itemStack = it.contents[i]
                if (itemStack != null)
                    player.enderChest.setItem(i, itemStack)
            }

        }
    }

    private fun loadXP(player: Player) {
        inventoryBridge.dataSourceHelper.getPlayersFloat(player).thenApply {
            player.exp = it
        }
        inventoryBridge.dataSourceHelper.getPlayersLvlAsInt(player).thenApply {
            player.level = it
        }
    }

    private fun saveXP(player: Player) {
        inventoryBridge.dataSourceHelper.savePlayersLvL(player)
    }

    //saves the inventory in mysql. "load" -> if you want still to save the inventory even tho he is not valid set true
    private fun saveInventory(player: Player) {
        inventoryBridge.dataSourceHelper.savePlayerInvInDatabase(player)
    }

    private fun saveEnderChest(player: Player) {
        inventoryBridge.dataSourceHelper.saveEnderInvInDataBase(player)
    }

    private fun isInDungeon(player: Player): Boolean {
        return inventoryBridge.dxl.playerCache.getGamePlayer(player) != null
    }

    //checks for dependenices
    fun validAndSave(player: Player) {
        var valid = true
        if (settings.dungeonXLActive && isInDungeon(player)) {
            valid = false
        }
        if (valid) {
            inventoryBridge.dataSourceHelper.setSync(player, true)
        } else {
            inventoryBridge.dataSourceHelper.setSync(player, false)
        }
        saveData(player)
    }


}