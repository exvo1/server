package Customs.BypassHandlers;


/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.Config;

/**
 * 
 * @author Anarchy
 */
public class BypassHandler
{
    private static Logger _log = Logger.getLogger(BypassHandler.class.getName());
    private final Map<Integer, IBypassHandler> _datatable = new HashMap<>();

    public static BypassHandler getInstance()
    {
        return SingletonHolder._instance;
    }

    private BypassHandler()
    {

    	registerBypassHandler(new DungeonBypasses());

    }

    public void registerBypassHandler(IBypassHandler handler)
    {
        String[] ids = handler.getBypassHandlersList();
        for (int i = 0; i < ids.length; i++)
        {
            if (Config.DEVELOPER)
                _log.fine("Adding handler for command " + ids[i]);
            _datatable.put(ids[i].hashCode(), handler);
        }
    }

    public IBypassHandler getBypassHandler(String bypass)
    {
        String command = bypass;

        if (bypass.indexOf(" ") != -1)
            command = bypass.substring(0, bypass.indexOf(" "));

        if (Config.DEVELOPER)
            _log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));
        return _datatable.get(command.hashCode());
    }

    public int size()
    {
        return _datatable.size();
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder
    {
        protected static final BypassHandler _instance = new BypassHandler();
    }
}