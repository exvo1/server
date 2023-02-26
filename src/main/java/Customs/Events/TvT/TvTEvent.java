/*
 * Copyright (C) 2004-2013 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package Customs.Events.TvT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import Custom.CustomConfig;
import Custom.loadCustomMods;
import Customs.Events.DM.DMConfig;
import Customs.tasks.eventAntiAfk;

public class TvTEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}
	
	protected static final Logger _log = Logger.getLogger(TvTEvent.class.getName());
	/** html path **/
	private static final String htmlPath = "data/html/mods/events/tvt/";
	/** The teams of the TvTEvent */
	 static TvTEventTeam[] _teams = new TvTEventTeam[2];
	/** The state of the TvTEvent */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc */
	private static L2Spawn _npcSpawn = null;
	/** the npc instance of the participation npc */
	private static Npc _lastNpcSpawn = null;
	
	/**
	 * No instance of this class!
	 */
	private TvTEvent()
	{
	}
	
	/**
	 * Teams initializing
	 */
	public static void init()
	{
		_teams[0] = new TvTEventTeam(TvTConfig.TVT_EVENT_TEAM_1_NAME, TvTConfig.TVT_EVENT_TEAM_1_COORDINATES ,  CustomConfig.TEAM_1_COLOR);
		_teams[1] = new TvTEventTeam(TvTConfig.TVT_EVENT_TEAM_2_NAME, TvTConfig.TVT_EVENT_TEAM_2_COORDINATES,  CustomConfig.TEAM_2_COLOR);
	}
	
	/**
	 * Starts the participation of the TvTEvent
	 * 1. Get NpcTemplate by TvTProperties.TVT_EVENT_PARTICIPATION_NPC_ID
	 * 2. Try to spawn a new npc of it
	 * 
	 * @return boolean: true if success, otherwise false
	 */
	public static boolean startParticipation()
	{
		NpcTemplate tmpl = NpcData.getInstance().getTemplate(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
			_log.warning("TvTEventEngine: L2EventManager is a NullPointer -> Invalid npc id in configs?");
			return false;
		}
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
		/*	_npcSpawn.setLocx(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.getAmount();
			_npcSpawn.setHeading(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("TvT Event");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
			*/
			//_npcSpawn = new Spawn(tmpl);
			_npcSpawn.setLoc(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0],TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1],TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2], TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			_npcSpawn.setRespawnState(true);
			SpawnTable.getInstance().addSpawn(_npcSpawn, false);		      	
			_npcSpawn.doSpawn(false);
			_lastNpcSpawn = _npcSpawn.getNpc();
			_lastNpcSpawn.getStatus().setCurrentHp(9.99999999E8D);
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getNpc().getX(), _npcSpawn.getNpc().getY(), _npcSpawn.getNpc().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "TvTEventEngine: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		return true;
	}
	
	private static int highestLevelPcInstanceOf(Map<Integer, Player> players)
	{
		int maxLevel = Integer.MIN_VALUE, maxLevelId = -1;
		for (Player player : players.values())
		{
			if (player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}
	
	/**
	 * Starts the TvTEvent fight
	 * 1. Set state EventState.STARTING
	 * 2. Close doors specified in configs
	 * 3. Abort if not enought participants(return false)
	 * 4. Set state EventState.STARTED
	 * 5. Teleport all participants to team spot
	 * 
	 * @return boolean: true if success, otherwise false
	 */
	public static boolean startFight()
	{
		// Set state to STARTING
		setState(EventState.STARTING);
		
		// Randomize and balance team distribution
		Map<Integer, Player> allParticipants = new HashMap<>();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		
		int balance[] =
		{
			0,
			0
		}, 
		priority = 0, highestLevelPlayerId;
		Player highestLevelPlayer;
		// XXX: allParticipants should be sorted by level instead of using highestLevelPcInstanceOf for every fetch
		while (!allParticipants.isEmpty())
		{
			// Priority team gets one player
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Exiting if no more players
			if (allParticipants.isEmpty())
			{
				break;
			}
			// The other team gets one player
			// XXX: Code not dry
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Recalculating priority
			priority = balance[0] > balance[1] ? 1 : 0;
		}
		
		// Check for enought participants
		if ((_teams[0].getParticipatedPlayerCount() < TvTConfig.TVT_EVENT_MIN_PLAYERS_IN_TEAMS) || (_teams[1].getParticipatedPlayerCount() < TvTConfig.TVT_EVENT_MIN_PLAYERS_IN_TEAMS))
		{
			// Set state INACTIVE
			setState(EventState.INACTIVE);
			// Cleanup of teams
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			// Unspawn the event NPC
			unSpawnNpc();
			return false;
		}
		
		// Closes all doors specified in configs for tvt
		if(DMConfig.CLOSE_DOORS)
		closeDoors(TvTConfig.TVT_DOORS_IDS_TO_CLOSE);
		// Set state STARTED
		
		
		// Iterate over all teams
		int teamNo=1;
		for (TvTEventTeam team : _teams)
		{
			// Iterate over all participated player instances in this team
			for (Player playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					if(teamNo == 1)
						playerInstance.setTeam(TeamType.BLUE);
					else
						playerInstance.setTeam(TeamType.RED);
					
					playerInstance._originalNameInt = playerInstance.getAppearance().getNameColor();
					playerInstance.getAppearance().setNameColor(Integer.decode("0x" + team.getColor().substring(4,6) + team.getColor().substring(2,4) + team.getColor().substring(0,2)));
					
					if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("title") || TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pmtitle"))
					{
						playerInstance._originalTitle = playerInstance.getTitle();
//						playerInstance._originalName = String.valueOf(playerInstance.getAppearance().getNameColor());
//						playerInstance.broadcastUserInfo();// (ta kanei nania prin mpun alla dn fenete to color name gia teams )
						playerInstance.setTitle("Kills: " + playerInstance.getPointScore());
					}
					playerInstance.broadcastTitleInfo();
					
					// Teleporter implements Runnable and starts itself
					new TvTEventTeleporter(playerInstance, team.getCoordinates(), false, false);
					
					playerInstance.sendPacket(new ExShowScreenMessage("TvT Afk system is started, if you stay Afk you will be kicked!", 6000));
				}
			}
			teamNo++;
		}
		teamNo=0;
		
		setState(EventState.STARTED);
		
		
		return true;
	}
	
	/**
	 * Calculates the TvTEvent reward
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding
	 * 2. Wait till teams are not at a tie anymore
	 * 3. Set state EvcentState.REWARDING
	 * 4. Reward team with more points
	 * 5. Show win html to wining team participants
	 * 
	 * @return String: winning team name
	 */
	public static String calculateRewards()
	{
		if (_teams[0].getPoints() == _teams[1].getPoints())
		{
			// Check if one of the teams have no more players left
			if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
			{
				// set state to rewarding
				setState(EventState.REWARDING);
				// return here, the fight can't be completed
				return "Team vs Team: Event has ended. No team won due to inactivity!";
			}
			
			// Both teams have equals points
			sysMsgToAllParticipants("Event has ended, both teams have tied.");
			if (TvTConfig.TVT_REWARD_TEAM_TIE)
			{
				rewardTeam(_teams[0],true);
				rewardTeam(_teams[1],true);
				return "Team vs Team: Event has ended with both teams tying.";
			}
			return "Team vs Team: Event has ended with both teams tying.";
		}
		
		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);
		
		// Get team which has more points
		TvTEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		rewardTeam(team,false);
		return "Team vs Team: Event finish! Team " + team.getName() + " won with " + team.getPoints() + " kills!";
	}
	
	private static void rewardTeam(TvTEventTeam team,boolean tie)
	{
		// Iterate over all participated player instances of the winning team
		for (Player playerInstance : team.getParticipatedPlayers().values())
		{
			// Check for nullpointer
			if (playerInstance == null)
                continue;
			
            // Checks if the player scored points.
            if (TvTConfig.TVT_REWARD_PLAYER)
                if (!team.onScoredPlayer(playerInstance.getObjectId()))
                    continue;
			
			SystemMessage systemMessage = null;

			// Iterate over all tvt event rewards
			
			if(tie) {
				for (int[] reward : TvTConfig.TVT_EVENT_REWARDS)
				{
					if (playerInstance.isVip()){
						playerInstance.addItem("Team vs Team:", reward[0],  2 , null, true);
						playerInstance.sendMessage("Because you are VIP you got more event medals (Only for Tie matches)");
					}
					else{
						 playerInstance.addItem("Team vs Team:", reward[0], 1, null, true);
					}
				}				
			}
			else {
				for (int[] reward : TvTConfig.TVT_EVENT_REWARDS)
				{
//					PcInventory inv = playerInstance.getInventory();
					if (playerInstance.isVip()){
						/*ItemInstance item = */ playerInstance.addItem("Team vs Team:", reward[0], reward[1] /** CustomConfig.VIP_DROP_RATE*/ , null, true);
					}
					else{
						 playerInstance.addItem("Team vs Team:", reward[0], reward[1], null, true);
					}
				}
			}
			
			StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Reward.htm"));
			playerInstance.sendPacket(statusUpdate);
			playerInstance.sendPacket(npcHtmlMessage);
		}
		
		// Check for stackable item, non stackabe items need to be added one by one
//		if (ItemInstance().create(reward[0]).isStackable())
		
		
		/*					if(item.isStackable()) // Possible a temp item
							{
								//inv.addItem("Team vs Team:", reward[0], reward[1] * CustomConfig.VIP_DROP_RATE, playerInstance, playerInstance);

								if (reward[1] > 1)
								{
									systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
									systemMessage.addItemName(reward[0]);
									systemMessage.addItemNumber(reward[1]);
								}
								else
								{
									systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
									systemMessage.addItemName(reward[0]);
								}

								playerInstance.sendPacket(systemMessage);
							}
							else
							{
								for (int i = 0; i < reward[1]; ++i)
								{
									//inv.addItem("Team vs Team:", reward[0], 1 * CustomConfig.VIP_DROP_RATE, playerInstance, playerInstance);
									systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
									systemMessage.addItemName(reward[0]);
									playerInstance.sendPacket(systemMessage);
								}
							}
		*/
		
//		else
		
		 /*ItemInstance item =*/
	/*	if(item.isStackable()) // Possible a temp item
		//if (ItemData.getInstance().createDummyItem(reward[0]).isStackable())
		{
			//inv.addItem("Team vs Team:", reward[0], reward[1], playerInstance, playerInstance);

			if (reward[1] > 1)
			{
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				systemMessage.addItemName(reward[0]);
				systemMessage.addItemNumber(reward[1]);
			}
			else
			{
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				systemMessage.addItemName(reward[0]);
			}

			playerInstance.sendPacket(systemMessage);
		}
		else
		{
			for (int i = 0; i < reward[1]; ++i)
			{
				inv.addItem("Team vs Team:", reward[0], 1, playerInstance, playerInstance);
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				systemMessage.addItemName(reward[0]);
				playerInstance.sendPacket(systemMessage);
			}
		}*/
		
	}
	
	/**
	 * Stops the TvTEvent fight
	 * 1. Set state EventState.INACTIVATING
	 * 2. Remove tvt npc from world
	 * 3. Open doors specified in configs
	 * 4. Teleport all participants back to participation npc location
	 * 5. Teams cleaning
	 * 6. Set state EventState.INACTIVE
	 */
	public static void stopFight()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		// Unspawn event npc
		unSpawnNpc();
		// Opens all doors specified in configs for tvt
		
		if(DMConfig.CLOSE_DOORS)
		openDoors(TvTConfig.TVT_DOORS_IDS_TO_CLOSE);
		
		// Iterate over all teams
		for (TvTEventTeam team : _teams)
		{
			for (final Player playerInstance : team.getParticipatedPlayers().values())
			{
				// Check for nullpointer
				if (playerInstance != null)
				{
					new TvTEventTeleporter(playerInstance, TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
					
					//seset karma
					if(playerInstance.getKarma() > 0)
						playerInstance.setKarma(0);
					
					/*if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("title") || TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pmtitle"))
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								playerInstance.setTitle(playerInstance._originalTitle);
								//test
								playerInstance.getAppearance().setNameColor(playerInstance._originalNameInt);
								playerInstance.setTeam(TeamType.NONE);
//								playerInstance.getAppearance().setNameColor(Integer.decode("0x" + playerInstance._originalName.substring(4,6) + playerInstance._originalName.substring(2,4) + playerInstance._originalName.substring(0,2)));
								
//								playerInstance.broadcastTitleInfo();
								playerInstance.clearPoints();
							}
						}, TvTConfig.TVT_EVENT_START_LEAVE_TELEPORT_DELAY * 1000);
					}
					*/

					playerInstance.setTitle(playerInstance._originalTitle);
					//test
					playerInstance.getAppearance().setNameColor(playerInstance._originalNameInt);
					playerInstance.setTeam(TeamType.NONE);
//					playerInstance.getAppearance().setNameColor(Integer.decode("0x" + playerInstance._originalName.substring(4,6) + playerInstance._originalName.substring(2,4) + playerInstance._originalName.substring(0,2)));
						
//					playerInstance.broadcastTitleInfo();
					playerInstance.clearPoints();
					playerInstance.broadcastTitleInfo();
				}
			}
		}
		
		//clear it
		if(eventAntiAfk.getPlayers() != null)
			eventAntiAfk.getPlayers().clear();
		
		// Cleanup of teams
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		// Set state INACTIVE
		setState(EventState.INACTIVE);
	}
	
	/**
	 * Adds a player to a TvTEvent team
	 * 1. Calculate the id of the team in which the player should be added
	 * 2. Add the player to the calculated team
	 * 
	 * @param playerInstance as Player
	 * @return boolean: true if success, otherwise false
	 */
	public static synchronized boolean addParticipant(Player playerInstance)
	{
		// Check for nullpoitner
		if (playerInstance == null)
			return false;
		
		byte teamId = 0;
		
		// Check to which team the player should be added
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
			teamId = (byte) (Rnd.get(2));
		else
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		
		if(teamId == 1)
			return _teams[teamId].addPlayer(playerInstance);
		
		return _teams[teamId].addPlayer(playerInstance);
	}
	
	/**
	 * Removes a TvTEvent player from it's team
	 * 1. Get team id of the player
	 * 2. Remove player from it's team
	 * 
	 * @param playerObjectId
	 * @return boolean: true if success, otherwise false
	 */
	public static boolean removeParticipant(int playerObjectId)
	{
		// Get the teamId of the player
		byte teamId = getParticipantTeamId(playerObjectId);
		
		// Check if the player is participant
		if (teamId != -1)
		{
			// Remove the player from team
			_teams[teamId].removePlayer(playerObjectId);
			return true;
		}
		
		return false;
	}
	
	public static boolean payParticipationFee(Player activeChar)
	{
		int itemId = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[0];
		int itemNum = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[1];
		if (itemId == 0 || itemNum == 0)
			return true;
		
		if (activeChar.getInventory().getInventoryItemCount(itemId, -1) < itemNum)
			return false;
		
		return activeChar.destroyItemByItemId("TvT Participation Fee", itemId, itemNum, _lastNpcSpawn, true);
	}
	
	public static String getParticipationFee()
	{
		int itemId = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[0];
		int itemNum = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[1];
		
		if ((itemId == 0) || (itemNum == 0))
			return "-";
		
		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}
	
	/**
	 * Send a SystemMessage to all participated players
	 * 1. Send the message to all players of team number one
	 * 2. Send the message to all players of team number two
	 * 
	 * @param message as String
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_ALL, "TVT Event", message);
		
		for (Player playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendPacket(cs);
			}
		}
		
		for (Player playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendPacket(cs);
			}
		}
	}
	
	/**
	 * Close doors specified in configs
	 * @param doors
	 */
	private static void closeDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			Door doorInstance = DoorData.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.closeMe();
			}
		}
	}
	
	/**
	 * Open doors specified in configs
	 * @param doors
	 */
	private static void openDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			Door doorInstance = DoorData.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.openMe();
			}
		}
	}
	
	/**
	 * UnSpawns the TvTEvent npc
	 */
	private static void unSpawnNpc()
	{
	/*	if(_lastNpcSpawn !=null) {
			// Delete the npc
			_lastNpcSpawn.deleteMe();
			SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		}*/
		_lastNpcSpawn.deleteMe();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		_lastNpcSpawn.getSpawn().setRespawnState(false);
		
		// Stop respawning of the npc
//		_npcSpawn.stopRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}
	
	/**
	 * Called when a player logs in
	 * 
	 * @param playerInstance as Player
	 */
	public static void onLogin(Player playerInstance)
	{
		if ((playerInstance == null) || (!isStarting() && !isStarted()))
			return;
		
		byte teamId = getParticipantTeamId(playerInstance.getObjectId());
		
		if (teamId == -1)
			return;
		
		_teams[teamId].addPlayer(playerInstance);
		new TvTEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true, false);
	}
	
	/**
	 * Called when a player logs out
	 * 
	 * @param playerInstance as Player
	 */
	public static void onLogout(Player playerInstance)
	{
		if ((playerInstance != null) && (isStarting() || isStarted() || isParticipating()))
		{
			if (removeParticipant(playerInstance.getObjectId()))
			{
				playerInstance.teleportTo((TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101)) - 50, (TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101)) - 50, TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2], 0);
				playerInstance.setTeamEvent(0);
			}
			if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("title") || TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pmtitle"))
			{
				//test
				playerInstance.getAppearance().setNameColor(playerInstance._originalNameInt);
//				playerInstance.getAppearance().setNameColor(Integer.decode(("0x" + playerInstance._originalName.substring(4,6) + playerInstance._originalName.substring(2,4) + playerInstance._originalName.substring(0,2))));
				playerInstance.broadcastTitleInfo();
			}
			playerInstance.setTitle(playerInstance._originalTitle);
			playerInstance.setTeam(TeamType.NONE);
			playerInstance.broadcastTitleInfo();
		}
	}
	
	/**
	 * Called on every bypass by npc of type L2TvTEventNpc
	 * Needs synchronization cause of the max player check
	 * 
	 * @param command as String
	 * @param playerInstance as Player
	 */
	public static synchronized void onBypass(String command, Player playerInstance)
	{
		if (playerInstance == null || !isParticipating())
			return;
		
		final String htmContent;
		
		if (command.equals("tvt_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = playerInstance.getLevel();
			
			if (playerInstance.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "CursedWeaponEquipped.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}	
			else if(playerInstance.isSupportClass(playerInstance.getActiveClass(),true)) {
				playerInstance.sendMessage("Your class is not allowed in this event.");
				return ;
			}
			else if ( playerInstance.isRegisteredInTournament())
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Tournament.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (OlympiadManager.getInstance().isRegistered(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Olympiad.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (playerInstance.getKarma() > 0)
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Karma.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (TvTConfig.DISABLE_ID_CLASSES.contains(playerInstance.getClassId().getId()))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Class.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if ((playerLevel < TvTConfig.TVT_EVENT_MIN_LVL) || (playerLevel > TvTConfig.TVT_EVENT_MAX_LVL))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%min%", String.valueOf(TvTConfig.TVT_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%max%", String.valueOf(TvTConfig.TVT_EVENT_MAX_LVL));
				}
			}
			else if ((_teams[0].getParticipatedPlayerCount() == TvTConfig.TVT_EVENT_MAX_PLAYERS_IN_TEAMS) && (_teams[1].getParticipatedPlayerCount() == TvTConfig.TVT_EVENT_MAX_PLAYERS_IN_TEAMS))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "TeamsFull.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(TvTConfig.TVT_EVENT_MAX_PLAYERS_IN_TEAMS));
				}
			}
            else if (TvTConfig.TVT_EVENT_MULTIBOX_PROTECTION_ENABLE && onMultiBoxRestriction(playerInstance))
            {
                htmContent = HtmCache.getInstance().getHtm(htmlPath + "MultiBox.htm");
                if (htmContent != null)
                {
                    npcHtmlMessage.setHtml(htmContent);
                    npcHtmlMessage.replace("%maxbox%", String.valueOf(TvTConfig.TVT_EVENT_NUMBER_BOX_REGISTER));
                }
            }
			else if (!payParticipationFee(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "ParticipationFee.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%fee%", getParticipationFee());
				}
			}
			else if (addParticipant(playerInstance))
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Registered.htm"));
			else
				return;
			
			playerInstance.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("tvt_event_remove_participation"))
		{
			removeParticipant(playerInstance.getObjectId());
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Unregistered.htm"));
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	/**
	 * Called on every onAction in L2PcIstance
	 * 
	 * @param playerInstance
	 * @param targetedPlayerObjectId
	 * @return boolean: true if player is allowed to target, otherwise false
	 */
	public static boolean onAction(Player playerInstance, int targetedPlayerObjectId)
	{
		if (playerInstance == null || !isStarted())
			return true;
		
		if (playerInstance.isGM())
			return true;
		
		byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);
		
		if (((playerTeamId != -1) && (targetedPlayerTeamId == -1)) || ((playerTeamId == -1) && (targetedPlayerTeamId != -1)))
		{
			playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if ((playerTeamId != -1) && (targetedPlayerTeamId != -1) && (playerTeamId == targetedPlayerTeamId) && (playerInstance.getObjectId() != targetedPlayerObjectId) && !TvTConfig.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED)
		{
			playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every scroll use
	 * 
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to use scroll, otherwise false
	 */
	public static boolean onScrollUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !TvTConfig.TVT_EVENT_SCROLL_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every potion use
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to use potions, otherwise false
	 */
	public static boolean onPotionUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !TvTConfig.TVT_EVENT_POTIONS_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every escape use(thanks to nbd)
	 * @param playerObjectId
	 * @return boolean: true if player is not in tvt event, otherwise false
	 */
	public static boolean onEscapeUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId))
			return false;
		
		return true;
	}
	
	/**
	 * Called on every summon item use
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to summon by item, otherwise false
	 */
	public static boolean onItemSummon(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !TvTConfig.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Is called when a player is killed
	 * 
	 * @param killerCharacter as Creature
	 * @param killedPlayerInstance as Player
	 */
	public static void onKill(Creature killerCharacter, Player killedPlayerInstance)
	{
		if (killedPlayerInstance == null || !isStarted())
			return;
		
		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());
		
		if (killedTeamId == -1)
			return;
		
		new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);
		
		if (killerCharacter == null)
			return;
		
		Player killerPlayerInstance = null;
		
		if ((killerCharacter instanceof Pet) || (killerCharacter instanceof Summon))
		{
			killerPlayerInstance = ((Summon) killerCharacter).getOwner();
			
			if (killerPlayerInstance == null)
			{
				return;
			}
		}
		else if (killerCharacter instanceof Player)
		{
			killerPlayerInstance = (Player) killerCharacter;
		}
		else
		{
			return;
		}
		
		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());
		if (killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId)
		{
			TvTEventTeam killerTeam = _teams[killerTeamId];
			
			killerTeam.increasePoints();
			killerTeam.increasePoints(killerPlayerInstance.getObjectId());
			killerPlayerInstance.increasePvpKills();
			
			if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pm"))
			{
				sysMsgToAllParticipants(killerPlayerInstance.getName() + " Hunted Player " + killedPlayerInstance.getName() + "!");
			}
			else if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("title"))
			{
				killerPlayerInstance.increasePointScore();
				killerPlayerInstance.setTitle("Kills: " + killerPlayerInstance.getPointScore());
				killerPlayerInstance.broadcastTitleInfo();
			}
			else if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pmtitle"))
			{
				sysMsgToAllParticipants(killerPlayerInstance.getName() + " Hunted Player " + killedPlayerInstance.getName() + "!");
				killerPlayerInstance.increasePointScore();
				killerPlayerInstance.setTitle("Kills: " + killerPlayerInstance.getPointScore());
				killerPlayerInstance.broadcastTitleInfo();
			}
		}
	}
	
	/**
	 * Called on Appearing packet received (player finished teleporting)
	 * @param playerInstance
	 */
	public static void onTeleported(Player playerInstance)
	{
		if (!isStarted() || (playerInstance == null) || !isPlayerParticipant(playerInstance.getObjectId()))
			return;
		
		if (playerInstance.isMageClass())
		{
			if (TvTConfig.TVT_EVENT_MAGE_BUFFS != null && !TvTConfig.TVT_EVENT_MAGE_BUFFS.isEmpty())
			{
				for (int i : TvTConfig.TVT_EVENT_MAGE_BUFFS.keySet())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, TvTConfig.TVT_EVENT_MAGE_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(playerInstance, playerInstance);
				}
			}
		}
		else
		{
			if (TvTConfig.TVT_EVENT_FIGHTER_BUFFS != null && !TvTConfig.TVT_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for (int i : TvTConfig.TVT_EVENT_FIGHTER_BUFFS.keySet())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, TvTConfig.TVT_EVENT_FIGHTER_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(playerInstance, playerInstance);
				}
			}
		}
		removeParty(playerInstance);
		
		//AFK started
//		TvTAntiAfk.getInstance();
		eventAntiAfk.getInstance();
		
	}
	
	/**
	 * @param source
	 * @param target
	 * @param skill
	 * @return true if player valid for skill
	 */
	public static final boolean checkForTvTSkill(Player source, Player target, L2Skill skill)
	{
		if (!isStarted())
			return true;

		// TvT is started
		final int sourcePlayerId = source.getObjectId();
		final int targetPlayerId = target.getObjectId();
		final boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		final boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);
		
		// both players not participating
		if (!isSourceParticipant && !isTargetParticipant)
			return true;

		// one player not participating
		if (!(isSourceParticipant && isTargetParticipant))
			return false;

		// players in the different teams ?
		if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if (!skill.isOffensive())
				return false;
		}
		return true;
	}
	
	/**
	 * Sets the TvTEvent state
	 * 
	 * @param state as EventState
	 */
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}
	
	/**
	 * Is TvTEvent inactive?
	 * 
	 * @return boolean: true if event is inactive(waiting for next event cycle), otherwise false
	 */
	public static boolean isInactive()
	{
		boolean isInactive;
		
		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}
		
		return isInactive;
	}
	
	/**
	 * Is TvTEvent in inactivating?
	 * 
	 * @return boolean: true if event is in inactivating progress, otherwise false
	 */
	public static boolean isInactivating()
	{
		boolean isInactivating;
		
		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}
		
		return isInactivating;
	}
	
	/**
	 * Is TvTEvent in participation?
	 * 
	 * @return boolean: true if event is in participation progress, otherwise false
	 */
	public static boolean isParticipating()
	{
		boolean isParticipating;
		
		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}
		
		return isParticipating;
	}
	
	/**
	 * Is TvTEvent starting?
	 * 
	 * @return boolean: true if event is starting up(setting up fighting spot, teleport players etc.), otherwise false
	 */
	public static boolean isStarting()
	{
		boolean isStarting;
		
		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}
		
		return isStarting;
	}
	
	/**
	 * Is TvTEvent started?
	 * 
	 * @return boolean: true if event is started, otherwise false
	 */
	public static boolean isStarted()
	{
		boolean isStarted;
		
		synchronized (_state)
		{
			isStarted = _state == EventState.STARTED;
		}
		
		return isStarted;
	}
	
	/**
	 * Is TvTEvent rewarding?
	 * 
	 * @return boolean: true if event is currently rewarding, otherwise false
	 */
	public static boolean isRewarding()
	{
		boolean isRewarding;
		
		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}
		
		return isRewarding;
	}
	
	/**
	 * Returns the team id of a player, if player is not participant it returns -1
	 * @param playerObjectId
	 * @return byte: team name of the given playerName, if not in event -1
	 */
	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : (_teams[1].containsPlayer(playerObjectId) ? 1 : -1));
	}
	
	/**
	 * Returns the team of a player, if player is not participant it returns null
	 * @param playerObjectId
	 * @return TvTEventTeam: team of the given playerObjectId, if not in event null
	 */
	public static TvTEventTeam getParticipantTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[0] : (_teams[1].containsPlayer(playerObjectId) ? _teams[1] : null));
	}
	
	/**
	 * Returns the enemy team of a player, if player is not participant it returns null
	 * @param playerObjectId
	 * @return TvTEventTeam: enemy team of the given playerObjectId, if not in event null
	 */
	public static TvTEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[1] : (_teams[1].containsPlayer(playerObjectId) ? _teams[0] : null));
	}
	
	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null
	 * @param playerObjectId
	 * @return int[]: coordinates of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getCoordinates() : (_teams[1].containsPlayer(playerObjectId) ? _teams[1].getCoordinates() : null);
	}
	
	/**
	 * Is given player participant of the event?
	 * @param playerObjectId
	 * @return boolean: true if player is participant, ohterwise false
	 */
	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if (!isParticipating() && !isStarting() && !isStarted())
			return false;
		
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	
	public static boolean isPlayerParticipantCustom(int playerObjectId)
	{
		if (isParticipating() && !isStarting() && !isStarted() )
			return false;
		
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	public static boolean isPlayerParticipantCustom1(int playerObjectId)
	{
		if (isStarted() )
			return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
			
		return false;
	}
	public static boolean areTeammates(Player player, Player target)
	{
//		if (isParticipating() && !isStarting() && !isStarted() )
//			return false;
		if ( isStarted()) {
			
//			if (_teams.length < 2)
//				return false;
			
			if (_teams[0].containsPlayer(player.getObjectId()) == _teams[0].containsPlayer(target.getObjectId()))
				return true;
			
			if (_teams[1].containsPlayer(player.getObjectId()) == _teams[1].containsPlayer(target.getObjectId()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns participated player count
	 * 
	 * @return int: amount of players registered in the event
	 */
	public static int getParticipatedPlayersCount()
	{
		if (!isParticipating() && !isStarting() && !isStarted())
			return 0;
		
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	/**
	 * Returns teams names
	 * 
	 * @return String[]: names of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static String[] getTeamNames()
	{
		return new String[]
		{
			_teams[0].getName(),
			_teams[1].getName()
		};
	}
	
	/**
	 * Returns player count of both teams
	 * 
	 * @return int[]: player count of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]
		{
			_teams[0].getParticipatedPlayerCount(), _teams[1].getParticipatedPlayerCount()
		};
	}
	
	/**
	 * Returns points count of both teams
	 * @return int[]: points of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getTeamsPoints()
	{
		return new int[]
		{
			_teams[0].getPoints(), _teams[1].getPoints()
		};
	}
	
	public static void removeParty(Player activeChar)
	{
		if (activeChar.getParty() != null)
		{
			Party party = activeChar.getParty();
			party.removePartyMember(activeChar, MessageType.LEFT);
		}
	}
	
    public static List<Player> allParticipants()
    {
        List<Player> players = new ArrayList<>();
        players.addAll(_teams[0].getParticipatedPlayers().values());
        players.addAll(_teams[1].getParticipatedPlayers().values());
        return players;
    }
   
  /*  public static boolean onMultiBoxRestriction(Player activeChar)
    {
    	return IPManager.getInstance().validBox(activeChar, TvTConfig.TVT_EVENT_NUMBER_BOX_REGISTER, allParticipants(), false);
    }*/
    public static boolean  onMultiBoxRestriction(Player player) {
    	for(Player p : allParticipants()) {
    		if(p.gethwid().equals(player.gethwid()))
    			return true;
    	}

    	return false;
    }
    
    
	public static class TvTAntiAfk //implements Runnable
	{
		// Delay between location checks , Default 60000 ms (1 minute)
		private final int CheckDelay = 20000;

		static ArrayList<String> TvTPlayerList = new ArrayList<>();
		private static String[] Splitter;
		private static int xx, yy, zz, SameLoc;
		static Player _player;
		
		
		TvTAntiAfk()
		{
			_log.info("TvTAntiAfk: Auto-Kick AFK in TVT System initiated.");
			//ThreadPool.schedule(new AntiAfk(), CheckDelay);
			//ThreadPool.scheduleAtFixedRate(new AntiAfk(), CheckDelay, CheckDelay);
			ThreadPool.schedule(new AntiAfk(), CheckDelay);
		}

		private class AntiAfk implements Runnable
		{
			AntiAfk()
			{
			}
			@Override
			public void run()
			{
				if (TvTEvent.isStarted())
				{
					synchronized (TvTEvent._teams)
					{
						// Iterate over all teams
						for (TvTEventTeam team : TvTEvent._teams)
						{
							synchronized (team.getParticipatedPlayers().values()) {
								ConcurrentHashMap<Integer, Player> nea = new ConcurrentHashMap<>();
								
							// Iterate over all participated player instances in this team
								for (Player playerInstance : team.getParticipatedPlayers().values())
								{
									if (playerInstance != null && playerInstance.isOnline() && !playerInstance.isDead() /*&& !playerInstance.isPhantom() */ && !playerInstance.isGM() && !playerInstance.isImmobilized() && !playerInstance.isParalyzed())
									{
										_player = playerInstance;
										AddTvTSpawnInfo(playerInstance.getName(),playerInstance.getX(),playerInstance.getY(),playerInstance.getZ());
										ThreadPool.schedule(new AntiAfk(), CheckDelay);
									}
								}
							}
							
						}
					}
				}
				else
				{
					TvTPlayerList.clear();
				}
			}
		}
		
		static void AddTvTSpawnInfo(String name, int _x, int _y, int _z)
		{
			if (!CheckTvTSpawnInfo(name))
			{
				String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
				TvTPlayerList.add(temp);
			}
			else
			{
				Object[] elements = TvTPlayerList.toArray();
				for(int i=0; i < elements.length ; i++)
				{
					Splitter = ((String) elements[i]).split(":");
					String nameVal = Splitter[0];
					if (name.equals(nameVal))
					{
						GetTvTSpawnInfo(name);
						if (_x == xx && _y == yy && _z == zz && _player.isAttackingNow() == false && _player.isCastingNow() == false && _player.isOnline() == true && _player.isParalyzed() == false)
						{
							++SameLoc;
							if (SameLoc >= 4)//Kick after 4 same x/y/z, location checks
							{
								//kick here
								TvTPlayerList.remove(i);
								onLogout(_player);
								KickedMsg(_player);
								return;
							}
							TvTPlayerList.remove(i);
							String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":" + SameLoc;
							TvTPlayerList.add(temp);
							return;
						}
						TvTPlayerList.remove(i);
						String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
						TvTPlayerList.add(temp);
					}
				}
			}
		}
		
		private static boolean CheckTvTSpawnInfo(String name)
		{
			Object[] elements = TvTPlayerList.toArray();
			for(int i=0; i < elements.length ; i++)
			{
				Splitter = ((String) elements[i]).split(":");
				String nameVal = Splitter[0];
				if (name.equals(nameVal))
					return true;
			}
			return false;
		}

		private static void GetTvTSpawnInfo(String name)
		{
			Object[] elements = TvTPlayerList.toArray();
			for(int i=0; i < elements.length ; i++)
			{
				Splitter = ((String) elements[i]).split(":");
				String nameVal = Splitter[0];
				if (name.equals(nameVal))
				{
					xx = Integer.parseInt(Splitter[1]);
					yy = Integer.parseInt(Splitter[2]);
					zz = Integer.parseInt(Splitter[3]);
					SameLoc = Integer.parseInt(Splitter[4]);
				}
			}
		}
		
	    private static void KickedMsg(Player playerInstance)
	    {
	    	playerInstance.sendPacket(new ExShowScreenMessage("You're kicked out of the TvT by staying afk!", 6000));
	    }
	    
		public static TvTAntiAfk getInstance()
		{
			return SingletonHolder._instance;
		}
		
		private static class SingletonHolder
		{
			protected static final TvTAntiAfk _instance = new TvTAntiAfk();
		}
	}
}