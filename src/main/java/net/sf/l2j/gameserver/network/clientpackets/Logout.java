package net.sf.l2j.gameserver.network.clientpackets;

import Customs.Events.CTF.CTFEvent;
import Customs.Events.DM.DMEvent;
import Customs.Events.TvT.TvTEvent;
import net.sf.l2j.gameserver.data.manager.FestivalOfDarknessManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExVariationResult;
import net.sf.l2j.gameserver.network.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public final class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (player.getActiveEnchantItem() != null || player.isLocked())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		//custom dungeon
		if (player.getDungeon() != null)
		{
			player.sendMessage("You cannot logout while in a dungeon.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		

		//custom tournament
		if (player.isRegisteredInTournament() || player.isInTournament())
		{
			player.sendMessage("You can not leave the game while attending an event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		
		if (player.isInsideZone(ZoneId.NO_RESTART))
		{
			player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(player))
		{
			player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isFestivalParticipant() && FestivalOfDarknessManager.getInstance().isFestivalInitialized())
		{
			player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		player.removeFromBossZone();

		//custom for augment window problem
		player.sendPacket(new ExVariationResult(0, 0, 0));


        CTFEvent.onLogout(player);
        DMEvent.onLogout(player);
      //LMEvent.onLogout(player);
        TvTEvent.onLogout(player);

		player.logout(true);


	}
}