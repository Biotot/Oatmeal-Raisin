package ScreenWriterUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class MovingShapePane extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int PREF_W = 900;
	private static final int PREF_H = 700;
	private static final int TIMER_DELAY = 100;
	public int rectX = 100;
	public int rectY = 100;
	public int width = 80;
	public int height = 100;

	public MovingShapePane() {
		setOpaque(false);
		setLayout(new GridBagLayout());

		new Timer(TIMER_DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actEvt) {
				if (rectX < PREF_W && rectY < PREF_H) {
					rectX++;
					rectY++;
					repaint();
				} else {
					((Timer)actEvt.getSource()).stop();
				}
			}
		}).start();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREF_W, PREF_H);
	}

	@Override
	protected void paintComponent(Graphics g) {
		for (int i = 0; i < 100; i++) {
			g.setColor(new Color(122, 122, 122, 200));
			g.drawRect(rectX + i, rectY + i, width, height);
			g.fillRect(rectX,  rectY,  width,  height);
			g.setColor(new Color(255, 255, 255));
			g.drawString("YOLOOO", rectX + 10 + i, rectY + 10 + i);
		}
		g.dispose();
	}
}
