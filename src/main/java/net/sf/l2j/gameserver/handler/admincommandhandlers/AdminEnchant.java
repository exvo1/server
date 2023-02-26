package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.Shortcut;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * This class handles following admin commands: - enchant_armor
 */
public class AdminEnchant implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_seteh", // 6
		"admin_setec", // 10
		"admin_seteg", // 9
		"admin_setel", // 11
		"admin_seteb", // 12
		"admin_setew", // 7
		"admin_setes", // 8
		"admin_setle", // 1
		"admin_setre", // 2
		"admin_setlf", // 4
		"admin_setrf", // 5
		"admin_seten", // 3
		"admin_setun", // 0
		"admin_setba", // 13
		"admin_enchant",
		"admin_setaugment"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_enchant"))
			showMainPage(activeChar);
		else
		{
			int armorType = -1;
			
			if (command.startsWith("admin_seteh"))
				armorType = Inventory.PAPERDOLL_HEAD;
			else if (command.startsWith("admin_setec"))
				armorType = Inventory.PAPERDOLL_CHEST;
			else if (command.startsWith("admin_seteg"))
				armorType = Inventory.PAPERDOLL_GLOVES;
			else if (command.startsWith("admin_seteb"))
				armorType = Inventory.PAPERDOLL_FEET;
			else if (command.startsWith("admin_setel"))
				armorType = Inventory.PAPERDOLL_LEGS;
			else if (command.startsWith("admin_setew"))
				armorType = Inventory.PAPERDOLL_RHAND;
			else if (command.startsWith("admin_setes"))
				armorType = Inventory.PAPERDOLL_LHAND;
			else if (command.startsWith("admin_setle"))
				armorType = Inventory.PAPERDOLL_LEAR;
			else if (command.startsWith("admin_setre"))
				armorType = Inventory.PAPERDOLL_REAR;
			else if (command.startsWith("admin_setlf"))
				armorType = Inventory.PAPERDOLL_LFINGER;
			else if (command.startsWith("admin_setrf"))
				armorType = Inventory.PAPERDOLL_RFINGER;
			else if (command.startsWith("admin_seten"))
				armorType = Inventory.PAPERDOLL_NECK;
			else if (command.startsWith("admin_setun"))
				armorType = Inventory.PAPERDOLL_UNDER;
			else if (command.startsWith("admin_setba"))
				armorType = Inventory.PAPERDOLL_BACK;
			else if (command.startsWith("admin_setaug")) {
				armorType = -111;
			}

			if (armorType != -1)
			{
				try
				{
					int ench = Integer.parseInt(command.substring(12));
					
					// check value
					if (ench < 0 || ench > 65535)
						activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
					else
						setEnchant(activeChar, ench, armorType);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Please specify a new enchant value.");
				}
			}
			if (armorType == -111) {
				try {
					String[] cmd = command.split(" ");
					adminSetVariation(activeChar, cmd);
				} catch (Exception e) {
					activeChar.sendMessage("Please specify a new augment value.");
				}
			}
			// show the enchant menu after an action
			showMainPage(activeChar);
		}
		
		return true;
	}

	private void adminSetVariation(Player activeChar, String[] wordList) {
		WorldObject target = activeChar.getTarget();
		if (target != null && target instanceof Player && activeChar == target) {
			Player player = (Player)target;
			if (wordList.length == 3) {
				int id = Integer.parseInt(wordList[1]);
				int level = Integer.parseInt(wordList[2]);
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				if (skill != null) {
					processAugment(activeChar, player, skill.getId(), skill.getLevel());
				} else {
					activeChar.sendMessage("Error: there is no such skill or variation.");
				}
			}
			return;
		}
		activeChar.sendPacket((L2GameServerPacket) SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
	}

	private void processAugment(Player activeChar, Player player, int skillId, int skillLvl) {
		if (player == null || player.isTeleporting())
			return;
		ItemInstance targetItem = player.getInventory().getPaperdollItem(7);
		if (targetItem == null) {
			activeChar.sendMessage("setAugment: targetItem == null");
			return;
		}
		if (targetItem.isAugmented()) {
			activeChar.sendPacket((L2GameServerPacket)SystemMessage.getSystemMessage(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN));
			activeChar.sendPacket((L2GameServerPacket)ActionFailed.STATIC_PACKET);
			return;
		}
		int augId = 1063590051;
		L2Augmentation augmentation = new L2Augmentation(augId, skillId, skillLvl);
		boolean equipped;
		if (equipped = targetItem.isEquipped())
			player.disarmWeapons();
		augmentation.applyBonus(player);
		targetItem.setAugmentation(augmentation);
		if (equipped)
			player.getInventory().equipItem(targetItem);
		try {
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			try {
				PreparedStatement statement = con.prepareStatement("REPLACE INTO augmentations VALUES(?,?,?,?)");
				try {
					statement.setInt(1, targetItem.getObjectId());
					statement.setInt(2, augId);
					statement.setInt(3, skillId);
					statement.setInt(4, skillLvl);
					InventoryUpdate inventoryUpdate = new InventoryUpdate();
					player.sendPacket((L2GameServerPacket)inventoryUpdate);
					statement.execute();
					statement.close();
					if (statement != null)
						statement.close();
				} catch (Throwable throwable) {
					if (statement != null)
						try {
							statement.close();
						} catch (Throwable throwable1) {
							throwable.addSuppressed(throwable1);
						}
					throw throwable;
				}
				if (con != null)
					con.close();
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		player.sendMessage("Successfully To Add " + SkillTable.getInstance().getInfo(skillId, skillLvl).getName() + ".");
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		player.sendPacket((L2GameServerPacket)iu);
		Arrays.<Shortcut>stream(player.getShortcutList().getShortcuts()).filter(sc -> (sc.getId() == targetItem.getObjectId() && sc.getType() == ShortcutType.ITEM)).forEach(sc -> player.sendPacket((L2GameServerPacket)new ShortCutRegister(sc)));
	}

	private static void setEnchant(Player activeChar, int ench, int armorType)
	{
		WorldObject target = activeChar.getTarget();
		if (!(target instanceof Player))
			target = activeChar;
		
		final Player player = (Player) target;
		
		final ItemInstance item = player.getInventory().getPaperdollItem(armorType);
		if (item != null && item.getLocationSlot() == armorType)
		{
			final Item it = item.getItem();
			final int oldEnchant = item.getEnchantLevel();
			
			item.setEnchantLevel(ench);
			item.updateDatabase();
			
			// If item is equipped, verify the skill obtention/drop (+4 duals, +6 armorset).
			if (item.isEquipped())
			{
				final int currentEnchant = item.getEnchantLevel();
				
				// Skill bestowed by +4 duals.
				if (it instanceof Weapon)
				{
					// Old enchant was >= 4 and new is lower : we drop the skill.
					if (oldEnchant >= 4 && currentEnchant < 4)
					{
						final L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
						if (enchant4Skill != null)
						{
							player.removeSkill(enchant4Skill.getId(), false);
							player.sendSkillList();
						}
					}
					// Old enchant was < 4 and new is 4 or more : we add the skill.
					else if (oldEnchant < 4 && currentEnchant >= 4)
					{
						final L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
						if (enchant4Skill != null)
						{
							player.addSkill(enchant4Skill, false);
							player.sendSkillList();
						}
					}
				}
				// Add skill bestowed by +6 armorset.
				else if (it instanceof Armor)
				{
					// Old enchant was >= 6 and new is lower : we drop the skill.
					if (oldEnchant >= 6 && currentEnchant < 6)
					{
						// Checks if player is wearing a chest item
						final ItemInstance chestItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
						if (chestItem != null)
						{
							final ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
							if (armorSet != null)
							{
								final int skillId = armorSet.getEnchant6skillId();
								if (skillId > 0)
								{
									player.removeSkill(skillId, false);
									player.sendSkillList();
								}
							}
						}
					}
					// Old enchant was < 6 and new is 6 or more : we add the skill.
					else if (oldEnchant < 6 && currentEnchant >= 6)
					{
						// Checks if player is wearing a chest item
						final ItemInstance chestItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
						if (chestItem != null)
						{
							final ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
							if (armorSet != null && armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
							{
								final int skillId = armorSet.getEnchant6skillId();
								if (skillId > 0)
								{
									final L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
									if (skill != null)
									{
										player.addSkill(skill, false);
										player.sendSkillList();
									}
								}
							}
						}
					}
				}
			}
			
			player.sendPacket(new ItemList(player, false));
			player.broadcastUserInfo();
			
			activeChar.sendMessage("Changed enchantment of " + player.getName() + "'s " + it.getName() + " from " + oldEnchant + " to " + ench + ".");
			if (player != activeChar)
				player.sendMessage("A GM has changed the enchantment of your " + it.getName() + " from " + oldEnchant + " to " + ench + ".");
		}
	}
	
	private static void showMainPage(Player activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "enchant.htm");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}