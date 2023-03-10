package Customs.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.L2DatabaseFactory;


public class RankingPKManager
{
	private int _posId;

	private StringBuilder _playerList = new StringBuilder();

	public RankingPKManager()
	{
		loadFromDB();
	}

	public void loadFromDB()
	{
		Connection con = null;
		try
		{
			_posId = 0;
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name, online, pkkills FROM characters WHERE accesslevel=0 ORDER BY pkkills DESC LIMIT 10");

			ResultSet result = statement.executeQuery();

			while (result.next())
			{
			_posId = _posId + 1;

				addPlayerToList(_posId, result.getString("char_name"),
						result.getInt("online"),
						result.getInt("pkkills"));
			}

			result.close();
			statement.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				con.close();
			} catch (Exception e)
			{
				System.out.println(e);
			}
		}
	}

	public String loadPlayerPK()
	{
		return _playerList.toString();
	}

	private void addPlayerToList(int objId, String name, int online, int pkkills)
	{
		_playerList.append("<table width=270>");
		_playerList.append("<tr>");
		_playerList.append("<td></td>");
		_playerList.append("<td FIXWIDTH=22>" + objId + ".</td>");
		_playerList.append("<td FIXWIDTH=48>" + name + "</td>");
		_playerList.append("<td FIXWIDTH=34>" + pkkills + "</td>");
		if (online == 1)
		_playerList.append("<td FIXWIDTH=27><font color=00FF00>Online</font></td>");
		else
		_playerList.append("<td FIXWIDTH=27><font color=FF0000>Offline</font></td>");
		_playerList.append("</tr>");
		_playerList.append("</table>");
		_playerList.append("<img src=\"L2UI.Squaregray\" width=\"300\" height=\"1\">");
	}
}