package MinerUtils;

public class Unit {

	/*
	 * OFFSET LISTS
	 * We want to expand this to read the cheat engine file
	 */
	public static final int O_HPC = 0x670, O_HPM = 0x0680, O_MAC = 0x02C4, O_MAM = 0x02D4;
	public static final int O_AR = 0xAAC, O_MR = 0xA34, O_AD = 0x984, O_AP = 0x994;
	public static final int O_X = 0x080, O_Y = 0x088, O_Z = 0x084;
	public static final int O_ChampName = 0x1764, O_PlayerName=0x01C, O_UnitType=0x018, O_Team=0x014;
	public static final int O_Alive = 0x094, O_Level = 0x399C;
	public static final int O_Movespeed = 0xA44;
	
	//Living = 642
	
	/*
	 * Basically Defines.
	 */
	public static final int CHAMP_TYPE = 5121, MINION_TYPE=12;
	
	/*
	 * VARS AND SHIT
	 */
	public int m_UnitBase;
	public int m_UnitType, m_Team;
	public float m_Coords[];
	public int m_GridLoc[];
	public float m_HPC, m_HPM, m_MAC, m_MAM;
	//public float m_X, m_Y, m_Z; //Depricating XYZ, using float[3] since it's FUCKLOADS simpler
	public int m_Level;
	public float m_AR, m_MR, m_APen, m_MRPen, m_APPen, m_MRPPen, m_Movespeed;
	public float m_AD, m_AP;//, m_APen, m_MRPen, m_APPen, m_MRPPen, m_Movespeed;
	public String m_ChampName, m_PlayerName;
	/*
	 * MORE VARS AND SHIT CAN GO HERE.
	 * 
	 */
	public float m_LastUpdated, m_DT;
	public boolean m_Valid, m_Alive;
	public float m_StatScore;
	public float m_DistanceToUser, m_Threat;
	
	public int m_DeathCount;
	public float m_DeathTileSum;
	
	public Unit()
	{
		Default();
	}
	public void Default()
	{
		m_DeathCount=0;
		m_DeathTileSum=0;
		m_Level = 0;
		m_AR=m_MR=m_APen=m_MRPen=m_APPen=m_MRPPen=m_Movespeed=m_AD=m_AP=m_HPM=0;
		m_Coords = new float[3];
		m_GridLoc = new int[2];
		m_StatScore = 0; 
		m_Threat = 1;
	}
	public void Score(Unit tDev, Unit tAvg)
	{
		m_StatScore = m_Level;//0;
		
		if (tDev.m_Level!=0)
		{
			m_StatScore += (m_Level-tAvg.m_Level)/tDev.m_Level;
		}
		if (tDev.m_AR!=0)
		{
			m_StatScore += (m_AR-tAvg.m_AR)/(tDev.m_AR*2);
		}
		if (tDev.m_MR!=0)
		{
			m_StatScore += (m_MR-tAvg.m_MR)/(tDev.m_MR*2);
		}
		if (tDev.m_Movespeed!=0)
		{
			m_StatScore += (m_Movespeed-tAvg.m_Movespeed)/(tDev.m_Movespeed*2);
		}
		if (tDev.m_AD!=0)
		{
			m_StatScore += (m_AD-tAvg.m_AD)/tDev.m_AD;
		}
		if (tDev.m_AP!=0)
		{
			m_StatScore += (m_AP-tAvg.m_AP)/tDev.m_AP;
		}
		if (tDev.m_HPM!=0)
		{
			m_StatScore += (m_HPM-tAvg.m_HPM)/tDev.m_HPM;
		}
		/*
		m_StatScore += (m_AR-tAvg.m_AR)/tDev.m_AR;
		m_StatScore += (m_MR-tAvg.m_MR)/tDev.m_MR;
		m_StatScore += (m_Movespeed-tAvg.m_Movespeed)/tDev.m_Movespeed;
		m_StatScore += (m_AD-tAvg.m_AD)/tDev.m_AD;
		m_StatScore += (m_AP-tAvg.m_AP)/tDev.m_AP;
		m_StatScore += (m_HPM-tAvg.m_HPM)/tDev.m_HPM;
		*/
		//m_StatScore += (m_Level-tAvg.m_Level)/tDev.m_Level;
		//m_StatScore += (m_Level-tAvg.m_Level)/tDev.m_Level;
		//m_StatScore += (m_Level-tAvg.m_Level)/tDev.m_Level;
		//m_StatScore += (m_Level-tAvg.m_Level)/tDev.m_Level;
		

		float aThreatModifier = (m_HPC/m_HPM);
		aThreatModifier *= m_Level;
		m_Threat = aThreatModifier;
	}
	public void Plus(Unit tAdd)
	{
		m_Level += tAdd.m_Level;
		m_AR += tAdd.m_AR;
		m_MR += tAdd.m_MR;
		//m_APen += tAdd.m_APen;
		//m_MRPen += tAdd.m_MRPen;
		m_Movespeed += tAdd.m_Movespeed;
		m_Level += tAdd.m_Level;
		m_AD += tAdd.m_AD;
		m_AP += tAdd.m_AP;
		m_HPM += tAdd.m_HPM;
	}
	public void Divide(float tDiv)
	{
		m_Level /= tDiv;
		m_AR /= tDiv;
		m_MR /= tDiv;
		//m_APen /= tDiv;
		//m_MRPen /= tDiv;
		m_Movespeed /= tDiv;
		m_Level /= tDiv;
		m_AD /= tDiv;
		m_AP /= tDiv; 
		m_HPM /= tDiv; 
	}
	public Unit Deviation(Unit tDev)
	{
		Unit aRet = new Unit();
		aRet.Default();
		
		aRet.m_Level += Math.abs(tDev.m_Level- m_Level);
		aRet.m_AR += Math.abs(tDev.m_AR- m_AR);
		aRet.m_MR += 	Math.abs(tDev.m_MR- m_MR);
		//aRet.m_APen += 	Math.abs(tDev.m_APen- m_APen);
		//aRet.m_MRPen += Math.abs(tDev.m_MRPen- m_MRPen);
		aRet.m_Movespeed += Math.abs(tDev.m_Movespeed- m_Movespeed);
		aRet.m_AD += 	Math.abs(tDev.m_AD- m_AD);
		aRet.m_AP += 	Math.abs(tDev.m_AP- m_AP);
		aRet.m_HPM += 	Math.abs(tDev.m_HPM- m_HPM);
		return aRet;
	}
	public float GetDistance(float tCoords[])
	{

		float aRet,aChamp,aBase;
		aRet = aChamp = aBase = 0;
		
		aChamp += (m_Coords[0]-tCoords[0]) * (m_Coords[0]-tCoords[0]);
		aChamp += (m_Coords[1]-tCoords[1]) * (m_Coords[1]-tCoords[1]);
		aChamp += (m_Coords[2]-tCoords[2]) * (m_Coords[2]-tCoords[2]);
		aRet = (float) Math.sqrt(aChamp);
		
		
		if (m_DT>8)
		{
			if (m_Team==100)
			{
				int aBlueBase = 500;
				aBase += (aBlueBase-tCoords[0]) * (aBlueBase-tCoords[0]);
				aBase += (aBlueBase-tCoords[1]) * (aBlueBase-tCoords[1]);
				aBase += (aBlueBase-tCoords[2]) * (aBlueBase-tCoords[2]);
			}
			else
			{
				int aRedBase = 14500;
				aBase += (aRedBase-tCoords[0]) * (aRedBase-tCoords[0]);
				aBase += (aRedBase-tCoords[1]) * (aRedBase-tCoords[1]);
				aBase += (aRedBase-tCoords[2]) * (aRedBase-tCoords[2]);
			}
			aBase = (float) Math.sqrt(aBase);
			if (aBase < aRet) aRet = aBase;
		}
		
		return aRet;
	}
	public float GetETA(float tCoords[])
	{
		float aDistance = GetDistance(tCoords);
		if (aDistance>(m_DT*m_Movespeed))
		{
			return (aDistance-(m_DT*m_Movespeed))/m_Movespeed;
		}
		else
		{
			return 0;
		}
	}
}
