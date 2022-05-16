package me.bright.townywars.utils;

import me.bright.townywars.TownyWars;
import me.bright.townywars.configs.Configs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class M {

    private static JavaPlugin plugin = TownyWars.getPlugin();
    private static String PREFIX = "&4Wars &7> ";

    public static void msg(CommandSender sender, String message) {
        sender.sendMessage(color(PREFIX + " &f" + message));
    }

    public static String getMessage(String path) {
        try {
            return Configs.MESSAGES_CONFIG.getConfig().getString("messages." + path);
        } catch (NullPointerException e) {
            return path;
        }
    }

    public static void log(String message) {
        plugin.getServer().getConsoleSender().sendMessage(color(message));
    }

    public static void error(String message) {
        plugin.getServer().getConsoleSender().sendMessage(color("&c&l" + message));
    }

    public static String correct(int number, String var1, String var2, String var3) {
        String correctString = "ошибка";
        int number1 = number%10;
        int number2 = number%100;
        if(number1 == 1) {
            correctString = var1;
        }
        if(number1 >= 2 && number1 <= 4) {
            correctString = var2;
        }
        if((number1 >= 5) && (number1 <= 9) || (number1 == 0) || (number2 >= 11) && (number2 <= 14)) {
            correctString = var3;
        }
        return correctString;
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&',message);
    }

    public static ArrayList<String> color(String... strings) {
        ArrayList<String> coloredList = new ArrayList<>();
        for (String string : strings) {
            coloredList.add(M.color(string));
        }
        return coloredList;
    }

    public static ArrayList<String> color(ArrayList<String> list) {
        ArrayList<String> coloredList = new ArrayList<>();
        for (String string : list) {
            coloredList.add(M.color(string));
        }
        return coloredList;
    }
}
