package MinerUtils;

public class Unit {

	/*
	 * OFFSET LISTS
	 * We want to expand this to read the cheat engine file
	 */
	public static final int O_HPC = 0x678, O_HPM = 0x0688, O_MAC = 0x02CC, O_MAM = 0x02DC;
	public static final int O_X = 0x80, O_Y = 0x84, O_Z = 0x88;
	public static final int O_ChampName = 0x7C0, O_PlayerName=0x018, O_UnitType=0x018, O_Team=0x014;
	public static final int O_Movespeed = 0x0AA0;
	
	
	
	/*
	 * Basically Defines.
	 */
	public static final int CHAMP_TYPE = 5212, MINION_TYPE=12;
	
	/*
	 * VARS AND SHIT
	 */
	public int m_UnitBase;
	public int m_UnitType;
	public float m_HPC, m_HPM, m_MAC, m_MAM;
	public float m_X, m_Y, m_Z;
	public float m_Armor, m_MR, m_APen, m_MRPen, m_APPen, m_MRPPen, m_Movespeed;
	public String m_ChampName, m_PlayerName;
	/*
	 * MORE VARS AND SHIT CAN GO HERE.
	 * 
	 */
	public float m_LastUpdated;
	public boolean m_Valid;
	
	
	public float GetDistance(Unit tUnit)
	{
		float aRet=0;
		aRet += (m_X-tUnit.m_X) * (m_X-tUnit.m_X);
		aRet += (m_Y-tUnit.m_Y) * (m_Y-tUnit.m_Y);
		aRet += (m_Z-tUnit.m_Z) * (m_Z-tUnit.m_Z);
		return (float) Math.sqrt(aRet);
	}
	public float GetETA(Unit tUnit)
	{
		return GetDistance(tUnit)/tUnit.m_Movespeed;
	}
}
