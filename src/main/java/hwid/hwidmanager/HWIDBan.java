package hwid.hwidmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;

public class HWIDBan
{
	protected static Logger _log = Logger.getLogger(HWIDBan.class.getName());
	private static HWIDBan _instance;
	private static Map<Integer, HWIDBanList> _lists;

	public HWIDBan()
	{
		_lists = new HashMap<>();
		load();
		System.out.println("- HWID Ban: Loaded " + _lists.size() + " HWIDs");
	}

	public static HWIDBan getInstance()
	{
		if (_instance == null)
		{
			_instance = new HWIDBan();
		}
		return _instance;
	}

	private static void load()
	{
		String name = "";
		String HWID = "";
		int counterHWIDBan = 0;
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			Throwable localThrowable2 = null;
			try
			{
				PreparedStatement statement = con.prepareStatement("SELECT * FROM hwid_bans");
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					name = rset.getString("char_name");
					HWID = rset.getString("hwid");
					HWIDBanList hb = new HWIDBanList(counterHWIDBan);
					hb.setHWIDBan(HWID);
					_lists.put(Integer.valueOf(counterHWIDBan), hb);
					counterHWIDBan++;
				}
			}
			catch (Throwable localThrowable1)
			{
				localThrowable2 = localThrowable1;
				throw localThrowable1;
			}
			finally
			{
				if (con != null)
				{
					if (localThrowable2 != null)
					{
						try
						{
							con.close();
						}
						catch (Throwable x2)
						{
							localThrowable2.addSuppressed(x2);
						}
					}
					else
					{
						con.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void reload()
	{
		_instance = new HWIDBan();
	}

	public boolean checkFullHWIDBanned(GameClient client)
	{
		if (_lists.size() == 0)
		{
			return false;
		}
		for (int i = 0; i < _lists.size(); i++)
		{
			if (_lists.get(Integer.valueOf(i)).getHWID().equals(client.getHWID()))
			{
				return true;
			}
		}
		return false;
	}

	public static int getCountHWIDBan()
	{
		return _lists.size();
	}

	public static void addHWIDBan(GameClient client,String name)
	{
		String HWID = client.getHWID();
		int counterHwidBan = _lists.size();
		HWIDBanList hb = new HWIDBanList(counterHwidBan);
		hb.setHWIDBan(HWID);
		hb.setName(name);
		_lists.put(Integer.valueOf(counterHwidBan), hb);
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			Throwable localThrowable2 = null;
			try
			{
				PreparedStatement statement = con.prepareStatement("INSERT INTO hwid_bans SET char_name=?,hwid=?");
				statement.setString(1, name);
				statement.setString(2, HWID);
				statement.execute();
			}
			catch (Throwable localThrowable1)
			{
				localThrowable2 = localThrowable1;
				throw localThrowable1;
			}
			finally
			{
				if (con != null)
				{
					if (localThrowable2 != null)
					{
						try
						{
							con.close();
						}
						catch (Throwable x2)
						{
							localThrowable2.addSuppressed(x2);
						}
					}
					else
					{
						con.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void delHWIDBan(Player p)
	{

		try(Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stm = con.prepareStatement("SELECT lasthwid FROM characters WHERE account_name=?");
			stm.setString(1, p.getAccountName());
			ResultSet rset = stm.executeQuery();
			String lasthwid = "";
			if (rset.next())
				lasthwid = rset.getString("lasthwid");
			
			
				PreparedStatement stm2 = con.prepareStatement("DELETE FROM hwid_bans WHERE hwid=?");
				stm2.setString(1, lasthwid);
				stm2.execute();
			
				_log.info("Deleted HWID BAN: " + p.getName() + " Acc name: " + p.getAccountName());
				stm2.close();
				rset.close();
				stm.close();
				reload();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}