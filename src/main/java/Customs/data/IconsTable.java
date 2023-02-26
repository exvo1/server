package Customs.data;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.commons.data.xml.IXmlReader;

import net.sf.l2j.gameserver.GameServer;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class IconsTable implements IXmlReader
{
  public static final Map<Integer, String> Icons = new HashMap<>();
  private static int count;
  private static long t0;
  private static double t;
  private static final Logger _log = Logger.getLogger(GameServer.class.getName());
  
  public void reload()
  {
    Icons.clear();
    load();
  }
  
  public IconsTable() {
	  load();
  }
  
  @Override
  public void load() {
      parseFile("./data/xml/Icons.xml");
   //   LOGGER.info("Loaded {} Icons.", Icons.size());
  }
  
  @Override
  public void parseDocument(Document doc, Path path)
  {
    count = 0;
    t0 = System.currentTimeMillis();
    try
    {
      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if (d.getNodeName().equalsIgnoreCase("icon"))
            {
              count += 1;
              NamedNodeMap attrs = d.getAttributes();
              Node att = attrs.getNamedItem("Id");
              Node att2 = attrs.getNamedItem("value");
              Icons.put(Integer.valueOf(att.getNodeValue()), String.valueOf(att2.getNodeValue()));
            }
          }
        }
      }
      t = System.currentTimeMillis() - t0;
      _log.config("IconsTable: Succesfully loaded " + count + " icons, in " + t + " Milliseconds.");
    }
    catch (Exception e)
    {
      _log.config("IconsTable: Failed loading IconsTable. Possible error: " + e.getMessage());
    }
  }
  
  public static String getIcon(int id)
  {
    if (Icons.get(Integer.valueOf(id)) == null)
    {
      //_log.config("IconsTable: Invalid Icon request: " + id + ", or it doesn't exist, Ignoring ...");
      return "null";
    }
    return Icons.get(Integer.valueOf(id));
  }
  
  public static final IconsTable getInstance()
  {
	  return SingletonHolder._instance;
  }
  
  private static class SingletonHolder
  {
    protected static final IconsTable _instance = new IconsTable();
  }
}


/*
public class IconsTable
{
    public static final Map<Integer, String> Icons = new HashMap<>();
    private static int count;
    private static long t0;
    private static double t;

    private static final Logger _log = Logger.getLogger(GameServer.class.getName());

    public void reload()
    {
        Icons.clear();
        parseData();
    }

    public static void parseData()
    {
        count=0;
        t0 = System.currentTimeMillis();
        try
        {
            File f = new File("./data/xml/icons.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);

            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
            {
                if ("list".equalsIgnoreCase(n.getNodeName()))
                {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                    {
                        if (d.getNodeName().equalsIgnoreCase("icon"))
                        {
                            count++;
                            NamedNodeMap attrs = d.getAttributes();
                            Node att = attrs.getNamedItem("Id");
                            Node att2 = attrs.getNamedItem("value");
                            Icons.put(Integer.valueOf(att.getNodeValue()), String.valueOf(att2.getNodeValue()));
                        }
                    }
                }
            }
            t = System.currentTimeMillis() - t0;
           _log.config("IconsTable: Succesfully loaded "+count+" icons, in "+t+" Milliseconds.");
        }
        catch (Exception e)
        {
            _log.config("IconsTable: Failed loading IconsTable. Possible error: "+e.getMessage());
        }
    }

    public static String getIcon(int id)
    {

        if (Icons.get(id)==null)
        {
            _log.config("IconsTable: Invalid Icon request: "+id+", or it doesn't exist, Ignoring ...");
            return "null";
        }
        return Icons.get(id);
    }

    public static final IconsTable getInstance()
    {
        parseData();
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final IconsTable _instance = new IconsTable();
    }
}
*/