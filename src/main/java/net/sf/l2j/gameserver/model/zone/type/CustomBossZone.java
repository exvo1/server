package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;


public class CustomBossZone extends ZoneType
{
	public CustomBossZone(int id)
	{
		super(id);
	}
	

	@Override
	protected void onEnter(Creature character)
	{
		if(character instanceof Player)
		{
			Player activeChar = ((Player) character);
			
			character.setInsideZone(ZoneId.NO_STORE, true);
			character.setInsideZone(ZoneId.CUSTOMBOSS, true);
			character.sendMessage("You entered A Boss Zone");

		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if(character instanceof Player){
			Player player = (Player) character;


			character.setInsideZone(ZoneId.NO_STORE, false);
			character.setInsideZone(ZoneId.CUSTOMBOSS, false);
			character.sendMessage("You Exited A Boss Zone");

		}
		
	}
}