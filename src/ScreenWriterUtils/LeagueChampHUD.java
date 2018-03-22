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
	public Unit m_UserUnit;
	
	private static LeagueMiner m_Miner;
	
	public LeagueChampHUD(int tMapSize) {
		m_Framesize = tMapSize;
		this.setMinimumSize(getPreferredSize());
		setOpaque(false);
		setLayout(new GridBagLayout());
		m_Miner = new LeagueMiner(tMapSize);
		m_Miner.UpdatePlayerListPrimary();
		m_Miner.UpdatePlayerListSecondary();
		m_UserUnit = m_Miner.m_ChampList.get(m_Miner.m_PlayerIndex);
		m_UserTeam = m_UserUnit.m_Team;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		int aTimeModifier = 15;
		

		//float[] aCoord = m_Miner.GetScreenCoords();
		//String aCoordLabel = aCoord[0] +"," + aCoord[1] + "," + aCoord[2];
		//g.drawString(aCoordLabel, rectX, rectY+75);
		/*
		 * I've divided these panels up into 3 parts. 
		 * they need to be positioned less shitty and cleaned up
		 * 
		 */
		
		//first panel uses the first 1/3
		int aXCoord = m_Framesize - (width*2) - 5;
		for (int x = 0; x < m_Miner.m_ChampList.size(); x++) {
			Unit aUnit = m_Miner.m_ChampList.get(x);
			if (aUnit.m_Team!=m_UserTeam)
			{
				int aYCoord = m_Framesize + (aIndex*height);
				aIndex++;
				g.setColor(TempPicker(aTimeSlice, aUnit.m_DistanceToUser-aTimeModifier));
				g.drawRect(aXCoord, aYCoord, width, height);
				g.drawString(aUnit.m_ChampName, aXCoord+5, 10+aYCoord);
				g.drawString(String.format("%.0f",aUnit.m_DistanceToUser), aXCoord+5, 20+aYCoord);
				g.drawString(String.format("%.0f",aUnit.m_DT), aXCoord+45, 20+aYCoord);
			}
		}

		float[] aCoord = m_Miner.GetScreenCoords();
		double aHeatColorMult = 0.75;
		//second panel shows the risk of the corner slots.
		
		{
			//2500 is hardcoded for 5x5 heatmap
			int aHeadsupX = 4;
			int aHeadsupY = m_Framesize+4;
			int aHeadsupSpacing = 10;
			int aHeatMapSpacing = 15000/LeagueMiner.HEATMAPSIZE;

			//g.drawString(""+aXIndex+":"+aYIndex, aHeadsupX+aHeadsupSpacing*2, aHeadsupY+4);
			//String aCoordLabel = aCoord[0] +"," + aCoord[1] + "," + aCoord[2];
			//g.drawString(aCoordLabel, aHeadsupX+(aHeadsupSpacing*2), aHeadsupY+aHeadsupSpacing+4);
			
			aCoord[2]+=1500;
			for (int x = 0; x<2; x++)
			{

				int aLoopHeadsUpY = aHeadsupY + ((aHeadsupSpacing+10)*x) + 2;
				int aXIndex = (x==0)? (int)(aCoord[0])/aHeatMapSpacing : (int)m_UserUnit.m_Coords[0]/2500;
				int aYIndex = (x==0)? (int)(aCoord[2])/aHeatMapSpacing : (int)m_UserUnit.m_Coords[2]/2500;
				//int aXIndex = (int)aCoord[0]/2500;
				//int aYIndex = (int)(aCoord[2]+2000)/2500;
				////int aXIndex = (int)m_UserUnit.m_Coords[0]/2500;
				//int aYIndex = (int)m_UserUnit.m_Coords[2]/2500;a
				g.setColor(new Color(255, 255, 255, 200));
				g.drawRect(0, aLoopHeadsUpY-4, aHeadsupSpacing+10, aHeadsupSpacing+10);
			
				boolean aLeft, aRight, aTop, aBot;
				aLeft = aRight = aTop = aBot = false;
				if (aXIndex>0) aLeft = true;
				if (aYIndex>0) aBot = true;
				if (aXIndex<LeagueMiner.HEATMAPSIZE) aRight = true;
				if (aYIndex<LeagueMiner.HEATMAPSIZE) aTop = true;
				
				//Shifting shit just a smidge over
				aXIndex--;
				aYIndex--;
			
				
				if (aTop&&aLeft)
				{
					g.setColor(TempPicker(aHeatColorMult, m_Miner.m_HeatMap[aXIndex][aYIndex+1].m_Score));
					g.drawRect(aHeadsupX, aLoopHeadsUpY, 2, 2);
				}
				if (aTop&&aRight)
				{
					g.setColor(TempPicker(aHeatColorMult, m_Miner.m_HeatMap[aXIndex+1][aYIndex+1].m_Score));
					g.drawRect(aHeadsupX+aHeadsupSpacing, aLoopHeadsUpY, 2, 2);
				}
				if (aBot&&aLeft)
				{
					g.setColor(TempPicker(aHeatColorMult, m_Miner.m_HeatMap[aXIndex][aYIndex].m_Score));
					g.drawRect(aHeadsupX, aLoopHeadsUpY+aHeadsupSpacing, 2, 2);
				}
				if (aBot&&aRight)
				{
					g.setColor(TempPicker(aHeatColorMult, m_Miner.m_HeatMap[aXIndex+1][aYIndex].m_Score));
					g.drawRect(aHeadsupX+aHeadsupSpacing, aLoopHeadsUpY+aHeadsupSpacing, 2, 2);
				}
			}
		}
		
		
		//third panel displays the heatmap on the minimap.
		//This panel is properly positioned
		int aHeatMapX = 0;
		int aHeatMapY = (m_Framesize*2);

		int[] aCameraLoc = LeagueMiner.CoordsToMiniMap(aCoord, m_Framesize);
		g.drawRect(aCameraLoc[0], aCameraLoc[1]+aHeatMapY, 2, 2);
		
		g.setColor(new Color(255, 255, 255, 200));
		g.drawRect(aHeatMapX, aHeatMapY, m_Framesize-2, m_Framesize-2);
		for(int x=0; x<LeagueMiner.HEATMAPSIZE; x++)
		{
			for (int y=0; y<LeagueMiner.HEATMAPSIZE; y++)
			{
				int aX = aHeatMapX+m_Miner.m_HeatMap[x][y].m_ScreenCoords[0];
				int aY = aHeatMapY+m_Miner.m_HeatMap[x][y].m_ScreenCoords[1];
				
				//Choose color
				//x<-2 white, x<-1 Red, -1<x<1 white, x>1 yellow x>2 green
				g.setColor(TempPicker(aHeatColorMult, m_Miner.m_HeatMap[x][y].m_Score));

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
		if (aVal<-1)
		{
			aRet = new Color(255, 0, 0, 255);//Red
		}
		else if (aVal<-0.1)
		{  
			aRet = new Color(255,100, 0, 255);//Orange
		}
		else if (aVal<0.1)
		{  
			aRet = new Color(0,0, 0, 255);//White
		}
		else if (aVal<1)
		{
			aRet = new Color(255,255, 100, 255);//Yellow w/ blue
		}
		else if (aVal>=1)
		{
			aRet = new Color(0,255, 0, 255);//green
		}
		else
		{//Default to transparent
			aRet = new Color(0,0,0,0);
		}
		
		/*
		 * 
		else if (aVal>=-0.5&&aVal<=0.5)
		{
			aRet = new Color(255,255, 255, 255);
		}
		 */
		
		return aRet;
	}
}
