package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.ArrayList;
import java.util.List;

public class L2SkillTeleport extends L2Skill
{
	private final String _recallType;
	private final Location _loc;

	private List<Location> _locUnstuck = null;

	public L2SkillTeleport(StatsSet set)
	{
		super(set);
		
		_recallType = set.getString("recallType", "");

		String teleCoordsUnstuck = set.getString("teleCoordsUnstuck", null);
		if(teleCoordsUnstuck != null)
		{
			_locUnstuck = new ArrayList<>();
			try
			{
				String[] valuesSplit = teleCoordsUnstuck.split(";");
				for(String str : valuesSplit)
				{
					String[] valuesSplitTp = str.split(",");
					_locUnstuck.add(new Location(Integer.parseInt(valuesSplitTp[0]), Integer.parseInt(valuesSplitTp[1]), Integer.parseInt(valuesSplitTp[2])));
				}
			}
			catch (Exception e)
			{
				_locUnstuck = null;
			}
		}

		String coords = set.getString("teleCoords", null);
		if (coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_loc = new Location(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]), Integer.parseInt(valuesSplit[2]));
		}
		else
			_loc = null;
	}
	
	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets)
	{
		if (activeChar instanceof Player)
		{
			// Check invalid states.
			if (((Player) activeChar).isInTournament() || activeChar.isAfraid() || ((Player) activeChar).isInOlympiadMode() || activeChar.isInsideZone(ZoneId.BOSS))
				return;
		}
		
		boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			
			if (target instanceof Player)
			{
				Player targetChar = (Player) target;
				
				// Check invalid states.
				if (targetChar.isFestivalParticipant() || targetChar.isInJail() || targetChar.isInDuel())
					continue;
				
				if (targetChar != activeChar)
				{
					if (targetChar.isInOlympiadMode())
						continue;
					
					if (targetChar.isInsideZone(ZoneId.BOSS))
						continue;
				}
			}
			
			Location loc = null;
			if (getSkillType() == L2SkillType.TELEPORT)
			{
				if (_loc != null)
				{
					if (!(target instanceof Player) || !target.isFlying())
						loc = _loc;
				}
			}
			else
			{
				if (_recallType.equalsIgnoreCase("Castle"))
					loc = MapRegionData.getInstance().getLocationToTeleport(target, TeleportType.CASTLE);
				else if (_recallType.equalsIgnoreCase("ClanHall"))
					loc = MapRegionData.getInstance().getLocationToTeleport(target, TeleportType.CLAN_HALL);
				else if (_locUnstuck != null && !_locUnstuck.isEmpty())
					loc = _locUnstuck.get(Rnd.get(_locUnstuck.size() - 1));
				else
					loc = MapRegionData.getInstance().getLocationToTeleport(target, TeleportType.TOWN);
			}
			
			if (loc != null)
			{
				if (target instanceof Player)
					((Player) target).setIsIn7sDungeon(false);
				
				target.teleportTo(loc, 20);
			}
		}
		
		activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, isStaticReuse());
	}
}