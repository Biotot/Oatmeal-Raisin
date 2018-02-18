package ScreenWriterUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class StaticTextPane extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int PREF_W = 300;
	private static final int PREF_H = 200;
	private static final int TIMER_DELAY = 100;
	public int rectX = 100;
	public int rectY = 100;
	public int width = 80;
	public int height = 100;
	
	private static final String STATIC_TEXT = "Panel-Level Static Text";
	private static JLabel movingJLabel; 
	
	public StaticTextPane() {
		movingJLabel = new JLabel(STATIC_TEXT);
		movingJLabel.setForeground(new Color(244, 18, 244, 100));
		setOpaque(false);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREF_W, PREF_H);
	}
}
