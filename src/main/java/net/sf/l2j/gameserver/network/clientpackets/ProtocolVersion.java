package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.serverpackets.KeyPacket;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

import hwid.Hwid;
import hwid.HwidConfig;



public final class ProtocolVersion extends L2GameClientPacket
{
	private int _version;
	private byte _data[];
	private String _hwidHdd = "NoHWID-HD";
	private String _hwidMac = "NoHWID-MAC";
	private String _hwidCPU = "NoHWID-CPU";
	
	@Override
	protected void readImpl()
	{
		_version = readD();

		if (Hwid.isProtectionOn())
		{
			if (_buf.remaining() > 260)
			{
				_data = new byte[260];
				readB(_data);
				if (Hwid.isProtectionOn())
				{
					_hwidHdd = readS();
					_hwidMac = readS();
					_hwidCPU = readS();
				}
			}
		}
		else if (Hwid.isProtectionOn())
		{
			getClient().close(new KeyPacket(getClient().enableCrypt()));
		}
	}

	@Override
	protected void runImpl()
	{
		if (_version == -2)
			getClient().close((L2GameServerPacket) null);
		else if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
		{
			System.out.println("Client: " + getClient().toString() + " -> Protocol Revision: " + _version + " is invalid. Minimum and maximum allowed are: " + Config.MIN_PROTOCOL_REVISION + " and " + Config.MAX_PROTOCOL_REVISION + ". Closing connection.");
			getClient().close((L2GameServerPacket) null);
		}
		else
			getClient().sendPacket(new KeyPacket(getClient().enableCrypt()));

		getClient().setRevision(_version);

		if (Hwid.isProtectionOn())
		{
			if (_hwidHdd.equals("NoGuard") && _hwidMac.equals("NoGuard") && _hwidCPU.equals("NoGuard"))
			{
				System.out.println("HWID Status: No Client side dlls");
				getClient().close(new KeyPacket(getClient().enableCrypt()));
			}

			switch (HwidConfig.GET_CLIENT_HWID)
			{
				case 1:
					getClient().setHWID(_hwidHdd);
					break;
				case 2:
					getClient().setHWID(_hwidMac);
					break;
				case 3:
					getClient().setHWID(_hwidCPU);
					break;
			}
		}
	}
}
	

/*
public final class ProtocolVersion extends L2GameClientPacket
{
	private int _version;
	
	@Override
	protected void readImpl()
	{
		_version = readD();
	}
	
	@Override
	protected void runImpl()
	{
		switch (_version)
		{
			case 737:
			case 740:
			case 744:
			case 746:
				getClient().sendPacket(new KeyPacket(getClient().enableCrypt()));
				break;
			
			default:
				getClient().close((L2GameServerPacket) null);
				break;
		}
	}
}*/