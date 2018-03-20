package MinerUtils;

public class HeatmapPoint {
	public float m_MapCoords[];
	public int m_ScreenCoords[];
	public float m_Score;
	
	public HeatmapPoint()
	{
		m_MapCoords = new float[3];
		m_ScreenCoords = new int[2];
		m_Score = 0;
	}
}
