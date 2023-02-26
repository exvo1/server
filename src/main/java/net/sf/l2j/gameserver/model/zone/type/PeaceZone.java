package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;

/**
 * A zone extending {@link ZoneType}, notably used for peace behavior (pvp related).
 */
public class PeaceZone extends ZoneType
{
	public PeaceZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if(getId() == 110020)
		{
			final Player player = character.getActingPlayer();
			if (player != null)
				player.sendMessage("You entered [Safe Zone]");
		}

		character.setInsideZone(ZoneId.PEACE, true);

	}
	
	@Override
	protected void onExit(Creature character)
	{
		if(getId() == 110020)
		{
			final Player player = character.getActingPlayer();
			if (player != null)
				player.sendMessage("You exit [Safe Zone]");
		}
		character.setInsideZone(ZoneId.PEACE, false);

	}
}