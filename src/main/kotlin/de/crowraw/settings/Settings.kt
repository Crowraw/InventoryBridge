package de.crowraw.settings

import de.crowraw.lib.data.MessageConfig
import de.erethon.dungeonsxl.api.DungeonsAPI

/*
   _____                                      
 / ____|                                     
| |     _ __ _____      ___ __ __ ___      __
| |    | '__/ _ \ \ /\ / / '__/ _` \ \ /\ / /
| |____| | | (_) \ V  V /| | | (_| |\ V  V / 
 \_____|_|  \___/ \_/\_/ |_|  \__,_| \_/\_/  
    
    
    Crowraw#9875 for any questions
    Date: 14.10.2021
    
    
    
 */class Settings(messageConfig: MessageConfig, version: String) {

    val saveXP: Boolean
    val syncEnderChest: Boolean
    var dungeonXLActive: Boolean = true
    var autoBackUp: Boolean = true
    var interval = 10
    var withOffHand = false

    init {

        messageConfig.registerMessage("setting_save_xp", "true")
        messageConfig.registerMessage("setting_save_enderchest", "true")
        messageConfig.registerMessage("setting_auto_Back_UP", "true")
        messageConfig.registerMessage("setting_auto_Back_UP_Interval_Min", "10")

        saveXP = messageConfig.getMessageForKey("setting_save_xp").toBoolean()
        syncEnderChest = messageConfig.getMessageForKey("setting_save_enderchest").toBoolean()
        autoBackUp = messageConfig.getMessageForKey("setting_auto_Back_UP").toBoolean()
        interval = messageConfig.getMessageForKey("setting_auto_Back_UP_Interval_Min").toInt()

        if (version.contains("1.12") ||
            version.contains("1.13") ||
            version.contains("1.14") ||
            version.contains("1.15") ||
            version.contains("1.16") ||
            version.contains("1.17")){
            withOffHand=true
        }
    }
}