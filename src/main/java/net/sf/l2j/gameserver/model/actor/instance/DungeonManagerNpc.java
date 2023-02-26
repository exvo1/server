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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import Customs.Events.Dungeon.DungeonManager;



/**
 * @author Anarchy
 */
public class DungeonManagerNpc extends Folk
{
	public DungeonManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("dungeon"))
		{
			if (DungeonManager.getInstance().isInDungeon(player) || player.isInOlympiadMode())// || (EventManager.getInstance().getCurrentEvent() != null && EventManager.getInstance().getActiveEvent().isInEvent(player)))
			{
				player.sendMessage("You are currently unable to enter a Dungeon. Please try again later.");
				return;
			}
			
			int dungeonId = Integer.parseInt(command.substring(8));
			if(dungeonId == 1 || dungeonId == 2)
			{
				DungeonManager.getInstance().enterDungeon(dungeonId, player);
			}
			/*else if(dungeonId == 3)
			{
				ClanDungeonOpenWorld.getInstance().join(player);
			}*/
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	public static String getPlayerStatus(Player player, int dungeonId)
	{
		String s = "You can enter";
		
	//	String ip = player.gethwid(); // Is ip or hwid?
		String ip = player.getIP(); //TODO:Fix hwid so i can enable it
		if (DungeonManager.getInstance().getPlayerData().containsKey(ip) && DungeonManager.getInstance().getPlayerData().get(ip)[dungeonId] > 0)
		{
			long total = (DungeonManager.getInstance().getPlayerData().get(ip)[dungeonId] + (1000*60*60*12)) - System.currentTimeMillis();
			
			if (total > 0)
			{
				int hours = (int) (total/1000/60/60);
				int minutes = (int) ((total/1000/60) - hours*60);
				int seconds = (int) ((total/1000) - (hours*60*60 + minutes*60));
				
				s = String.format("%02d:%02d:%02d", hours, minutes, seconds);
			}
		}
		
		return s;
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setFile("data/html/mods/dungeon/"+getNpcId()+(val == 0 ? "" : "-"+val)+".htm");
		
		String[] s = htm.getHtml().split("%");
		for (int i = 0; i < s.length; i++)
		{
			if (i % 2 > 0 && s[i].contains("dung "))
			{
				StringTokenizer st = new StringTokenizer(s[i]);
				st.nextToken();
				htm.replace("%"+s[i]+"%", getPlayerStatus(player, Integer.parseInt(st.nextToken())));
			}
		}
		
		htm.replace("%objectId%", getObjectId()+"");
		
		player.sendPacket(htm);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/mods/dungeon/" + filename + ".htm";
	}
}
