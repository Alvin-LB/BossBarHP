# BossBarHP
Source code for the Bukkit Plugin BossBarHP, developed by AlvinLB. BukkitDev page:
https://dev.bukkit.org/bukkit-plugins/bossbarhp/
##What is it?
BossBarHP is a very simple plugin. It lets you display the health of any mob you hit in a boss bar at the top of the screen. It was made based on a request on the bukkit forums.

## Features
-Configurable title to the bossbar.
-Option to toggle the bossbar.
-Ability to change style and colour of the bar.
## Commands/Permissions
-/bossbar reload - Reloads the plugins configuration file. Permission node: bossbar.reload Only operators have this permission by -default.
-/bossbar toggle - Toggles the bossbar for the individual user. Permission node: bossbar.toggle Everyone has this permission by default.
-bossbar.* - Gives access to all permissions of this plugin.
## Configuration
```
# Configuration for the BossBarHP plugin
# Time in seconds until the bar for the mob disappears (after the last hit)
display-time: 5
# The name to be displayed above the boss bar. Use § for colour codes (IE §a). You can put in %name% which will be replaced with the name of the entity. This value should always be in apostrophes.
format: '%name%'
# The colour of the bar to be used. Acceptable values are: PINK, BLUE, RED, GREEN, YELLOW, PURPLE and WHITE
bar-colour: 'RED'
# The style of the bar to be used. Acceptable values are: SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
bar-style: 'SOLID'
```
## Screenshots
http://i.imgur.com/hJmhvk9.png http://i.imgur.com/HtvYG4o.png

## Other
Have you found a bug/want to make a feature request? Post an issue on the GitHub page! You can also submit a PullRequest if you feel like coding!
