package pklogger;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.BatchPacketsEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.plugin.PluginBase;

import java.util.ArrayList;
import java.util.List;

public class Main extends PluginBase implements Listener {

    private int mode = 0;
    private List<Long> pl = new ArrayList<>();

    public void onEnable() {
        new Logger(System.getProperty("user.dir") + "/pklogger.txt");
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
                pl.add(p.getId());
                p.sendMessage("§7[PKLogger] §aData packet logging enabled");
            } else if (args[0].equals("off")) {
                pl.remove(p.getId());
                p.sendMessage("§7[PKLogger] §aData packet logging disabled");
            } else {
                return false;
            }

            return true;
        } else if (args.length == 2) {
            Player p = (Player) sender;
            if (args[1].equals("send")) {
                mode = 1;
                p.sendMessage("§7[PKLogger] §aLogging mode set to §6send");
            } else if (args[1].equals("receive")) {
                mode = 2;
                p.sendMessage("§7[PKLogger] §aLogging mode set to §6receive");
            } else if (args[1].equals("batch")) {
                mode = 3;
                p.sendMessage("§7[PKLogger] §aLogging mode set to §6batch");
            } else {
                mode = 0;
                p.sendMessage("§7[PKLogger] §aLogging mode set to §6all");
            }

            return true;
        }

        return false;
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        pl.remove(e.getPlayer().getId());
    }

    @EventHandler
    public void send(DataPacketSendEvent e) {
        if (mode == 0 || mode == 1) {
            if (pl.contains(e.getPlayer().getId())) {
                Logger.get.print(1, e.getPlayer().getName(), e.getPacket());
            }
        }
    }

    @EventHandler
    public void receive(DataPacketReceiveEvent e) {
        if (mode == 0 || mode == 2) {
            if (pl.contains(e.getPlayer().getId())) {
                Logger.get.print(2, e.getPlayer().getName(), e.getPacket());
            }
        }
    }

    @EventHandler
    public void batch(BatchPacketsEvent e) {
        if (mode == 0 || mode == 3) {
            for (DataPacket pk : e.getPackets()) {
                Logger.get.print(1, "BATCH", pk);
            }
        }
    }
}
