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
	private static final int PREF_W = 900;
	private static final int PREF_H = 700;
	private static final int TIMER_DELAY = 100;
	public int rectX = 50;
	public int rectY = 50;
	public int width = 100;
	public int height = 50;
	
	private static LeagueMiner m_Miner;
	
	public LeagueChampHUD() {
		setOpaque(false);
		setLayout(new GridBagLayout());
		m_Miner = new LeagueMiner();
		
		new Timer(TIMER_DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actEvt) {

				m_Miner.UpdatePlayerListPrimary();
				m_Miner.UpdatePlayerListSecondary();
				repaint();
			}
		}).start();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREF_W, PREF_H);
	}

	@Override
	protected void paintComponent(Graphics g) {

		g.drawString("TESTING", 0, 0);
		for (int x = 0; x < m_Miner.m_ChampList.size(); x++) {
			int aXCoord = rectX + (x*width);
			Unit aUnit = m_Miner.m_ChampList.get(x);
			g.setColor(new Color(122, 122, 122, 200));
			g.drawRect(aXCoord, rectY, width, height);
			//g.fillRect(rectX,  rectY,  width,  height);
			g.drawString(aUnit.m_ChampName, aXCoord+5, rectY+10);
			g.drawString(aUnit.m_PlayerName, aXCoord+5, rectY+20);
			g.drawString(""+aUnit.m_HPC, aXCoord+5, rectY+30);
			g.drawString(""+aUnit.m_DistanceToUser, aXCoord+5, rectY+40);
			g.setColor(new Color(255, 255, 255));
		}
		g.dispose();
	}
}
