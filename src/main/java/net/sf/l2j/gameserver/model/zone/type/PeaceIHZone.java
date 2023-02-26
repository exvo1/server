package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneType;

import java.util.ArrayList;
import java.util.List;

/**
 * A zone peace island helper {@link ZoneType}, type unstuck).
 */
public class PeaceIHZone extends SpawnZoneType
{
	private int _id;
	public List<Location> _locations = new ArrayList<>();

	public PeaceIHZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("id"))
			_id = Integer.parseInt(value);
		else if (name.equals("locs"))
		{
			for (String locs : value.split(";"))
				_locations.add(new Location(Integer.parseInt(locs.split(",")[0]), Integer.parseInt(locs.split(",")[1]), Integer.parseInt(locs.split(",")[2])));
		}
		else
			super.setParameter(name, value);
	}

	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.ISLAND_RES, true);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.ISLAND_RES, false);
	}

	@Override
	public int getId()
	{
		return _id;
	}

	public Location getLoc()
	{
		return _locations.get(Rnd.get(_locations.size()));
	}
}