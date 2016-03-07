package us.myles.ViaVersion.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.util.ReflectionUtil;

public class PortalListener implements org.bukkit.event.Listener {
	
	private HashMap<UUID, Integer> portaltime;
	
	public PortalListener(ViaVersionPlugin plugin)	{
		portaltime = new HashMap<UUID, Integer>();
		new BukkitRunnable() {

			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(p.getLocation().getBlock().getType() == Material.PORTAL && p.getGameMode() != GameMode.CREATIVE) {
						int time = registerPortal(p.getUniqueId());
						if(time == 15) {
							Location l = p.getLocation();
							World from = l.getWorld();
							World to;
							double x,y = l.getY(),z;
							if(from.getEnvironment() == Environment.THE_END)
								return;
							else if(from.getEnvironment() == Environment.NORMAL) {
								to = Bukkit.getWorld(from.getName() + "_nether");
								x = l.getX()/8;
								z = l.getZ()/8;
							}
							else {
								to = Bukkit.getWorld(from.getName().substring(0, from.getName().length()-7));
								x = l.getX()*8;
								z = l.getZ()*8;
							}
							if(to == null)
								return;
							Location toloc = new Location(to,x,y,z);
							TravelAgent agent = null;
							try {
								Class cw = ReflectionUtil.obc("CraftWorld");
								Object ws = cw.getMethod("getHandle").invoke(cw.cast(to));
								Object cta = ReflectionUtil.obc("CraftTravelAgent")
										.getConstructor(ReflectionUtil.nms("WorldServer"))
										.newInstance(ws);
								agent = (TravelAgent)cta;
							} catch (ClassNotFoundException e1) {
								e1.printStackTrace();
							} catch (NoSuchMethodException e1) {
								e1.printStackTrace();
							} catch (SecurityException e1) {
								e1.printStackTrace();
							} catch (InvocationTargetException e1) {
								e1.printStackTrace();
							} catch (IllegalAccessException e1) {
								e1.printStackTrace();
							} catch (IllegalArgumentException e1) {
								e1.printStackTrace();
							} catch (InstantiationException e1) {
								e1.printStackTrace();
							}
							if(agent == null)
								return;
							agent.setCanCreatePortal(true);
							PlayerPortalEvent e = new PlayerPortalEvent(p, l, toloc, agent, TeleportCause.NETHER_PORTAL);
							Bukkit.getPluginManager().callEvent(e);
							if(e.isCancelled())
								return;
							Location portal = e.getPortalTravelAgent().findOrCreate(e.getTo());
							if(portal != null) {
								portal = portal.add(0.5, 0, 0.5); //Make players spawn in the middle of the portal
								p.teleport(portal);
							}
						}
					}
					else
						removePortal(p.getUniqueId());
				}
			}
			
		}.runTaskTimer(plugin, 5L, 5L);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		if(this.portaltime.containsKey(uuid))
			portaltime.remove(uuid);
	}
	
	private int registerPortal(UUID uuid) {
		if(!this.portaltime.containsKey(uuid)) {
			portaltime.put(uuid, 1);
			return 1;
		}
		else {
			int time = portaltime.get(uuid)+1;
			portaltime.put(uuid, time);
			return time;
		}
	}
	
	private void removePortal(UUID uuid) {
		if(this.portaltime.containsKey(uuid))
			portaltime.remove(uuid);
	}

}
