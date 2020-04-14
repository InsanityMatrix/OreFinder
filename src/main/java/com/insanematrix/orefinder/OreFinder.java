/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.insanematrix.orefinder;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author dvpie
 */
public final class OreFinder extends JavaPlugin {

    private final int FIND_IRON_COOLDOWN = 30;
    private final int FIND_GOLD_COOLDOWN = 60;
    private final int FIND_DIAMOND_COOLDOWN = 300;
    private Map<String, Long> ironCooldowns = new HashMap<>();
    private Map<String, Long> goldCooldowns = new HashMap<>();
    private Map<String, Long> diamondCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("find").setTabCompleter(new FindTabCompleter());
        
    }

    @Override
    public void onDisable() {
        
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("find")) {
            //Find Ore & Stuff
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.YELLOW.toString() + "Only Players can use this command!");
                    return true;
                }
                //Sender is a Player and we can now get what they want.
                String ore = args[0];
                if (ore.equalsIgnoreCase("iron")) {
                    FindIron((Player) sender);
                } else if (ore.equalsIgnoreCase("gold")) {
                    FindGold((Player) sender);
                } else if (ore.equalsIgnoreCase("diamond")) {
                    FindDiamond((Player)sender);
                }else {
                    sender.sendMessage(ChatColor.RED.toString() + "This argument is not supported (yet)!");
                }
            } else {
                sender.sendMessage(ChatColor.RED.toString() + "Too many arguments for this command!");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("orefinder")) {
            DisplayOreFinderInfo(sender);
            return true;
        }
        return false;
    }
    
    public void FindDiamond(Player sender) {
        DecimalFormat df = new DecimalFormat("#.##");
        if(!sender.hasPermission("orefinder.find") && !sender.hasPermission("orefinder.find.diamond")) {
            sender.sendMessage(ChatColor.RED.toString() + "You don't have permission to do this!");
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (diamondCooldowns.containsKey(sender.getName())) {
            if(currentTime - diamondCooldowns.get(sender.getName()) < FIND_DIAMOND_COOLDOWN * 1000) {
                int timePassed = (int)((currentTime - diamondCooldowns.get(sender.getName())) / 1000);
                int timeLeft = FIND_DIAMOND_COOLDOWN - timePassed;
                int minutes = timeLeft/60;
                int seconds = timeLeft % 60;
                String msg = ChatColor.GOLD.toString() + "Cooldown: ";
                if(minutes > 0) {
                    msg += ChatColor.RED.toString() + minutes + " minutes ";
                    msg += ChatColor.GOLD.toString() + "and "; 
                }
                msg += ChatColor.RED.toString() + seconds + " seconds ";
                msg += ChatColor.GOLD.toString() + "remaining";
                sender.sendMessage(msg);
                return;
            }
        }
        diamondCooldowns.put(sender.getName(), currentTime);
        World world = sender.getWorld();
        if (world.getEnvironment() == Environment.NETHER) {
            sender.sendMessage(ChatColor.GOLD.toString() + "There are no diamonds in the nether.");
            return;
        }
        Block highestBlock = world.getHighestBlockAt(sender.getLocation());
        if (highestBlock.getLocation().getBlockY() > sender.getLocation().getBlockY()) {
            Block[] diamondBlocks = new Block[0];
            //Start at players location and get nearest iron up then down, then compare distances (if there is an iron block
            for (int x = sender.getLocation().getBlockX() - 40; x <= sender.getLocation().getBlockX() + 40; x++) {
                for (int y = sender.getLocation().getBlockY(); y <= highestBlock.getLocation().getBlockY(); y++) {
                    for (int z = sender.getLocation().getBlockZ() - 40; z <= sender.getLocation().getBlockZ() + 40; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.DIAMOND_ORE) {
                            diamondBlocks = extendBlockArray(diamondBlocks, thisBlock);
                        }
                    }
                }
            }
            for (int x = sender.getLocation().getBlockX() - 40; x <= sender.getLocation().getBlockX() + 40; x++) {
                for (int y = sender.getLocation().getBlockY(); y >= 0; y--) {
                    for (int z = sender.getLocation().getBlockZ() - 40; z <= sender.getLocation().getBlockZ() + 40; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.DIAMOND_ORE) {
                            diamondBlocks = extendBlockArray(diamondBlocks, thisBlock);
                        }
                    }
                }
            }
            //Check Closest Block of Iron in the array
            double closestBlockDistance = 3000;
            for (Block b : diamondBlocks) {
                double distance = getBlockDistance(sender, b);
                if (distance < closestBlockDistance) {
                    closestBlockDistance = distance;
                }
            }
            if (closestBlockDistance != 3000) {
                sender.sendMessage(ChatColor.GOLD.toString() + "Closest diamond ore is " + ChatColor.RED.toString() + df.format(closestBlockDistance) + ChatColor.GOLD.toString() + " blocks away.");
            } else {
                sender.sendMessage(ChatColor.GOLD.toString() + "There is no close diamond ore!");
            }
            return;
        } else {
            Block[] diamondBlocks = new Block[0];
            for (int x = sender.getLocation().getBlockX() - 60; x <= sender.getLocation().getBlockX() + 60; x++) {
                for (int y = sender.getLocation().getBlockY(); y >= 0; y--) {
                    for (int z = sender.getLocation().getBlockZ() - 60; z <= sender.getLocation().getBlockZ() + 60; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.DIAMOND_ORE) {
                            diamondBlocks = extendBlockArray(diamondBlocks, thisBlock);
                        }
                    }
                }
            }
            double closestBlockDistance = 3000;
            for (Block b : diamondBlocks) {
                double distance = getBlockDistance(sender, b);
                if (distance < closestBlockDistance) {
                    closestBlockDistance = distance;
                }
            }
            if(closestBlockDistance == 3000) {
                sender.sendMessage(ChatColor.GOLD.toString() + "Closest diamond ore is " + ChatColor.RED.toString() + df.format(closestBlockDistance) + ChatColor.GOLD.toString() + " blocks away.");
            } else {
                sender.sendMessage(ChatColor.GOLD.toString() + "There is no close diamond ore!");
            }
                
            return;
        }
    }
    public void FindGold(Player sender) {
        DecimalFormat df = new DecimalFormat("#.##");
        if (!sender.hasPermission("orefinder.find") && !sender.hasPermission("orefinder.find.gold")) {
            sender.sendMessage(ChatColor.RED.toString() + "You don't have permission to do this!");
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (goldCooldowns.containsKey(sender.getName())) {
            if (currentTime - goldCooldowns.get(sender.getName()) < FIND_GOLD_COOLDOWN * 1000) {
                int timePassed = (int) ((currentTime - goldCooldowns.get(sender.getName())) / 1000);
                int timeLeft = FIND_GOLD_COOLDOWN - timePassed;
                sender.sendMessage(ChatColor.RED.toString() + "Cooldown: " + timeLeft + " Seconds left.");
                return;
            }
        }
        goldCooldowns.put(sender.getName(), currentTime);
        World world = sender.getWorld();
        if (world.getEnvironment() == Environment.NETHER) {
            sender.sendMessage(ChatColor.GOLD.toString() + "There is no gold in the nether.");
            return;
        }
        
        Block highestBlock = world.getHighestBlockAt(sender.getLocation());
        if (highestBlock.getLocation().getBlockY() > sender.getLocation().getBlockY()) {
            Block[] goldBlocks = new Block[0];
            //Start at players location and get nearest iron up then down, then compare distances (if there is an iron block
            for (int x = sender.getLocation().getBlockX() - 40; x <= sender.getLocation().getBlockX() + 40; x++) {
                for (int y = sender.getLocation().getBlockY(); y <= highestBlock.getLocation().getBlockY(); y++) {
                    for (int z = sender.getLocation().getBlockZ() - 40; z <= sender.getLocation().getBlockZ() + 40; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.GOLD_ORE) {
                            goldBlocks = extendBlockArray(goldBlocks, thisBlock);
                        }
                    }
                }
            }
            for (int x = sender.getLocation().getBlockX() - 40; x <= sender.getLocation().getBlockX() + 40; x++) {
                for (int y = sender.getLocation().getBlockY(); y >= 0; y--) {
                    for (int z = sender.getLocation().getBlockZ() - 40; z <= sender.getLocation().getBlockZ() + 40; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.GOLD_ORE) {
                            goldBlocks = extendBlockArray(goldBlocks, thisBlock);
                        }
                    }
                }
            }
            //Check Closest Block of Iron in the array
            double closestBlockDistance = 3000;
            for (Block b : goldBlocks) {
                double distance = getBlockDistance(sender, b);
                if (distance < closestBlockDistance) {
                    closestBlockDistance = distance;
                }
            }
            if (closestBlockDistance != 3000) {
                sender.sendMessage(ChatColor.GOLD.toString() + "Closest gold ore is " + ChatColor.RED.toString() + df.format(closestBlockDistance) + ChatColor.GOLD.toString() + " blocks away.");
            } else {
                sender.sendMessage(ChatColor.GOLD.toString() + "There is no close gold ore!");
            }
            return;
        } else {
            Block[] goldBlocks = new Block[0];
            for (int x = sender.getLocation().getBlockX() - 60; x <= sender.getLocation().getBlockX() + 60; x++) {
                for (int y = sender.getLocation().getBlockY(); y >= 0; y--) {
                    for (int z = sender.getLocation().getBlockZ() - 60; z <= sender.getLocation().getBlockZ() + 60; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.GOLD_ORE) {
                            goldBlocks = extendBlockArray(goldBlocks, thisBlock);
                        }
                    }
                }
            }
            double closestBlockDistance = 3000;
            for (Block b : goldBlocks) {
                double distance = getBlockDistance(sender, b);
                if (distance < closestBlockDistance) {
                    closestBlockDistance = distance;
                }
            }
            if(closestBlockDistance == 3000) {
                sender.sendMessage(ChatColor.GOLD.toString() + "Closest gold ore is " + ChatColor.RED.toString() + df.format(closestBlockDistance) + ChatColor.GOLD.toString() + " blocks away.");
            } else {
                sender.sendMessage(ChatColor.GOLD.toString() + "There is no close gold ore!");
            }
                
            return;
        }
    }

    public void FindIron(Player sender) {
        DecimalFormat df = new DecimalFormat("#.##");
        if (!sender.hasPermission("orefinder.find") && !sender.hasPermission("orefinder.find.iron")) {
            sender.sendMessage(ChatColor.RED.toString() + "You don't have permission to do this!");
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (ironCooldowns.containsKey(sender.getName())) {
            if (currentTime - ironCooldowns.get(sender.getName()) < FIND_IRON_COOLDOWN * 1000) {
                int timePassed = (int) ((currentTime - ironCooldowns.get(sender.getName())) / 1000);
                int timeLeft = FIND_IRON_COOLDOWN - timePassed;
                sender.sendMessage(ChatColor.RED.toString() + "Cooldown: " + timeLeft + " Seconds left.");
                return;
            }
        }
        ironCooldowns.put(sender.getName(), currentTime);
        World world = sender.getWorld();
        if (world.getEnvironment() == Environment.NETHER) {
            sender.sendMessage(ChatColor.GOLD.toString() + "There is no iron in the nether.");
            return;
        }
        Block highestBlock = world.getHighestBlockAt(sender.getLocation());
        if (highestBlock.getLocation().getBlockY() > sender.getLocation().getBlockY()) {
            Block[] ironBlocks = new Block[0];
            //Start at players location and get nearest iron up then down, then compare distances (if there is an iron block
            for (int x = sender.getLocation().getBlockX() - 40; x <= sender.getLocation().getBlockX() + 40; x++) {
                for (int y = sender.getLocation().getBlockY(); y <= highestBlock.getLocation().getBlockY(); y++) {
                    for (int z = sender.getLocation().getBlockZ() - 40; z <= sender.getLocation().getBlockZ() + 40; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.IRON_ORE) {
                            ironBlocks = extendBlockArray(ironBlocks, thisBlock);
                        }
                    }
                }
            }
            for (int x = sender.getLocation().getBlockX() - 40; x <= sender.getLocation().getBlockX() + 40; x++) {
                for (int y = sender.getLocation().getBlockY(); y >= 0; y--) {
                    for (int z = sender.getLocation().getBlockZ() - 40; z <= sender.getLocation().getBlockZ() + 40; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.IRON_ORE) {
                            ironBlocks = extendBlockArray(ironBlocks, thisBlock);
                        }
                    }
                }
            }
            //Check Closest Block of Iron in the array
            double closestBlockDistance = 3000;
            for (Block b : ironBlocks) {
                double distance = getBlockDistance(sender, b);
                if (distance < closestBlockDistance) {
                    closestBlockDistance = distance;
                }
            }
            if (closestBlockDistance != 3000) {
                sender.sendMessage(ChatColor.GOLD.toString() + "Closest iron ore is " + ChatColor.RED.toString() + df.format(closestBlockDistance) + ChatColor.GOLD.toString() + " blocks away.");
            } else {
                sender.sendMessage(ChatColor.GOLD.toString() + "There is no close iron ore!");
            }
            return;
        } else {
            Block[] ironBlocks = new Block[0];
            for (int x = sender.getLocation().getBlockX() - 60; x <= sender.getLocation().getBlockX() + 60; x++) {
                for (int y = sender.getLocation().getBlockY(); y >= 0; y--) {
                    for (int z = sender.getLocation().getBlockZ() - 60; z <= sender.getLocation().getBlockZ() + 60; z++) {
                        Block thisBlock = world.getBlockAt(x, y, z);
                        if (thisBlock.getBlockData().getMaterial() == Material.IRON_ORE) {
                            ironBlocks = extendBlockArray(ironBlocks, thisBlock);
                        }
                    }
                }
            }
            double closestBlockDistance = 3000;
            for (Block b : ironBlocks) {
                double distance = getBlockDistance(sender, b);
                if (distance < closestBlockDistance) {
                    closestBlockDistance = distance;
                }
            }
            sender.sendMessage(ChatColor.GOLD.toString() + "Closest iron ore is " + ChatColor.RED.toString() + df.format(closestBlockDistance) + ChatColor.GOLD.toString() + " blocks away.");
            return;
        }
    }
    
    public void DisplayOreFinderInfo(CommandSender sender) {
        String[] info = {
            ChatColor.GOLD.toString() + "----[" + ChatColor.YELLOW.toString() + "OreFinder" + ChatColor.GOLD.toString() + "]----",
            ChatColor.YELLOW.toString() + "Author: " + ChatColor.WHITE.toString() + "InsaneMatrix",
            ChatColor.YELLOW.toString() + "GitHub: " + ChatColor.WHITE.toString() + "https://github.com/InsanityMatrix/OreFinder",
            ChatColor.YELLOW.toString() + "Description: " + ChatColor.WHITE.toString() + "A plugin to find ores in survival",
            ChatColor.GOLD.toString() + "-------------------",};
        sender.sendMessage(info);
    }
    
    public double getBlockDistance(Player player, Block block) {
        double distance = player.getLocation().distance(block.getLocation());
        return distance;
    }

    public Block[] extendBlockArray(Block[] array, Block block) {
        Block[] newArray = new Block[array.length + 1];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        newArray[array.length] = block;
        return newArray;
    }
}
