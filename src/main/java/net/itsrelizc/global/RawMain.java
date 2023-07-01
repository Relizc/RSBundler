package net.itsrelizc.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.itsrelizc.buildtools.BuildTools;
import net.itsrelizc.commands.CommandLobby;
import net.itsrelizc.commands.CommandServerMenu;
import net.itsrelizc.commands.CommandShutdown;
import net.itsrelizc.commands.tests.OpenDemoMenu;
import net.itsrelizc.commands.tests.WarpWithCat;
import net.itsrelizc.debugger.Debugger;
import net.itsrelizc.hooks.SimplePackedInjector;
import net.itsrelizc.nerdbot.Watcher;
import net.itsrelizc.networking.Communication;
import net.itsrelizc.networking.CommunicationInput;
import net.itsrelizc.networking.CommunicationType;
import net.itsrelizc.networking.executors.KickPlayer;
import net.itsrelizc.npc.NPC;
import net.itsrelizc.players.Commanding;
import net.itsrelizc.players.Grouping;
import net.itsrelizc.players.Profile;
import net.itsrelizc.players.TablistUtils;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;

public class RawMain extends JavaPlugin implements Listener{
	
	private Properties prop;
	public static String srvid = null;
	
	public static SimplePackedInjector a = new SimplePackedInjector();
	
	private static Object getPrivateField(Object object, String field)throws SecurityException,
	    NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	    Class<?> clazz = object.getClass();
	    Field objectField = clazz.getDeclaredField(field);
	    objectField.setAccessible(true);
	    Object result = objectField.get(object);
	    objectField.setAccessible(false);
	    return result;
	}
	 
	public void unRegisterBukkitCommand(PluginCommand cmd) {
	    try {
	        Object result = getPrivateField(this.getServer().getPluginManager(), "commandMap");
	        SimpleCommandMap commandMap = (SimpleCommandMap) result;
	        Object map = getPrivateField(commandMap, "knownCommands");
	        @SuppressWarnings("unchecked")
	        HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
	        knownCommands.remove(cmd.getName());
	        for (String alias : cmd.getAliases()){
	           if(knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(this.getName())){
	                knownCommands.remove(alias);
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		getLogger().info("SFN Global Library had been sucessfully installed!");

		Bukkit.getPluginCommand("menu1").setExecutor(new OpenDemoMenu());
		
		Bukkit.getPluginCommand("servermenu").setExecutor(new CommandServerMenu());
		Bukkit.getPluginCommand("servermenu").setAliases(ChatUtils.fromArgs("sm", "menuserver"));
		
		Bukkit.getPluginCommand("catwarp").setExecutor(new WarpWithCat());
		Bukkit.getPluginCommand("lobby").setExecutor(new CommandLobby());
		
		Bukkit.getPluginCommand("stop").setExecutor(new CommandShutdown());
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		BuildTools.registerAllFunctions(this);
		
		Commanding.initlizeBlacklistCommands(this);
		
		
		this.prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("server.properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			prop.load(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Handler handler = new Handler() {

			@Override
			public void publish(LogRecord record) {
				if (record.getLevel() == Level.SEVERE) {
					Communication com = new Communication(CommunicationType.ACTIVE_ERROR_BROADCAST);
					byte b = 0x00;
					if (prop.getProperty("rid").equalsIgnoreCase("t")) {
						b = 0x00;
					} else if (prop.getProperty("rid").equalsIgnoreCase("s")) {
						b = 0x01;
					} else if (prop.getProperty("rid").equalsIgnoreCase("m")) {
						b = 0x02;
					} else if (prop.getProperty("rid").equalsIgnoreCase("b")) {
						b = 0x03;
					} else if (prop.getProperty("rid").equalsIgnoreCase("g")) {
						b = 0x04;
					}
					com.writeByte(b);
					com.writeString(prop.getProperty("sid"));
					com.writeString(record.getMessage());
					try {
						com.sendMessage();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Communication com = new Communication(CommunicationType.ACTIVE_LOG);
				byte b = 0x00;
				if (prop.getProperty("rid").equalsIgnoreCase("t")) {
					b = 0x00;
				} else if (prop.getProperty("rid").equalsIgnoreCase("s")) {
					b = 0x01;
				} else if (prop.getProperty("rid").equalsIgnoreCase("m")) {
					b = 0x02;
				} else if (prop.getProperty("rid").equalsIgnoreCase("b")) {
					b = 0x03;
				} else if (prop.getProperty("rid").equalsIgnoreCase("g")) {
					b = 0x04;
				}
				com.writeByte(b);
				com.writeString(prop.getProperty("sid"));
				if (record.getLevel() == Level.WARNING) {
					com.writeByte((byte) 0x01);
				} else if (record.getLevel() == Level.SEVERE) {
					com.writeByte((byte) 0x02);
				} else {
					com.writeByte((byte) 0x00);
				}
				com.writeString(record.getMessage());
				try {
					com.sendMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			@Override
			public void flush() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void close() throws SecurityException {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		Bukkit.getLogger().addHandler(handler);
		
		Communication com = new Communication(CommunicationType.HANDSHAKE_LAUNCH_SUCESS);
		byte b = 0x00;
		if (prop.getProperty("rid").equalsIgnoreCase("t")) {
			b = 0x00;
		} else if (prop.getProperty("rid").equalsIgnoreCase("s")) {
			b = 0x01;
		} else if (prop.getProperty("rid").equalsIgnoreCase("m")) {
			b = 0x02;
		} else if (prop.getProperty("rid").equalsIgnoreCase("b")) {
			b = 0x03;
		} else if (prop.getProperty("rid").equalsIgnoreCase("g")) {
			b = 0x04;
		}
		final byte c = b;
		com.writeByte(b);
		com.writeString(prop.getProperty("version"));
		com.writeString(prop.getProperty("sid"));
		com.writeString(prop.getProperty("name"));
		com.writeString(prop.getProperty("type"));
		com.writeShort((short) Bukkit.getPort());
		
		try {
			com.sendMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				Communication c1 = new Communication(CommunicationType.PING);
				c1.writeByte(c);
				c1.writeString(prop.getProperty("sid"));
				c1.writeString(prop.getProperty("name"));
				c1.writeString(TPSUtils.getTPS().toString());
				
				Runtime r = Runtime.getRuntime();
				long memUsed = (r.totalMemory() - r.freeMemory()) / 1048576;
				
				c1.writeLong(memUsed);
				
				List<List<Object>> li = new ArrayList<List<Object>>();
				for (Player player : Bukkit.getOnlinePlayers()) {
					List<Object> op = new ArrayList<Object>();
					op.add(player.getName());
					op.add(true);
					op.add(player.getUniqueId().toString());
					op.add(false);
					li.add(op);
				}
				
				c1.writeTypeArray(li);
				
				try {
					c1.sendMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		}, 0L, 20L);
		
		Me.setCode(prop.getProperty("sid"));
		Me.setRAMByte(b);
		Me.plugin = this;
		
		Grouping.initlizeRankGroups();
		
		TPSUtils.startRecording(this);
		
		Watcher.initlize(this);
		
		Debugger.enable(this);
		
		TablistUtils.startTicking();
	}
	
	@EventHandler
	public void playerlogin(PlayerJoinEvent event) {
//		PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
//		packet.getChatComponents().write(0, WrappedChatComponent.fromText("§e§lRELIZC NETWORK§r\n§bmc.itsrelizc.net\n")).write(1, WrappedChatComponent.fromText("\n§b§lNews: §r§eWelcome to Beta Testing!\n§aYour fun starts here!\n\n§7Server: §8[RS-" + prop.getProperty("rid").toUpperCase() + prop.getProperty("sid") + "]"));
//		try {
//			ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet);
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		
		TablistUtils.initlizeDefault(event.getPlayer());
		
		//a.addPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void mov(PlayerMoveEvent event) {
		if (Profile.awaitCreation.contains(event.getPlayer())) {
			event.setCancelled(true);
		} else if (Profile.awaitSetUUID.contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void playerlog(PlayerLoginEvent event) {
		//BanUtils.checkLogin(event);
		if (event.getKickMessage() != null) {
			String msg = event.getKickMessage();
			int code = 0;
			if (event.getResult() == Result.KICK_BANNED) {
				code = 570;
			} else if (event.getResult() == Result.KICK_FULL) {
				code = 470;
			} else if (event.getResult() == Result.KICK_OTHER) {
				code = 270;
			} else if (event.getResult() == Result.KICK_WHITELIST) {
				code = 370;
			}
			
			event.setKickMessage(msg + "\n\n§80x" + String.format("%08X", code) + " (" + event.getPlayer().getUniqueId().toString().replace("-", "") + ")");
		}
	}
	
	@EventHandler
	public void playerlog(PlayerKickEvent event) {
		if (event.getReason() != null) {
			String msg = event.getReason();
			
			int code = 131;
			
			event.setReason(msg + "\n\n§80x" + String.format("%08X", code) + " (" + event.getPlayer().getUniqueId().toString().replace("-", "") + ")");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("libraryversion")) {
			String version = this.getDescription().getVersion();
			String message = "§aThis server is running on a ";
			String msg = "§b" + version.split("-")[1] + "§a ";
			if (version.contains("alpha")) {
				message += msg + "§c§lALPHA TEST VERSION§r§a";
			} else if (version.contains("alpha")) {
				message += msg + "§e§lBETA RELEASE VERSION§r§a";
			} else if (version.contains("stable")) {
				message += msg + "§a§lSTABLE VERSION§r§a";
			} else if (version.contains("final")) {
				message += msg + "§b§lFINAL VERSION§r§a";
			}
			message += " of SFNLibrary modded by Salty Fish Network Developers.";
			sender.sendMessage(message);
			return true;
		}
		return false;
	}
	
}
