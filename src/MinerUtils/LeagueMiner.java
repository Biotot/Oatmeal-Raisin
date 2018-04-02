package MinerUtils;

import java.util.ArrayList;

import com.sun.jna.Memory;

public class LeagueMiner extends MinerBase {

	/*
	 * CONST BASE GAME OFFSETS
	 */
	static final int O_Screen = 0x021BF724, O_Clock = 0x021BF73C;
	static final int O_ChampList = 0x021BDCAC, O_UserLocation = 0x02E1DC08;
	public static final int HEATMAPSIZE = 15, MAPWIDTH = 15000, MAPHEIGHT = 15000;//Summoners rift specific. Fuck the other maps
	
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
					m_ChampList.get(x).m_DistanceToUser = m_ChampList.get(x).GetETA(m_ChampList.get(m_PlayerIndex).m_Coords);
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
		Unit aAvg = new Unit();
		aAvg.Default();
		for (int x=0; x<m_ChampList.size(); x++)
		{
			UpdatePlayerSecondary(m_ChampList.get(x));
			aAvg.Plus(m_ChampList.get(x));
		}
		aAvg.Divide(m_ChampList.size());
		Unit aDev = new Unit();
		aDev.Default();
		for (int x=0; x<m_ChampList.size(); x++)
		{
			aDev.Plus(m_ChampList.get(x).Deviation(aAvg));
		}
		aDev.Divide(m_ChampList.size());
		for (int x=0; x<m_ChampList.size(); x++)
		{
			m_ChampList.get(x).Score(aDev, aAvg);
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

		
		float aPotentialHP = tUnit.m_HPC;
		float aPotentialMP = tUnit.m_MAC;
		if (aChanged)//Check if any values changed. If yes, update last updated
		{
			tUnit.m_LastUpdated = tClock;
			tUnit.m_DT = 0;
		}
		else
		{
			 tUnit.m_DT = (tClock - tUnit.m_LastUpdated);
			 if (tUnit.m_DT > 8)
			 {
				 aPotentialHP = tUnit.m_HPM;
				 aPotentialMP = tUnit.m_MAM;
			 }
			
		}
		
		//Threat modifier. 2 parts HP 1 part MANA //fuck... this'll fuck on energy units.
		//float aThreatModifier = (2*(tUnit.m_HPC/tUnit.m_HPM) + (tUnit.m_MAC/tUnit.m_MAM))/3;
		//Else, the player is MIA or fully idle <- Complicated but uncommon issue. Should be able to find the 'mia' flag, but it might require
		//a tab check like the KDA+CS check
		
		int aHeatMapSpacing = 15000/LeagueMiner.HEATMAPSIZE;
		tUnit.m_GridLoc[0] = (int)(tUnit.m_Coords[0])/aHeatMapSpacing;
		tUnit.m_GridLoc[1] = (int)(tUnit.m_Coords[2])/aHeatMapSpacing;
		
		
		/*
		*/
		
		return aChanged;
	}
	public void UpdatePlayerSecondary(Unit tUnit)
	{
		tUnit.m_HPM = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_HPM,4).getFloat(0);
		tUnit.m_MAM = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_MAM,4).getFloat(0);
		tUnit.m_Movespeed = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Movespeed,4).getFloat(0);
		tUnit.m_AR = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_AR, 4).getFloat(0);
		tUnit.m_MR = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_MR,4).getFloat(0);
		tUnit.m_AD = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_AD,4).getFloat(0);
		tUnit.m_AP = readMemory(m_Game, tUnit.m_UnitBase + Unit.O_AP,4).getFloat(0);
		boolean aAlive = tUnit.m_Alive;
		tUnit.m_Alive = (642==readMemory(m_Game, tUnit.m_UnitBase + Unit.O_Alive,4).getInt(0));
		if ((aAlive!=tUnit.m_Alive)&&(tUnit.m_Alive==false))
		{
			float aGridAvg = GetGridAverage(tUnit.m_GridLoc[0], tUnit.m_GridLoc[1]);
			tUnit.m_DeathTileSum+=aGridAvg;
			tUnit.m_DeathCount++;
		}
		tUnit.m_Level = readMemory(m_Game, tUnit.m_UnitBase+Unit.O_Level, 4).getInt(0);
		//tUnit.m_MRPen = readMemory(m_Game, tUnit.m_UnitBase+Unit.O_HPM, 4).getFloat(0);
		//tUnit.m_Armor = readMemory(m_Game, tUnit.m_UnitBase+Unit.O_HPM, 4).getFloat(0);
		
		//tUnit.m_Threat = tUnit.m_Level;
	}
	
	public void UpdateMapPressure()
	{
		m_Clock = GetClock();//Shouldn't be needed. Clock should be accurate. This is just assuring accuracy
		int aUserTeam = m_ChampList.get(m_PlayerIndex).m_Team;
		int aDMult = 5;//scalable multiplier for the distance. 
		float aScoreSum = 0;

		//fugly ass loop. N*(M^2)*(Distance) + 2(M^2)
		
		for(int x=0; x<HEATMAPSIZE; x++)
		{
			for (int y=0; y<HEATMAPSIZE; y++)
			{
				//m_HeatMap[x][y].m_Score = 0;
				m_HeatMap[x][y].m_Score -= 0;//m_HeatMap[x][y].m_Score/2;
				for (int z=0; z<m_ChampList.size(); z++)
				{
					if (m_ChampList.get(z).m_Alive)
					{
						float aETA = m_ChampList.get(z).GetETA(m_HeatMap[x][y].m_MapCoords);
						//float aScore = ((float)(aDMult)/aETA);//Expect them there in 20s or less points
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
			aPlayer.m_Level = readMemory(m_Game, aPlayer.m_UnitBase+Unit.O_Level, 4).getInt(0);
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
	public float GetGridAverage(int tX, int tY)
	{
		//float aRet = 0;
		 ArrayList<HeatmapPoint> aBorderTiles = GetGridBorders(tX,tY);
		float aAvg = 0;
		int aActiveCount = 0;
		for(int y=0; y<aBorderTiles.size(); y++)
		{
			
			if (aBorderTiles.get(y)!=null)
			{
				aAvg += aBorderTiles.get(y).m_Score;
				aActiveCount++;
			}
		}
		aAvg = aAvg/aActiveCount;
		return aAvg;
	}
	public ArrayList<HeatmapPoint> GetGridBorders(int tX, int tY)
	{
		ArrayList<HeatmapPoint> aGridPoints = new ArrayList<HeatmapPoint>();
		boolean aLeft, aRight, aTop, aBot;
		aLeft = aRight = aTop = aBot = false;
		if (tX>0) aLeft = true;
		if (tY>0) aBot = true;
		if (tX<LeagueMiner.HEATMAPSIZE) aRight = true;
		if (tY<LeagueMiner.HEATMAPSIZE) aTop = true;
		
		//Shifting shit just a smidge over
		tX--;
		tY--;
		if (aTop&&aLeft)
		{
			aGridPoints.add(m_HeatMap[tX][tY+1]);
		}
		else
		{
			aGridPoints.add(null);
		}
		if (aTop&&aRight)
		{
			aGridPoints.add(m_HeatMap[tX+1][tY+1]);
		}
		else
		{
			aGridPoints.add(null);
		}
		if (aBot&&aLeft)
		{
			aGridPoints.add(m_HeatMap[tX][tY]);
		}
		else
		{
			aGridPoints.add(null);
		}
		if (aBot&&aRight)
		{
			aGridPoints.add(m_HeatMap[tX+1][tY]);
		}
		else
		{
			aGridPoints.add(null);
		}
		
		return aGridPoints;
	}
}
