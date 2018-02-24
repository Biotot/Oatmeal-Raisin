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
	public int rectX = 50;
	public int rectY = 50;
	public int width = 100;
	public int height = 50;
	
	private static LeagueMiner m_Miner;
	
	public LeagueChampHUD() {
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
				System.out.println("TOCK" + System.currentTimeMillis());
			}
		}).start();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(1920, 500);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(new Color(255, 255, 255, 200));
		
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
		
		int aHeatMapX = 0;
		int aHeatMapY = 200;
		for(int x=0; x<m_Miner.HEATMAPSIZE; x++)
		{
			for (int y=0; y<m_Miner.HEATMAPSIZE; y++)
			{
				int aX = aHeatMapX+(x*25);
				int aY = aHeatMapY+(250-(y*25));
				g.drawString(String.format("%.1f",m_Miner.m_HeatMap[x][y].m_Score), aX, aY);
				//System.out.print(String.format("%.1f,",m_Miner.m_HeatMap[x][y].m_Score));
				
			}
		}
		
		//System.out.println("TICK");
		g.dispose();
	}
}
