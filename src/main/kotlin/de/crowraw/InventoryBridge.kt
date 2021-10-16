package de.crowraw

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.crowraw.backup.BackUpSaver
import de.crowraw.bridge.StatsBridgeManager
import de.crowraw.data.DataSourceHelper
import de.crowraw.lib.data.ConfigProvider
import de.crowraw.lib.data.MessageConfig
import de.crowraw.lib.data.MySQLConfiguration
import de.crowraw.listener.ConnectionListener
import de.crowraw.settings.Settings
import de.erethon.dungeonsxl.api.DungeonsAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


/*
   _____                                      
 / ____|                                     
| |     _ __ _____      ___ __ __ ___      __
| |    | '__/ _ \ \ /\ / / '__/ _` \ \ /\ / /
| |____| | | (_) \ V  V /| | | (_| |\ V  V / 
 \_____|_|  \___/ \_/\_/ |_|  \__,_| \_/\_/  
    
    
    Crowraw#9875 for any questions
    Date: 11.10.2021
    
    
    
 */class InventoryBridge : JavaPlugin() {
    lateinit var messageConfig: MessageConfig
    lateinit var dataSource: HikariDataSource
    lateinit var dataSourceHelper: DataSourceHelper
    lateinit var manager: StatsBridgeManager
    lateinit var settings: Settings
    lateinit var dxl: DungeonsAPI
    override fun onEnable() {
        messageConfig = MessageConfig(this, ConfigProvider(this))
        settings = Settings(messageConfig,Bukkit.getVersion())
        createConnection()
        defaultDatabase()
        dataSourceHelper = DataSourceHelper(dataSource)
        manager = StatsBridgeManager(this, settings)
        registerMessages()
        registerListeners()
        dungeonXLSupport()

    }

    override fun onDisable() {
        dataSource.close()
        dataSourceHelper.shutDown()
    }

    private fun dungeonXLSupport() {
        settings.dungeonXLActive = Bukkit.getPluginManager().isPluginEnabled("DungeonsXL")
        if (settings.dungeonXLActive)
            dxl = Bukkit.getPluginManager().getPlugin("DungeonsXL") as DungeonsAPI
    }

    private fun createConnection() {
        val prop = Properties()
        val mysqlSettings = MySQLConfiguration(messageConfig)
        prop.setProperty("dataSourceClassName", "org.mariadb.jdbc.MariaDbDataSource")
        prop.setProperty("dataSource.serverName", mysqlSettings.host)
        prop.setProperty("dataSource.portNumber", mysqlSettings.port.toString())
        prop.setProperty("dataSource.user", mysqlSettings.user)
        prop.setProperty("dataSource.password", mysqlSettings.password)
        prop.setProperty("dataSource.databaseName", mysqlSettings.database)

        val config = HikariConfig(prop)
        config.maximumPoolSize = 10

        dataSource = HikariDataSource(config)
    }

    private fun defaultDatabase() {
        try {

            val connection = dataSource.connection
            val prepared = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS INVENTORIES(" +
                        "UUID VARCHAR(36) PRIMARY KEY," +
                        "INV LONGTEXT," +
                        "XPLVL FLOAT," +
                        "SYNC BOOLEAN," +
                        "ENDERINV LONGTEXT," +
                        "XPASINT INT)"
            )

            prepared.execute()

            connection.close()
            prepared.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun registerMessages() {
        messageConfig.registerMessage(
            "waiting_time_less",
            "§cYou still need to wait §4%seconds%§c until your inventory is loaded."
        )
        messageConfig.registerMessage("waiting_interval_secs", "3")
    }

    private fun registerListeners() {
        ConnectionListener(this)
        BackUpSaver(this).invExecutorsStarter()
    }
}