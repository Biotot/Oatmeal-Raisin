package MinerUtils;

import java.util.ArrayList;

import com.sun.jna.Memory;

public class LeagueMiner extends MinerBase {

	/*
	 * CONST BASE GAME OFFSETS
	 */
	static final int O_Screen = 0x0215070C, O_Clock = 0x02150724;
	static final int O_ChampList = 0x02D968E4, O_UserLocation = 0x02DB2160;
	
	public int m_PlayerIndex;
	public float m_Clock;
	public ArrayList<Unit> m_ChampList;
	public float m_HeatMap[][];
	
	public LeagueMiner()
	{
		m_HeatMap = new float[5][5];
		Setup();
		m_ChampList = new ArrayList<Unit>();
		PlayerListInit();
	}
	
	
	public int PlayerListInit()
	{
		m_Clock = GetClock();
		int aUserBase = readMemory(m_Game, O_GameBase + O_UserLocation,4).getInt(0);
		for(int x=0; x<10; x++)//Change to 12 if rito shines hexakill upon us.
		{
			Unit aUnit = GetPlayer(x);
			
			if (aUnit.m_UnitType==Unit.CHAMP_TYPE)
			{
				aUnit.m_LastUpdated = m_Clock;
				aUnit.m_DistanceToUser = 0;
				if (aUnit.m_UnitBase==aUserBase)
				{
					m_PlayerIndex = x;
				}
				m_ChampList.add(aUnit);
			}
			else//if invalid the champ list has ended, stop looking. 
			{
				System.out.println("Invalid champ @" + x);
				break;
			}
		}

		return m_ChampList.size();
	}
	
	public void UpdatePlayerListPrimary()
	{
		m_Clock = GetClock();
		for (int x=0; x<m_ChampList.size(); x++)
		{
			UpdatePlayerPrimary(m_ChampList.get(x), m_Clock);
			//This just looks ugly. Functional, but ugly.
			if (x!=m_PlayerIndex)//If the unit isn't the user find the distance.
			{
				if (x!=m_PlayerIndex)//User isnt dead
				{
					m_ChampList.get(x).m_DistanceToUser = m_ChampList.get(x).GetETA(m_ChampList.get(m_PlayerIndex), m_Clock);
				}
				else
				{
					m_ChampList.get(x).m_DistanceToUser = 15000;
				}
			}
			else
			{
				m_ChampList.get(x).m_DistanceToUser = 0;
			}
		}
	}

	public void UpdatePlayerListSecondary()
	{
		for (int x=0; x<m_ChampList.size(); x++)
		{
			UpdatePlayerSecondary(m_ChampList.get(x));
		}
	}
	//This would be cleaner in the Unit class, but I'd prefer the readmemory to be an inherited call instead of static.
	//Potential change for thin mints
	public boolean UpdatePlayerPrimary(Unit tUnit, float tClock)
	{
		boolean aChanged = false;//Check if any values changed. If yes, update last updated
		float aX = tUnit.m_X;
		tUnit.m_X = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_X,4).getFloat(0);
		aChanged = (aChanged || (aX!=tUnit.m_X));
		float aY = tUnit.m_Y;
		tUnit.m_Y = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Y,4).getFloat(0);
		aChanged = (aChanged || (aY!=tUnit.m_Y));
		float aZ = tUnit.m_Z;
		tUnit.m_Z = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Z,4).getFloat(0);
		aChanged = (aChanged || (aZ!=tUnit.m_Z));
		float aHPC = tUnit.m_HPC;
		tUnit.m_HPC = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_HPC,4).getFloat(0);
		aChanged = (aChanged || (aHPC!=tUnit.m_HPC));
		float aMAC = tUnit.m_MAC;
		tUnit.m_MAC = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_MAC,4).getFloat(0);
		aChanged = (aChanged || (aMAC!=tUnit.m_MAC));
		
		if (aChanged)//Check if any values changed. If yes, update last updated
		{
			tUnit.m_LastUpdated = tClock;
		}
		//Else, the player is MIA or fully idle <- Complicated but uncommon issue. Should be able to find the 'mia' flag, but it might require
		//a tab check like the KDA+CS check
		
		/*
		*/
		
		return aChanged;
	}
	public void UpdatePlayerSecondary(Unit tUnit)
	{
		tUnit.m_HPM = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_HPM,4).getFloat(0);
		tUnit.m_MAM = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_MAM,4).getFloat(0);
		tUnit.m_Movespeed = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Movespeed,4).getFloat(0);
	}
	public void UpdateMapPressure()
	{
		
		
		
	}
	
	public static Unit GetPlayer(int tUnitIndex)
	{
		Unit aPlayer = new Unit();
		if (tUnitIndex>=0 && tUnitIndex<10)//Change to 12 if rito shines hexakill upon us.
		{
			Memory aChampListAddress = readMemory(m_Game, O_GameBase + O_ChampList ,4);
			Memory aChampAddress = readMemory(m_Game, aChampListAddress.getInt(0) + (0x04*tUnitIndex) ,4);
			aPlayer.m_UnitBase = aChampAddress.getInt(0);
			aPlayer.m_UnitType = readMemory(m_Game, aPlayer.m_UnitBase + Unit.O_UnitType,4).getInt(0);
			aPlayer.m_Team = readMemory(m_Game, aPlayer.m_UnitBase + Unit.O_Team,4).getInt(0);
			aPlayer.m_ChampName = readMemory(m_Game, aPlayer.m_UnitBase + Unit.O_ChampName,10).getString(0);
			aPlayer.m_PlayerName = readMemory(m_Game, aPlayer.m_UnitBase + Unit.O_PlayerName,10).getString(0);
		}
		else
		{
			System.out.println("INVALID INDEX, STOP BEING SHITTY");
		}

		return aPlayer;
	}

	
	public static float[] GetScreenCoords()
	{
		return new float[] {
				readMemory(m_Game, O_GameBase + O_Screen + 0x0,4).getFloat(0),
				readMemory(m_Game, O_GameBase + O_Screen + 0x4,4).getFloat(0),
				readMemory(m_Game, O_GameBase + O_Screen + 0x8,4).getFloat(0)
		};
	}

	public float GetClock()
	{
		return readMemory(m_Game,O_GameBase + O_Clock,4).getFloat(0);
	}
	
}
