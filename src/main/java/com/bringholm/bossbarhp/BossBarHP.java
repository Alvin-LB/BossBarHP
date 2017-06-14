package com.bringholm.bossbarhp;

import com.bringholm.bossbarhp.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BossBarHP extends JavaPlugin implements Listener {
    private static Set<String> entityIDs;

    static {
        Set<String> ids = new HashSet<>();
        for (EntityType entityType : EntityType.values()) {
            //noinspection deprecation
            if (entityType.getName() != null && Damageable.class.isAssignableFrom(entityType.getEntityClass())) {
                //noinspection deprecation
                ids.add(entityType.getName());
            }
        }
        entityIDs = ids;
    }

    private HashMap<UUID, BossBar> bossBarHashMap = new HashMap<>();
    private HashMap<UUID, UUID> currentEntityTarget = new HashMap<>();
    private HashMap<UUID, BukkitRunnable> currentRunnables = new HashMap<>();
    private List<String> bossBarUsers = new ArrayList<>();
    private int seconds = 0;
    private BossBarData defaultData;
    private HashMap<String, BossBarData> customData = new HashMap<>();

    private void reloadConfigValues() {
        reloadConfig();
        seconds = getConfig().getInt("display-time");
        String bossBarString = getConfig().getString("format");
        File customConfigFile = new File(this.getDataFolder(), "players.yml");
        FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        bossBarUsers = customConfig.getStringList("UsersWithBossBarEnabled");
        BarColor barColor;
        try {
            barColor = BarColor.valueOf(getConfig().getString("bar-colour"));
        } catch (IllegalArgumentException e) {
            getLogger().warning("Bar colour " + getConfig().getString("bar-colour") + " is not valid!");
            barColor = BarColor.RED;
        }
        BarStyle barStyle;
        try {
            barStyle = BarStyle.valueOf(getConfig().getString("bar-style"));
        } catch (IllegalArgumentException e) {
            getLogger().warning("Bar style " + getConfig().getString("bar-style") + " is not valid!");
            barStyle = BarStyle.SOLID;
        }
        defaultData = new BossBarData(bossBarString, barColor, barStyle);
        customData.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("custom-rules");
        Set<String> keys = section.getKeys(false);
        if (keys.size() != 0) {
            for (String key : keys) {
                key = key.toLowerCase();
                if (entityIDs.contains(key) || key.equals("animal") || key.equals("monster")) {
                    String format = section.getString(key + ".format");
                    BarColor colour;
                    try {
                        colour = BarColor.valueOf(section.getString(key + ".bar-colour"));
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Bar colour " + section.getString(key + ".bar-colour") + " is not valid! Skipping entry!");
                        continue;
                    }
                    BarStyle style;
                    try {
                        style = BarStyle.valueOf(section.getString(key + ".bar-style"));
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Bar style " + section.getString(key + ".bar-style") + " is not valid! Skipping entry!");
                        continue;
                    }
                    customData.put(key, new BossBarData(format, colour, style));
                } else {
                    this.getLogger().warning("Entity ID " + key + " is not valid, skipping it!");
                }
            }
        }
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        reloadConfigValues();
        new Metrics(this);
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
                        BossBar bossBar = Bukkit.createBossBar(getTitle(e.getEntity()).replace("%name%", e.getEntity().getName()), getColour(e.getEntity()), getStyle(e.getEntity()));
                        bossBar.addPlayer(player);
                        bossBarHashMap.put(player.getUniqueId(), bossBar);
                        double entityHealth = entity.getHealth() - e.getDamage();
                        if (entityHealth > 0) {
                            bossBar.setProgress(entityHealth / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                            bossBar.setTitle(getTitle(e.getEntity()).replace("%name%", e.getEntity().getName()));
                        }
                    }
                    if (currentEntityTarget.containsValue(player.getUniqueId())) {
                        currentEntityTarget.values().remove(player.getUniqueId());
                    }
                    currentEntityTarget.put(entity.getUniqueId(), player.getUniqueId());
                    if (currentRunnables.containsKey(player.getUniqueId())) {
                        currentRunnables.get(player.getUniqueId()).cancel();
                        currentRunnables.remove(player.getUniqueId());
                    }
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            if (currentEntityTarget.containsKey(e.getEntity().getUniqueId())) {
                Player player = Bukkit.getPlayer(currentEntityTarget.get(e.getEntity().getUniqueId()));
                LivingEntity entity = (LivingEntity) e.getEntity();
                BossBar bossBar = bossBarHashMap.get(player.getUniqueId());
                double entityHealth = entity.getHealth() - e.getDamage();
                if (entityHealth > 0) {
                    BarStyle style = getStyle(entity);
                    BarColor color = getColour(entity);
                    if (color != bossBar.getColor() || style != bossBar.getStyle()) {
                        bossBar.removePlayer(player);
                        bossBar = Bukkit.createBossBar(getTitle(e.getEntity()).replace("%name%", e.getEntity().getName()), color, style);
                        bossBar.setProgress(entityHealth / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        bossBar.addPlayer(player);
                        if (currentRunnables.containsKey(player.getUniqueId())) {
                            currentRunnables.get(player.getUniqueId()).cancel();
                            currentRunnables.remove(player.getUniqueId());
                        }
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
                        bossBarHashMap.put(player.getUniqueId(), bossBar);
                    } else {
                        bossBar.setProgress(entityHealth / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        bossBar.setTitle(getTitle(e.getEntity()).replace("%name%", e.getEntity().getName()));
                    }
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
                    reloadConfigValues();
                    commandSender.sendMessage(ChatColor.YELLOW + "Reloaded BossBarHP config!");
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

    @SuppressWarnings("deprecation")
    private String getTitle(Entity entity) {
        String name = entity.getType().getName();
        if (customData.containsKey(name)) {
            return customData.get(name).title;
        } else if (entity instanceof Monster && customData.containsKey("monster")) {
            return customData.get("monster").title;
        } else if (entity instanceof Animals && customData.containsKey("animal")) {
            return customData.get("animal").title;
        } else {
            return defaultData.title;
        }
    }

    @SuppressWarnings("deprecation")
    private BarColor getColour(Entity entity) {
        String name = entity.getType().getName();
        if (customData.containsKey(name)) {
            return customData.get(name).colour;
        } else if (entity instanceof Monster && customData.containsKey("monster")) {
            return customData.get("monster").colour;
        } else if (entity instanceof Animals && customData.containsKey("animal")) {
            return customData.get("animal").colour;
        } else {
            return defaultData.colour;
        }
    }

    @SuppressWarnings("deprecation")
    private BarStyle getStyle(Entity entity) {
        String name = entity.getType().getName();
        if (customData.containsKey(name)) {
            return customData.get(name).style;
        } else if (entity instanceof Monster && customData.containsKey("monster")) {
            return customData.get("monster").style;
        } else if (entity instanceof Animals && customData.containsKey("animal")) {
            return customData.get("animal").style;
        } else {
            return defaultData.style;
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

    private class BossBarData {
        String title;
        BarColor colour;
        BarStyle style;

        BossBarData(String title, BarColor colour, BarStyle style) {
            this.title = title;
            this.colour = colour;
            this.style = style;
        }
    }
}
