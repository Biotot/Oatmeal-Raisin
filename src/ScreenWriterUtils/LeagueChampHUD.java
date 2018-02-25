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
	private static final int TIMER_DELAY = 100;
	private int m_Framesize = 500;
	public int rectX = 50;
	public int rectY = 50;
	public int width = 100;
	public int height = 50;
	
	private static LeagueMiner m_Miner;
	
	public LeagueChampHUD(int tMapSize) {
		m_Framesize = tMapSize;
		this.setMinimumSize(getPreferredSize());
		setOpaque(false);
		setLayout(new GridBagLayout());
		m_Miner = new LeagueMiner();
		
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
		return new Dimension(m_Framesize, m_Framesize);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(new Color(255, 255, 255, 200));
		
		/*
		for (int x = 0; x < m_Miner.m_ChampList.size(); x++) {
			int aXCoord = rectX + (x*width);
			Unit aUnit = m_Miner.m_ChampList.get(x);
			
			g.drawRect(aXCoord, rectY, width, height);
			//g.fillRect(rectX,  rectY,  width,  height);
			g.drawString(aUnit.m_ChampName, aXCoord+5, rectY+10);
			g.drawString(aUnit.m_PlayerName, aXCoord+5, rectY+20);
			g.drawString(""+aUnit.m_HPC, aXCoord+5, rectY+30);
			g.drawString(""+aUnit.m_DistanceToUser, aXCoord+5, rectY+40);
		}
		float[] aCoord = m_Miner.GetScreenCoords();

		String aCoordLabel = aCoord[0] +"," + aCoord[1] + "," + aCoord[2];
		g.drawString(aCoordLabel, rectX, rectY+75);
		*/
		
		int aHeatMapX = 25;
		int aHeatMapY = -25;
		int aHeatMapDistance = m_Framesize/LeagueMiner.HEATMAPSIZE;
		double aMult = 0.75;
		for(int x=0; x<LeagueMiner.HEATMAPSIZE; x++)
		{
			for (int y=0; y<LeagueMiner.HEATMAPSIZE; y++)
			{
				int aX = aHeatMapX+(x*aHeatMapDistance);
				int aY = aHeatMapY+(m_Framesize-(y*aHeatMapDistance));
				
				double aVal = m_Miner.m_HeatMap[x][y].m_Score/aMult;
				//Choose color
				//x<-2 white, x<-1 Red, -1<x<1 white, x>1 yellow x>2 green
				if (aVal<-2)
				{
					g.setColor(new Color(255, 0, 0, 255));
				}
				else if (aVal<-1)
				{  
					g.setColor(new Color(255,165, 0, 255));
				}
				else if (aVal>-1&&aVal<1)
				{
					g.setColor(new Color(255,255, 255, 255));
				}
				else if (aVal>2)
				{
					g.setColor(new Color(0,255, 0, 255));
				}
				else if (aVal>1)
				{
					g.setColor(new Color(255,255, 0, 255));
				}

				g.drawRect(aX, aY, 3, 3);
				//g.drawString(String.format("%.1f",m_Miner.m_HeatMap[x][y].m_Score), aX, aY);
				//System.out.print(String.format("%.1f,",m_Miner.m_HeatMap[x][y].m_Score));
				
			}
		}
		
		//System.out.println("TICK");
		g.dispose();
	}
}
