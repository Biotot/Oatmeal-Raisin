package ScreenWriterUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;

import adapters.User32;

public class ScreenWriterPlayground {
	public static void main(String[] args) {
		new ScreenWriterPlayground();
	}
	
	public ScreenWriterPlayground() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
					ex.printStackTrace();
				}
				
				Window frame = new Window(null);
				//frame.setUndecorated(true);
				frame.setBackground(new Color (0,0,0,0));
				//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(new MovingShapePane());
				
				frame.setAlwaysOnTop(true);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				
				
				JLabel label = new JLabel("WINDOW-LEVEL LABEL");
				label.setFont(new Font("", Font.BOLD, 69));
				label.setForeground(new Color(12, 23, 255, 190));
				frame.add(label);
				
				setTransparent(frame);
			}
			
			private HWND getHWnd(Component w) {
				HWND hwnd = new HWND();
				hwnd.setPointer(Native.getComponentPointer(w));
				return hwnd;
			}
			
			private void setTransparent(Component w) {
				WinDef.HWND hwnd = getHWnd(w);
				int wl = User32.INSTANCE.GetWindowLong(hwnd.getPointer(), WinUser.GWL_EXSTYLE);
				wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
				User32.INSTANCE.SetWindowLong(hwnd.getPointer(), WinUser.GWL_EXSTYLE, wl);
			}
		});
	}
}
