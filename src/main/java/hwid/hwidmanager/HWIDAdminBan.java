package hwid.hwidmanager;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

import hwid.HwidConfig;

public class HWIDAdminBan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ban_hwid","admin_unban_hwid","admin_reload_hwid"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String targetName = "";
		Player targetPlayer = null;
/*		if (st.hasMoreTokens())
			targetName = st.nextToken();

		Player targetPlayer = World.getInstance().getPlayer(targetName);
		if(targetName.equals("") || targetPlayer == null){
			activeChar.sendMessage("Usage: //admin_ban_hwid <account_name>.");
			return false;
		}
		*/
		if (st.hasMoreTokens())
		{
			targetName = st.nextToken();
			targetPlayer = World.getInstance().getPlayer(targetName);
		}
		else
		{
			// If there is no name, select target
			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof Player)
				targetPlayer = (Player) activeChar.getTarget();
		}
		
		// Can't ban yourself
		if (targetPlayer != null && targetPlayer.equals(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
			return false;
		}
		
		
		if (!HwidConfig.ALLOW_GUARD_SYSTEM)
			return false;
		
		if (activeChar == null)
			return false;
		
		if (command.startsWith("admin_ban_hwid"))
		{
			//WorldObject playerTarger = activeChar.getTarget();

		/*	if (playerTarger != null && !playerTarger.equals(activeChar))
			{
				activeChar.sendMessage("Target is empty");
				return false;
			}*/
			
			if (targetPlayer == null && targetName.equals("") )
			{
				activeChar.sendMessage("Usage: //ban_hwid <account_name> (if none, target char's account gets banned).");
				return false;
			}

			if(targetPlayer != null){
				HWIDBan.addHWIDBan(targetPlayer.getClient(),targetPlayer.getName());
				targetPlayer.logout(false);
				activeChar.sendMessage(targetPlayer.getName() + " banned in HWID");
			}

		}
		else if(command.startsWith("admin_unban_hwid")){
			if(targetPlayer != null)
				HWIDBan.delHWIDBan(targetPlayer);
		}
		else if(command.startsWith("admin_reload_hwid")){
			HWIDBan.reload();
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}