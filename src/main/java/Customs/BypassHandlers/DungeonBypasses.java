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
package Customs.BypassHandlers;


import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import Customs.Instance.InstanceManager;
import Customs.memo.PlayerMemoCustom;


/**
 * @author Anarchy
 *
 */
public class DungeonBypasses implements IBypassHandler
{
	@Override
	public boolean handleBypass(String bypass, Player activeChar)
	{
		if (bypass.startsWith("bp_reward"))
		{
			int type = Integer.parseInt(bypass.substring(10));
			int itemId = 0;
			int count = 1;
			
			switch (type)
			{
				case 0:
				{
					itemId = 9961;
					break;
				}
				case 1:
				{
					itemId = 9962;
					break;
				}
				case 2:
				{
					itemId = 9960;
					break;
				}
				case 3:
				{
					itemId = 9959;
					break;
				}
				case 4:
				{
					itemId = 9958;
					break;
				}
				case 5:
				{
					itemId = 9957;
					break;
				}
			}
			
			if (itemId == 0)
			{
				System.out.println(activeChar.getName()+" tried to send custom id on dungeon solo rewards.");
				return false;
			}
			
			ItemInstance item = activeChar.addItem("dungeon reward", itemId, count, null, true);
			
			PlayerMemoCustom.setVar(activeChar, "delete_temp_item_"+item.getObjectId(), item.getObjectId(), System.currentTimeMillis()+(1000*60*60*6));
			
			//TempItemsManager.getInstance().addItem(new TempItem(activeChar.getObjectId(), item.getObjectId(), System.currentTimeMillis()+(1000*60/*1000*60*60*6*/)));
			activeChar.setInstance(InstanceManager.getInstance().getInstance(0), true);
			activeChar.setDungeon(null);
			
			activeChar.teleportTo(82635, 148798, -3464, 25);
		}
		else if (bypass.startsWith("bp_party_reward"))
		{
			int type = Integer.parseInt(bypass.substring(16));
			int itemId = 0;
			int count = 1;
			
			switch (type)
			{
				case 0:
				{
					itemId = 7179;
					count = 500;
					break;
				}
			}
			
			if (itemId == 0)
			{
				System.out.println(activeChar.getName()+" tried to send custom id on dungeon party rewards.");
				return false;
			}
			
			ItemInstance item = activeChar.addItem("dungeon reward", itemId, count, null, true);
			if(!item.isStackable()) // Possible a temp item
				PlayerMemoCustom.setVar(activeChar, "delete_temp_item_"+item.getObjectId(), item.getObjectId(), System.currentTimeMillis()+(1000*60*60*6));
			
			//TempItemsManager.getInstance().addItem(new TempItem(activeChar.getObjectId(), item.getObjectId(), System.currentTimeMillis()+(1000*60*60*6)));
			activeChar.setInstance(InstanceManager.getInstance().getInstance(0), true);
			activeChar.setDungeon(null);
			
			activeChar.teleportTo(82635, 148798, -3464, 25);
		}
		
		return true;
	}

	@Override
	public String[] getBypassHandlersList()
	{
		return new String[] { "bp_reward", "bp_party_reward" };
	}
}
