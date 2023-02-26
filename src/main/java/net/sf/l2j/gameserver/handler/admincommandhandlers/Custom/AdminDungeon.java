/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers.Custom;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.util.Mysql;

import Customs.Events.Dungeon.DungeonManager;
import Customs.Instance.InstanceManager;



public class AdminDungeon implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_resetdungeon"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if(!st.hasMoreTokens())
			{
				activeChar.sendMessage("Write the name.");
				return false;
			}
			
			String target_name = st.nextToken();
			Player player = World.getInstance().getPlayer(target_name);
			if(player == null)
			{
				activeChar.sendMessage("Player is offline");
				return false;
			}
			
			DungeonManager.getInstance().getPlayerData().remove(player.getIP());
			DungeonManager.getInstance().getPlayerData().remove(player.gethwid());
			Mysql.set("DELETE FROM dungeon WHERE ipaddr=?",player.getIP());
			Mysql.set("DELETE FROM dungeon WHERE ipaddr=?",player.gethwid());
			activeChar.sendMessage("You cleared the dungeon limits from player: "+target_name+" success");
		}
		else if (command.equals("admin_dungeon_reload"))
		{
			if (DungeonManager.getInstance().isReloading())
			{
				activeChar.sendMessage("A reload command has already been issued.");
				return false;
			}
			
			if (DungeonManager.getInstance().reload())
				activeChar.sendMessage("dungeons.xml has been reloaded.");
			else
				activeChar.sendMessage("There are currently active dungeons running, the reload will be completed when they finish.");
		}
		else if (command.startsWith("admin_dungeonrr"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if(!st.hasMoreTokens())
			{
				activeChar.sendMessage("Write the name.");
				return false;
			}
			
			String target_name = st.nextToken();
			Player player = World.getInstance().getPlayer(target_name);
			if(player == null)
			{
				activeChar.sendMessage("Player is offline");
				return false;
			}
			
			player.setInstance(InstanceManager.getInstance().getInstance(0), true);
			player.teleportTo(82635, 148798, -3464, 25);
			activeChar.sendMessage("You cleared the dungeon limits from player: "+target_name+" success");
		}
		else if(command.startsWith("admin_getinstance")){
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if(!st.hasMoreTokens())
			{
				activeChar.sendMessage("Write the name.");
				return false;
			}
			
			String target_name = st.nextToken();
			Player player = World.getInstance().getPlayer(target_name);
			if(player == null)
			{
				activeChar.sendMessage("Player is offline");
				return false;
			}
			
			activeChar.setInstance(player.getInstance(), false);
			activeChar.sendMessage("You are with the same instance of player "+target_name);
		}
		else if (command.startsWith("admin_resetmyinstance"))
		{
			activeChar.setInstance(InstanceManager.getInstance().getInstance(0), false);
			activeChar.sendMessage("Your instance is now default");
		}
		
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return new String[] { "admin_resetdungeon" , "admin_dungeon_reload" , "admin_dungeonrr" , "admin_getinstance", "admin_resetmyinstance"};
	}
}
