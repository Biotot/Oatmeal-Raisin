package MinerUtils;

public class Unit {

	/*
	 * OFFSET LISTS
	 * We want to expand this to read the cheat engine file
	 */
	public static final int O_HPC = 0x678, O_HPM = 0x0688, O_MAC = 0x02CC, O_MAM = 0x02DC;
	public static final int O_X = 0x080, O_Y = 0x084, O_Z = 0x088;
	public static final int O_ChampName = 0x7C0, O_PlayerName=0x020, O_UnitType=0x018, O_Team=0x014;
	public static final int O_Alive = 0x09C;
	public static final int O_Movespeed = 0x0AA0;
	
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
	public float m_HPC, m_HPM, m_MAC, m_MAM;
	//public float m_X, m_Y, m_Z; //Depricating XYZ, using float[3] since it's FUCKLOADS simpler
	public float m_Coords[];
	public float m_Armor, m_MR, m_APen, m_MRPen, m_APPen, m_MRPPen, m_Movespeed;
	public String m_ChampName, m_PlayerName;
	/*
	 * MORE VARS AND SHIT CAN GO HERE.
	 * 
	 */
	public float m_LastUpdated, m_DT;
	public boolean m_Valid, m_Alive;
	public float m_DistanceToUser, m_Threat;
	
	public Unit()
	{
		m_Coords = new float[3];
		m_Threat = 1;
	}
	public float GetDistance(float tCoords[])
	{
		float aRet=0;
		aRet += (m_Coords[0]-tCoords[0]) * (m_Coords[0]-tCoords[0]);
		aRet += (m_Coords[1]-tCoords[1]) * (m_Coords[1]-tCoords[1]);
		aRet += (m_Coords[2]-tCoords[2]) * (m_Coords[2]-tCoords[2]);
		return (float) Math.sqrt(aRet);
	}
	public float GetETA(float tCoords[], float tClock)
	{
		m_DT = 0;
		if ((tClock-m_LastUpdated)>3)
		{
			m_DT = tClock-m_LastUpdated;
		}
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
