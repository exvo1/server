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
package Customs.Events.DM;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.entity.Duel;

public class DMEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private Player _activeChar = null;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];
	/** Admin removed this player from event */
	private boolean _adminRemove = false;
	
	 static L2Skill noblesse = SkillTable.getInstance().getInfo(1323, 1);
	 
	/**
	 * Initialize the teleporter and start the delayed task<br><br>
	 *
	 * @param activeChar as Player<br>
	 * @param coordinates as int[]<br>
	 * @param fastSchedule as boolean<br>
	 * @param adminRemove as boolean<br>
	 */
	public DMEventTeleporter(Player activeChar, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_activeChar = activeChar;
		_coordinates = coordinates;
		_adminRemove = adminRemove;

		loadTeleport(fastSchedule);
	}

	/**
	 * Initialize the teleporter and start the delayed task<br><br>
	 *
	 * @param activeChar as Player<br>
	 * @param fastSchedules
	 * @param fastSchedule as boolean<br>
	 * @param adminRemove as boolean<br>
	 */
	public DMEventTeleporter(Player activeChar, boolean fastSchedule, boolean adminRemove)
	{
		_activeChar = activeChar;
		_coordinates = DMConfig.DM_EVENT_PLAYER_COORDINATES.get(Rnd.get(DMConfig.DM_EVENT_PLAYER_COORDINATES.size()));
		_adminRemove = adminRemove;

		loadTeleport(fastSchedule);
	}

	private void loadTeleport(boolean fastSchedule)
	{
		long delay = (DMEvent.isStarted() ? DMConfig.DM_EVENT_RESPAWN_TELEPORT_DELAY : DMConfig.DM_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;
		ThreadPool.schedule(this, fastSchedule ? 0 : delay);
	}	
	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info<br><br>
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (_activeChar == null) return;


		
		Summon summon = _activeChar.getSummon();

		if (summon != null)
			summon.unSummon(_activeChar);

		if (DMConfig.DM_EVENT_EFFECTS_REMOVAL == 0 || (DMConfig.DM_EVENT_EFFECTS_REMOVAL == 1 && (_activeChar.getTeamEvent() == 0 || (_activeChar.isInDuel() && _activeChar.getDuelState() != Duel.DuelState.INTERRUPTED))))
			_activeChar.stopAllEffectsExceptThoseThatLastThroughDeath();

		if (_activeChar.isInDuel())
			_activeChar.setDuelState(Duel.DuelState.INTERRUPTED);


		_activeChar.doRevive();

		if(DMEvent.isRewarding() || DMEvent.isInactivating() || DMEvent.isInactive())
			_activeChar.teleportTo(82635, 148798, -3464, 0);
		else
			_activeChar.teleportTo((_coordinates[0] + Rnd.get(101)) - 50, (_coordinates[1] + Rnd.get(101)) - 50, _coordinates[2], 0);
		
		if(_activeChar.getKarma() > 0)
			_activeChar.setKarma(0);
		
	//	_activeChar.teleportTo(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], 0);

		if (DMEvent.isStarted() && !_adminRemove)
		{
			_activeChar.setTeamEvent(1);
		}
		else
		{
			_activeChar.setTeamEvent(0);
		}

		_activeChar.setCurrentCp(_activeChar.getMaxCp());
		_activeChar.setCurrentHp(_activeChar.getMaxHp());
		_activeChar.setCurrentMp(_activeChar.getMaxMp());
		 noblesse.getEffects(_activeChar, _activeChar);
		_activeChar.broadcastStatusUpdate();
		_activeChar.broadcastTitleInfo();
		_activeChar.broadcastUserInfo();		
	}
}