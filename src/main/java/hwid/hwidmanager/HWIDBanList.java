package hwid.hwidmanager;

public class HWIDBanList
{
	private final int _id;
	private String HWID;
	private String name;
	
	public HWIDBanList(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public String getHWID()
	{
		return HWID;
	}

	public void setHWIDBan(String hwid1)
	{
		HWID = hwid1;
	}
	
	public void setName(String name1){
		name = name1;
	}
	public String getName()
	{
		return name;
	}
	
}