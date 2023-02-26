package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author @IcathiaLord
 *
 */
public class unstuckDungeon implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = {
		"exitdungeon"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.equals("exitdungeon")) {
			if(activeChar.getDungeon() != null && activeChar.getDungeonStage() == null) // No more stages
				 activeChar.teleportTo(82725, 148596, -3473, 0);
			else
				activeChar.sendMessage("can't use it right now..");
		}
		return false;
	}


	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
}
