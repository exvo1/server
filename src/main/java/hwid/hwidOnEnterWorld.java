package hwid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author @Icathialord
 *
 */

public class hwidOnEnterWorld
{
	static
	{
		new File("log/hwid/").mkdirs();
	}
	public void init(Player player){
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		FileWriter fstream ;
		try
		{
			String fname = "log/hwid/" + player.getName() + ".txt";
			File file = new File(fname);
			if(!file.exists()){
				file.createNewFile();
				fstream = new FileWriter(fname);
			}
			else
				fstream = new FileWriter(fname, true); //true tells to append data.
			
			
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("("+ formatter.format(calendar.getTime()) + ")" + "[Player: " + player.getName() + " IP: " + player.getClient().getConnection().getInetAddress().getHostAddress() + " HWID: " + player.gethwid() + "]\n-------------------------------------------------\n");

			/*FileWriter fstream = new FileWriter(fname);//"log/hwid/hwid.txt", true); //true tells to append data.
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("("+ formatter.format(calendar.getTime()) + ")" + "[Player: " + player.getName() + " IP: " + player.getClient().getConnection().getInetAddress().getHostAddress() + " HWID: " + player.gethwid() + "]\n-------------------------------------------------\n");
			*/
			
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//player.sendMessage("Something went wrong try again.");
		}
	}
	
	private static class SingletonHolder
	{
		protected static final hwidOnEnterWorld _instance = new hwidOnEnterWorld();
	}
	public static hwidOnEnterWorld getInstance()
	{
		return SingletonHolder._instance;
	}
}
