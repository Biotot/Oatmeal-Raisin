package ScreenWriterUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import MinerUtils.LeagueMiner;
import MinerUtils.Unit;

public class LeagueChampHUD extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int TIMER_DELAY = 1000;
	private int m_Framesize = 500;
	public int rectX = 150;
	public int rectY = 50;
	public int width = 100;
	public int height = 25;
	public int m_UserTeam = 0;
	
	private static LeagueMiner m_Miner;
	
	public LeagueChampHUD(int tMapSize) {
		m_Framesize = tMapSize;
		this.setMinimumSize(getPreferredSize());
		setOpaque(false);
		setLayout(new GridBagLayout());
		m_Miner = new LeagueMiner();
		m_UserTeam = m_Miner.m_ChampList.get(m_Miner.m_PlayerIndex).m_Team;
		new Timer(TIMER_DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actEvt) {

				m_Miner.UpdatePlayerListPrimary();
				m_Miner.UpdatePlayerListSecondary();
				m_Miner.UpdateMapPressure();
				repaint();
				//System.out.println("TOCK" + System.currentTimeMillis());
			}
		}).start();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(m_Framesize, m_Framesize*3);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(new Color(255, 255, 255, 200));
		
		int aIndex = 0;
		int aTimeSlice = 5;
		int aTimeModifier = 10;
		

		int aXCoord = m_Framesize - width - 5;
		for (int x = 0; x < m_Miner.m_ChampList.size(); x++) {
			Unit aUnit = m_Miner.m_ChampList.get(x);
			if (aUnit.m_Team!=m_UserTeam)
			{
				int aYCoord = (aIndex*height);
				aIndex++;
				g.setColor(TempPicker(aTimeSlice, aUnit.m_DistanceToUser-aTimeModifier));
				g.drawRect(aXCoord, rectY+aYCoord, width, height);
				g.drawString(aUnit.m_ChampName, aXCoord+5, rectY+10+aYCoord);
				g.drawString(String.format("%.0f",aUnit.m_DistanceToUser), aXCoord+5, rectY+20+aYCoord);
				g.drawString(String.format("%.0f",aUnit.m_DT), aXCoord+45, rectY+20+aYCoord);
			}
		}
		//float[] aCoord = m_Miner.GetScreenCoords();

		//String aCoordLabel = aCoord[0] +"," + aCoord[1] + "," + aCoord[2];
		//g.drawString(aCoordLabel, rectX, rectY+75);
		
		
		int aHeatMapX = 0;
		int aHeatMapY = (m_Framesize*2);
		int aHeatMapDistance = m_Framesize/(LeagueMiner.HEATMAPSIZE+1);
		double aMult = 0.5;

		g.setColor(new Color(255, 255, 255, 200));
		g.drawRect(aHeatMapX, aHeatMapY, m_Framesize-2, m_Framesize-2);
		for(int x=0; x<LeagueMiner.HEATMAPSIZE; x++)
		{
			for (int y=0; y<LeagueMiner.HEATMAPSIZE; y++)
			{
				int aX = aHeatMapX+((x+1)*aHeatMapDistance);
				int aY = aHeatMapY+(m_Framesize-((y+1)*aHeatMapDistance));
				
				//Choose color
				//x<-2 white, x<-1 Red, -1<x<1 white, x>1 yellow x>2 green
				g.setColor(TempPicker(aMult, m_Miner.m_HeatMap[x][y].m_Score));

				g.drawRect(aX, aY, 2, 2);
				//g.drawString(String.format("%.1f",m_Miner.m_HeatMap[x][y].m_Score), aX, aY);
				
			}
		}
		
		g.dispose();
	}
	
	protected Color TempPicker(double tDev, float tData)
	{
		Color aRet;

		double aVal = tData/tDev;
		if (aVal<-2)
		{
			aRet = new Color(255, 0, 0, 255);
		}
		else if (aVal<-1)
		{  
			aRet = new Color(255,165, 0, 255);
		}
		else if (aVal>-1&&aVal<1)
		{
			aRet = new Color(255,255, 255, 255);
		}
		else if (aVal>2)
		{
			aRet = new Color(0,255, 0, 255);
		}
		else if (aVal>1)
		{
			aRet = new Color(255,255, 0, 255);
		}
		else
		{//Default to transparent
			aRet = new Color(0,0,0,0);
		}
		
		return aRet;
	}
}
