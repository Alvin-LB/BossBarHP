package com.bringholm.bossbarhp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
    private HashMap<UUID, BossBar> bossBarHashMap = new HashMap<>();
    private HashMap<UUID, UUID> currentEntityTarget = new HashMap<>();
    private HashMap<UUID, BukkitRunnable> currentRunnables = new HashMap<>();
    private List<String> bossBarUsers = new ArrayList<>();
    private int seconds = 0;
    private String bossBarString = "";
    private BarColor barColor;
    private BarStyle barStyle;

    private boolean reloadConfigValues() {
        boolean success = true;
        reloadConfig();
        seconds = getConfig().getInt("display-time");
        bossBarString = getConfig().getString("format");
        File customConfigFile = new File(this.getDataFolder(), "players.yml");
        FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        bossBarUsers = customConfig.getStringList("UsersWithBossBarEnabled");
        try {
            barColor = BarColor.valueOf(getConfig().getString("bar-colour"));
        } catch (IllegalArgumentException e) {
            getLogger().severe("Bar colour " + getConfig().getString("bar-colour") + " is not valid!");
            barColor = BarColor.RED;
            success = false;
        }
        try {
            barStyle = BarStyle.valueOf(getConfig().getString("bar-style"));
        } catch (IllegalArgumentException e) {
            getLogger().severe("Bar style " + getConfig().getString("bar-style") + " is not valid!");
            barStyle = BarStyle.SOLID;
            success = false;
        }
        return success;
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        boolean loadSuccess = reloadConfigValues();
        if (!loadSuccess) {
            getLogger().severe("Failed to load config.yml!");
        }
    }

    @EventHandler
    public void onEntityDamagedByPlayer(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            if (e.getDamager() instanceof Player || e.getDamager() instanceof Arrow) {
                Player player;
                if (e.getDamager() instanceof Player) {
                    player = (Player) e.getDamager();
                } else {
                    if (!(((Arrow) e.getDamager()).getShooter() instanceof Player)) {
                        return;
                    } else {
                        player = (Player) ((Arrow) e.getDamager()).getShooter();
                    }
                }
                LivingEntity entity = (LivingEntity) e.getEntity();
                if (bossBarUsers.contains(player.getUniqueId().toString())) {
                    if (!bossBarHashMap.containsKey(player.getUniqueId())) {
                        BossBar bossBar = Bukkit.createBossBar(bossBarString.replace("%name%", e.getEntity().getName()), barColor, barStyle);
                        bossBar.addPlayer(player);
                        bossBarHashMap.put(player.getUniqueId(), bossBar);
                        double entityHealth = entity.getHealth() - e.getDamage();
                        if (entityHealth > 0) {
                            bossBar.setProgress(entityHealth / entity.getMaxHealth());
                            bossBar.setTitle(bossBarString.replace("%name%", e.getEntity().getName()));
                        }
                    }
                    if (currentEntityTarget.containsValue(player.getUniqueId())) {
                        currentEntityTarget.values().remove(player.getUniqueId());
                    }
                    currentEntityTarget.put(entity.getUniqueId(), player.getUniqueId());
                    if (currentRunnables.containsKey(player.getUniqueId())) {
                        currentRunnables.get(player.getUniqueId()).cancel();
                        currentRunnables.remove(player.getUniqueId());
                        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (bossBarHashMap.containsKey(player.getUniqueId())) {
                                    bossBarHashMap.get(player.getUniqueId()).removePlayer(player);
                                    bossBarHashMap.remove(player.getUniqueId());
                                    currentEntityTarget.remove(entity.getUniqueId());
                                    currentRunnables.remove(player.getUniqueId());
                                }
                            }
                        };
                        bukkitRunnable.runTaskLater(this, seconds * 20);
                        currentRunnables.put(player.getUniqueId(), bukkitRunnable);
                    } else {
                        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (bossBarHashMap.containsKey(player.getUniqueId())) {
                                    bossBarHashMap.get(player.getUniqueId()).removePlayer(player);
                                    bossBarHashMap.remove(player.getUniqueId());
                                    currentEntityTarget.remove(entity.getUniqueId());
                                    currentRunnables.remove(player.getUniqueId());
                                }
                            }
                        };
                        bukkitRunnable.runTaskLater(this, seconds * 20);
                        currentRunnables.put(player.getUniqueId(), bukkitRunnable);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            if (currentEntityTarget.containsKey(e.getEntity().getUniqueId())) {
                Player player = Bukkit.getPlayer(currentEntityTarget.get(e.getEntity().getUniqueId()));
                LivingEntity entity = (LivingEntity) e.getEntity();
                BossBar bossBar = bossBarHashMap.get(player.getUniqueId());
                double entityHealth = entity.getHealth() - e.getDamage();
                if (entityHealth > 0) {
                    bossBar.setProgress(entityHealth / entity.getMaxHealth());
                    bossBar.setTitle(bossBarString.replace("%name%", e.getEntity().getName()));
                } else {
                    bossBar.removePlayer(player);
                    bossBarHashMap.remove(player.getUniqueId());
                    currentEntityTarget.remove(entity.getUniqueId());
                    if (currentRunnables.containsKey(player.getUniqueId())) {
                        currentRunnables.get(player.getUniqueId()).cancel();
                        currentRunnables.remove(player.getUniqueId());
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equals("reload")) {
                if (commandSender.hasPermission("bossbarhp.reload")) {
                    if (reloadConfigValues()) {
                        commandSender.sendMessage(ChatColor.YELLOW + "Reloaded BossBarHP config!");
                    } else if (commandSender instanceof ConsoleCommandSender) {
                        commandSender.sendMessage(ChatColor.RED + "Failed to load BossBarHP config!");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Failed to load BossBarHP config! Please check the console for further details.");
                    }
                    return true;
                } else {
                    commandSender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                    return true;
                }
            } else if (args[0].equals("toggle")) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    if (player.hasPermission("bossbarhp.toggle")) {
                        if (bossBarUsers.contains(player.getUniqueId().toString())) {
                            bossBarUsers.remove(player.getUniqueId().toString());
                            player.sendMessage(ChatColor.YELLOW + "You have disabled the health bar for mobs!");
                            this.saveBossBarUsers();
                        } else {
                            bossBarUsers.add(player.getUniqueId().toString());
                            player.sendMessage(ChatColor.YELLOW + "You have enabled the health bar for mobs!");
                            this.saveBossBarUsers();
                        }
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                        return true;
                    }
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Only players can execute this command!");
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private void saveBossBarUsers() {
        File customConfigFile = new File(this.getDataFolder(), "players.yml");
        FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        customConfig.set("UsersWithBossBarEnabled", bossBarUsers);
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
