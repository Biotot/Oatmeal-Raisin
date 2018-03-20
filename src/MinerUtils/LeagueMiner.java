package MinerUtils;

import java.util.ArrayList;

import com.sun.jna.Memory;

public class LeagueMiner extends MinerBase {

	/*
	 * CONST BASE GAME OFFSETS
	 */
	static final int O_Screen = 0x0219E81C, O_Clock = 0x0219E834;
	static final int O_ChampList = 0x02DF129C, O_UserLocation = 0x02DFCB80;
	public static final int HEATMAPSIZE = 5, MAPWIDTH = 15000, MAPHEIGHT = 15000;//Summoners rift specific. Fuck the other maps
	
	public int m_PlayerIndex;
	public float m_Clock;
	public ArrayList<Unit> m_ChampList;
	public HeatmapPoint m_HeatMap[][];
	
	public LeagueMiner(int tMapSize)
	{
		m_HeatMap = new HeatmapPoint[HEATMAPSIZE][HEATMAPSIZE];
		Setup();
		m_ChampList = new ArrayList<Unit>();
		PlayerListInit(tMapSize);
	}
	
	
	public int PlayerListInit(int tMapSize)
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
		//Loading the heatmap objects.
		
		int aHeatMapIntervalX = MAPWIDTH/(HEATMAPSIZE+1);
		int aHeatMapIntervalY = MAPHEIGHT/(HEATMAPSIZE+1);
		for(int x=0; x<HEATMAPSIZE; x++)
		{
			for (int y=0; y<HEATMAPSIZE; y++)
			{
				m_HeatMap[x][y] = new HeatmapPoint();
				m_HeatMap[x][y].m_MapCoords[0] = (x+1)*aHeatMapIntervalX;
				m_HeatMap[x][y].m_MapCoords[1] = 100;//Needs a y to reuse the distance math. Should throw this out tbh
				m_HeatMap[x][y].m_MapCoords[2] = (y+1)*aHeatMapIntervalY;
				m_HeatMap[x][y].m_ScreenCoords = CoordsToMiniMap(m_HeatMap[x][y].m_MapCoords, tMapSize);
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
				if (m_ChampList.get(x).m_Alive)//User isnt dead
				{
					m_ChampList.get(x).m_DistanceToUser = m_ChampList.get(x).GetETA(m_ChampList.get(m_PlayerIndex).m_Coords, m_Clock);
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
		float aX = tUnit.m_Coords[0];
		tUnit.m_Coords[0] = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_X,4).getFloat(0);
		aChanged = (aChanged || (aX!=tUnit.m_Coords[0]));
		float aY = tUnit.m_Coords[1];
		tUnit.m_Coords[1] = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Y,4).getFloat(0);
		aChanged = (aChanged || (aY!=tUnit.m_Coords[1]));
		float aZ = tUnit.m_Coords[2];
		tUnit.m_Coords[2] = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Z,4).getFloat(0);
		aChanged = (aChanged || (aZ!=tUnit.m_Coords[2]));
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
		tUnit.m_Alive = (642==readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Alive,4).getInt(0));
		//tUnit.m_APen = readMemory(m_Game, tUnit.m_UnitBase+Unit.O_HPM, 4).getFloat(0);
		//tUnit.m_MRPen = readMemory(m_Game, tUnit.m_UnitBase+Unit.O_HPM, 4).getFloat(0);
		//tUnit.m_Armor = readMemory(m_Game, tUnit.m_UnitBase+Unit.O_HPM, 4).getFloat(0);
	}
	
	public void UpdateMapPressure()
	{
		m_Clock = GetClock();//Shouldn't be needed. Clock should be accurate. This is just assuring accuracy
		int aUserTeam = m_ChampList.get(m_PlayerIndex).m_Team;
		int aDMult = 5;//scalable multiplier for the distance. 
		float aScoreSum = 0;

		//fugly ass loop. N*(M^2) + 2(M^2)
		for(int x=0; x<HEATMAPSIZE; x++)
		{
			for (int y=0; y<HEATMAPSIZE; y++)
			{
				m_HeatMap[x][y].m_Score = 0;
				for (int z=0; z<m_ChampList.size(); z++)
				{
					if (m_ChampList.get(z).m_Alive)
					{
						float aETA = m_ChampList.get(z).GetETA(m_HeatMap[x][y].m_MapCoords, m_Clock);
						float aScore = (aETA<(aDMult*1))? 5: (aETA<(aDMult*2))? 4 : (aETA<(aDMult*3))? 3 : (aETA<(aDMult*4))? 2 : 1;
						aScore = aScore*m_ChampList.get(z).m_Threat;
						m_HeatMap[x][y].m_Score += (m_ChampList.get(z).m_Team==aUserTeam)? aScore : -aScore;//+ if ally - if enemy
					}
				}
				aScoreSum = m_HeatMap[x][y].m_Score;
			}
		}
		float aScoreAvg = aScoreSum/(HEATMAPSIZE*HEATMAPSIZE);
		float aDev = 0;
		for(int x=0; x<HEATMAPSIZE; x++)
		{
			for (int y=0; y<HEATMAPSIZE; y++)
			{
				aDev += Math.abs(m_HeatMap[x][y].m_Score - aScoreAvg);
			}
		}
		aDev = aDev/(HEATMAPSIZE*HEATMAPSIZE);
		

		for(int x=0; x<HEATMAPSIZE; x++)
		{
			for (int y=0; y<HEATMAPSIZE; y++)
			{
				float aPointDev = (m_HeatMap[x][y].m_Score - aScoreAvg)/aDev;
				m_HeatMap[x][y].m_Score = aPointDev;//Can be made 1 line. Currently 2 for debugging.
			}
		}
		
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

	public static int[] CoordsToMiniMap(float[] tCoords, int tMapScreenSize)
	{
		
		int aMapSize = 15000;
		int aX = (int)(tMapScreenSize*(tCoords[0]/aMapSize));
		int aZ = (int)(tMapScreenSize*(tCoords[2]/aMapSize));
		
		return new int[] {
				aX,
				tMapScreenSize-aZ
		};
	}
	
	public float[] GetScreenCoords()
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
