package vg.civcraft.mc.citadel.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class Stats extends PlayerCommand{

	private List<Group> run = new ArrayList<Group>();
	
	public Stats(String name) {
		super(name);
		setIdentifier("cts");
		setDescription("Lists the stats about a certain group.");
		setUsage("/cts <group>");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("meh");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		
		if (g == null){
			p.sendMessage(ChatColor.RED + "This group does not exist.");
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!g.isMember(uuid) && !(p.isOp() || p.hasPermission("citadel.admin"))){
			p.sendMessage(ChatColor.RED + "You are not on this group.");
			return true;
		}
		synchronized(run){
			if (run.contains(g)){
				p.sendMessage(ChatColor.RED + "That group is already being searched for.");
				return true;
			}
			run.add(g);
		}
		Bukkit.getScheduler().runTaskAsynchronously(Citadel.getInstance(), new StatsMessage(p, g));
		return true;
	}
	
	public class StatsMessage implements Runnable{

		private final Player p;
		private final Group g;
		
		public StatsMessage(Player p, Group g){
			this.p = p;
			this.g = g;
		}
		
		@Override
		public void run() {
			String message = ChatColor.GREEN + "The amount of reinforcements on this group are: ";
			int count = Citadel.getCitadelDatabase().getReinCount(g.getName());
			message += count;
			synchronized(run){
				run.remove(g);
			}
			if (p != null && !p.isOnline()) // meh be safe
				return;
			p.sendMessage(message);
		}
		
	}

}
