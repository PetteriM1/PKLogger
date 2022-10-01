package pklogger;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.plugin.PluginBase;

import java.util.HashMap;
import java.util.Map;

public class Main extends PluginBase implements Listener {

    private final Map<Long, Logger> loggers = new HashMap<>();

    public void onEnable() {
        getDataFolder().mkdir();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou can run this command only as a player");
            return true;
        }
        if (args.length == 1) {
            Player p = (Player) sender;
            if (args[0].equals("on")) {
                if (loggers.containsKey(p.getId())) {
                    p.sendMessage("§7[PKLogger] §cData packet logging is already enabled");
                } else {
                    loggers.put(p.getId(), new Logger(getDataFolder() + "/" + p.getName() + "-" + System.currentTimeMillis() + ".txt"));
                    p.sendMessage("§7[PKLogger] §aData packet logging enabled");
                }
                return true;
            } else if (args[0].equals("off")) {
                if (removeLogger(p)) {
                    p.sendMessage("§7[PKLogger] §aData packet logging disabled");
                } else {
                    p.sendMessage("§7[PKLogger] §cData packet logging is already disabled");
                }
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        removeLogger(e.getPlayer());
    }

    @EventHandler
    public void send(DataPacketSendEvent e) {
        Player p = e.getPlayer();
        Logger logger = loggers.get(p.getId());
        if (logger != null) {
            logger.print(true, p.getName(), e.getPacket());
        }
    }

    @EventHandler
    public void receive(DataPacketReceiveEvent e) {
        Player p = e.getPlayer();
        Logger logger = loggers.get(p.getId());
        if (logger != null) {
            logger.print(false, p.getName(), e.getPacket());
        }
    }

    private boolean removeLogger(Player p) {
        long id = p.getId();
        Logger logger = loggers.get(id);
        if (logger != null) {
            loggers.remove(id);
            logger.shutdown();
            return true;
        }
        return false;
    }
}
