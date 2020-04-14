/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.insanematrix.orefinder;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 *
 * @author dvpie
 */
public class FindTabCompleter implements TabCompleter {
    public final String[] ores = {"iron", "gold", "diamond"};
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("find")) {
            List<String> list = new ArrayList<>();
            if(args.length == 0) {
                for(String str : ores) {
                    list.add(str);
                }
                return list;
            } else if (args.length == 1) {
                if(args[0] == null || args[0].equals("")) {
                    for(String str : ores) {
                        list.add(str);
                    }
                } else {
                    String input = args[0].toLowerCase();
                    for(String str : ores) {
                        if(str.contains(input))
                            list.add(str);
                    }
                }
                return list;
            }
        }
        return null;
    }
}
