package de.crowraw.listener

import de.crowraw.InventoryBridge
import de.crowraw.lib.player.CrowPlayer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/*
   _____                                      
 / ____|                                     
| |     _ __ _____      ___ __ __ ___      __
| |    | '__/ _ \ \ /\ / / '__/ _` \ \ /\ / /
| |____| | | (_) \ V  V /| | | (_| |\ V  V / 
 \_____|_|  \___/ \_/\_/ |_|  \__,_| \_/\_/  
    
    
    Crowraw#9875 for any questions
    Date: 11.10.2021
    
    
    
 */class ConnectionListener(var plugin: InventoryBridge) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private val executors = Executors.newScheduledThreadPool(5)
    private val interval = plugin.messageConfig.getMessageForKey("waiting_interval_secs").toInt()

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        plugin.manager.validAndSave(event.player)
    }

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        clearInv(event.player)
        var timeElapse = 0
        var future: ScheduledFuture<*>? = null
        future = executors.scheduleAtFixedRate({
            timeElapse++
            if (timeElapse >= interval) {
                plugin.manager.loadData(event.player)
                future?.cancel(true)
            }

            event.player.sendMessage(
                plugin.messageConfig.getMessageForKey("waiting_time_less")
                    .replace("%seconds%", (interval - timeElapse).toString())
            )
        }, 1, 1, TimeUnit.SECONDS)

    }


    private fun clearInv(player: Player) {
        if (plugin.settings.syncEnderChest) {
            player.enderChest.clear()
        }
        if (plugin.settings.saveXP) {
            player.level=0
            player.exp=0f
        }
        player.inventory.clear()
        CrowPlayer(player).resetArmor()

    }
}