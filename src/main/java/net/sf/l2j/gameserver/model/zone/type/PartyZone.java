package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;


public class PartyZone extends ZoneType
{

	public PartyZone(int id)
	{ 
		super(id);
	}

	@Override
	protected void onEnter(Creature character)
	{
		if(character instanceof Player){

			character.setInsideZone(ZoneId.NO_STORE, true);
			character.setInsideZone(ZoneId.PARTY, true);
			character.sendMessage("You entered Party Zone!");
			
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if(character instanceof Player){
			
			Player player = (Player) character;

			
			character.setInsideZone(ZoneId.NO_STORE, false);
			character.setInsideZone(ZoneId.PARTY, false);
			character.sendMessage("You left Party Zone!");
		}
		
	}
}	