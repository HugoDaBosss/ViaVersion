package us.myles.ViaVersion.commands;

import io.netty.util.ResourceLeakDetector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class ViaVersionCommand implements CommandExecutor {

    private final ViaVersionPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("viaversion.admin")) {
            if (args.length == 0) {
                sendHelp(sender);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    HashMap<Integer, List<Player>> protocols = new HashMap<Integer, List<Player>>();
                    for(ProtocolNames pn : ProtocolNames.values()) {
                        protocols.put(pn.getId(), new ArrayList<Player>());
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ViaVersion.getInstance().isPorted(p)) {
                            protocols.get(ViaVersion.getInstance().getPlayerVersion(p)).add(p);
                        } else {
                            protocols.get(ProtocolRegistry.SERVER_PROTOCOL).add(p);
                        }
                    }
                    for(Integer i : protocols.keySet()) {
                        sender.sendMessage(color("&8[&6" + ProtocolNames.getById(i).getName() + "&8]: &b" + protocols.get(i).toString()));
                    }
                } else if (args[0].equalsIgnoreCase("debug")) {
                    plugin.setDebug(!plugin.isDebug());
                    sender.sendMessage(color("&6Debug mode is now " + (plugin.isDebug() ? "&aenabled" : "&cdisabled")));
                } else if (args[0].equalsIgnoreCase("displayleaks")) {
                    if (ResourceLeakDetector.getLevel() != ResourceLeakDetector.Level.ADVANCED) {
                        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
                    } else {
                        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
                    }
                    sender.sendMessage(color("&6Leak detector is now " + (ResourceLeakDetector.getLevel() == ResourceLeakDetector.Level.ADVANCED ? "&aenabled" : "&cdisabled")));
                } else if (args[0].equalsIgnoreCase("dontbugme")) {
                    boolean newValue = !plugin.isCheckForUpdates();
                    plugin.getConfig().set("checkforupdates", newValue);
                    plugin.saveConfig();
                    sender.sendMessage(color("&6We will " + (newValue ? "&anotify you about updates." : "&cnot tell you about updates.")));
                } else if (args[0].equalsIgnoreCase("autoteam")) {
                    boolean newValue = !plugin.isAutoTeam();
                    plugin.getConfig().set("auto-team", newValue);
                    plugin.saveConfig();
                    sender.sendMessage(color("&6We will " + (newValue ? "&aautomatically team players" : "&cno longer auto team players")));
                    sender.sendMessage(color("&6All players will need to re-login for the change to take place."));
                } else {
                    sendHelp(sender);
                }
            }

        }
        return false;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&aViaVersion &c" + ViaVersion.getInstance().getVersion()));
        sender.sendMessage(color("&6Commands:"));
        sender.sendMessage(color("&2/viaversion list &7- &6Shows lists of all 1.9 clients and 1.8 clients."));
        sender.sendMessage(color("&2/viaversion debug &7- &6Toggle debug mode"));
        sender.sendMessage(color("&2/viaversion autoteam &7- &6Toggle automatically teaming to prevent colliding."));
        sender.sendMessage(color("&2/viaversion dontbugme &7- &6Toggle checking for updates."));
    }

    public String color(String string) {
        return string.replace("&", "§");
    }

    @RequiredArgsConstructor
    private enum ProtocolNames {

        V1_7_1(4, "1.7.1"),
        V1_7_6(5, "1.7.6"),
        V1_8(47, "1.8"),
        V1_9(107, "1.9"),
        V1_9_1_PRE2(108, "1.9.1-pre2");

        @Getter
        private final int id;
        @Getter
        private final String name;

        public static ProtocolNames getById(int id) {
            for(ProtocolNames pn : ProtocolNames.values()) {
                if(pn.getId() == id)
                    return pn;
            }
            return null;
        }
    }
}
