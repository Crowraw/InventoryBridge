package de.crowraw.backup

import de.crowraw.InventoryBridge
import org.bukkit.Bukkit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/*
   _____                                      
 / ____|                                     
| |     _ __ _____      ___ __ __ ___      __
| |    | '__/ _ \ \ /\ / / '__/ _` \ \ /\ / /
| |____| | | (_) \ V  V /| | | (_| |\ V  V / 
 \_____|_|  \___/ \_/\_/ |_|  \__,_| \_/\_/  
    
    
    Crowraw#9875 for any questions
    Date: 15.10.2021
    
    
    
 */class BackUpSaver(var plugin: InventoryBridge) {
    private val executors = Executors.newSingleThreadScheduledExecutor()
    fun invExecutorsStarter() {
        if (plugin.settings.autoBackUp) {
            executors.scheduleAtFixedRate({
                Bukkit.getOnlinePlayers().forEach {
                    plugin.manager.validAndSave(it)
                }
                plugin.logger.info("BackUpped " + Bukkit.getOnlinePlayers().size + " in database.")
            }, plugin.settings.interval.toLong(), plugin.settings.interval.toLong(), TimeUnit.MINUTES)
        }
    }
}