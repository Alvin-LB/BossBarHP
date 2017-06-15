![](http://i.imgur.com/qRuuUMy.png)

# BossBarHP
Source code for the Bukkit Plugin BossBarHP, developed by AlvinLB. BukkitDev page:
https://dev.bukkit.org/bukkit-plugins/bossbarhp/

## What is it?
BossBarHP is a very simple plugin. It lets you display the health of any mob you hit in a boss bar at the top of the screen. It was made based on a request on the bukkit forums.

## Features
- Configurable title to the bossbar.
- Option to toggle the bossbar.
- Ability to change style and colour of the bar.

## Commands/Permissions
- /bossbar reload - Reloads the plugins configuration file. Permission node: bossbar.reload Only operators have this permission by default.
- /bossbar toggle - Toggles the bossbar for the individual user. Permission node: bossbar.toggle Everyone has this permission by default.
- bossbar.* - Gives access to all permissions of this plugin.

## Configuration
Explanation on the various configuration values:
- display-time: The time the boss bar will show on the screen for, in seconds.
- format: The format to be displayed above the boss bar. Supports colour codes (using '§') and %name% will be replaced with the name of the entity.
- bar-colour: The colour to use in the bar. Possible values are PINK, BLUE, RED, GREEN, YELLOW, PURPLE and WHITE.
- bar-style: The style to use in the bar. Possible values are SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
- custom-rules: Define custom rules to be used for certain entity types. Further explanation is in the comments of the config file.

### Example
```
# Configuration for the BossBarHP plugin
# Time in seconds until the bar for the mob disappears (after the last hit)
display-time: 7
# The name to be displayed above the boss bar. Use § for colour codes (IE §a). You can put in %name% which will be replaced with the name of the entity.
# This value should always be in apostrophes.
format: '%name%'
# The colour of the bar to be used. Acceptable values are: PINK, BLUE, RED, GREEN, YELLOW, PURPLE and WHITE
bar-colour: 'YELLOW'
# The style of the bar to be used. Acceptable values are: SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
bar-style: 'SOLID'

# Custom rules for certain entities of the boss bar. Allows for each individual entity type to have its own settings. The values are the same as above.
# If a custom rule does not exist for a certain entity type, the one configured above is used.
# The custom rules are structured like this:
# custom-rules:
#   <entity id>:
#     format: 'value'
#     bar-colour: 'value'
#     bar-style: 'value'

# NOTE: Indentations must be at least two spaces to be valid (tabs do not count).

# Example:
# custom-rules:
#   pig:
#     format: '%name%'
#     bar-colour: 'PINK'
#     bar-style: 'SEGMENTED_20'

# The entity IDs are found in this table in the 'Savegame ID' column:
# https://minecraft.gamepedia.com/Data_values/Entity_IDs
# In addition to these, there are also two custom defined ones, 'animal' and 'monster'. Note that any specific type will take precedence over animal and monster.
custom-rules:
  animal:
    format: '%name%'
    bar-colour: 'GREEN'
    bar-style: 'SEGMENTED_10'
  parrot:
    format: '§3%name% (Parrot)'
    bar-colour: 'BLUE'
    bar-style: 'SEGMENTED_6'
```

## Metrics
As of version 1.2, BossBarHP uses Metrics on https://bstats.org/

This means that the following information is collected and sent to the bStats servers:

- The server's randomly generated UUID
- The amount of players on the server
- The online mode of the server
- The bukkit version of the server
- The java version of the system (e.g. Java 8)
- The name of the OS (e.g. Windows)
- The version of the OS
- The architecture of the OS (e.g. amd64)
- The system cores of the OS (e.g. 8)
- bStats-supported plugins
- Plugin version of bStats-supported plugins
- If you wish to opt out of this, there is a config option to disable it in /plugins/bStats/config.yml.

Read here for more information:

https://bstats.org/getting-started

## Screenshots
![](http://i.imgur.com/hJmhvk9.png)
![](http://i.imgur.com/HtvYG4o.png)

## Other
Have you found a bug/want to make a feature request? Post an issue! You can also submit a PullRequest if you feel like coding!
