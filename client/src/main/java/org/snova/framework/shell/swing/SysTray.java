/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.snova.framework.shell.swing;

import java.awt.Font;
import java.awt.Frame;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.arch.common.Pair;
import org.arch.config.IniProperties;
import org.arch.event.EventDispatcher;
import org.arch.event.NamedEventHandler;
import org.snova.framework.common.Constants;
import org.snova.framework.config.SnovaConfiguration;
import org.snova.framework.proxy.spac.SPAC;

/**
 * 
 * @author Administrator
 */
public class SysTray
{
	
	private void restoreFrame(MainFrame fm)
	{
		// fm.setAlwaysOnTop(true);
		fm.setVisible(true);
		fm.setFocusable(true);
		fm.setState(Frame.NORMAL);
		
	}
	
	private void updateProxyServiceMenus(List<Pair<JMenuItem, String>> list)
	{
		IniProperties cfg = SnovaConfiguration.getInstance().getIniProperties();
		for (Pair<JMenuItem, String> item : list)
		{
			String choice = item.second;
			if(choice.equals("SPAC")){
				choice = "Auto";
			}
			if (choice.endsWith(cfg.getProperty("SPAC", "Default")))
			{
				item.first.setIcon(ImageUtil.OK);
			}
			else
			{
				item.first.setIcon(null);
			}
		}
	}
	
	public SysTray(final MainFrame mainFrame)
	{
		final SystemTray tray = SystemTray.getSystemTray();
		final JPopupMenu popup = new JPopupMenu();
		ImageIcon icon = new ImageIcon(
		        MainFrame.class.getResource("/images/flag-16.png"));
		final TrayIcon trayIcon = new TrayIcon(icon.getImage(),
		        Constants.PROJECT_NAME);
		
		trayIcon.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				restoreFrame(mainFrame);
			}
		});
		trayIcon.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					// popup.
					popup.setLocation(e.getX(), e.getY());
					popup.setInvoker(popup);
					popup.setVisible(true);
				}
			}
		});
		
		JMenuItem item = new JMenuItem("Restore", new ImageIcon(
		        MainFrame.class.getResource("/images/flag-16.png")));
		item.setFont(new Font(null, Font.BOLD, 12));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				restoreFrame(mainFrame);
			}
		});
		popup.add(item);
		
		final JMenu servicePop = new JMenu("Services");
		final List<Pair<JMenuItem, String>> serviceMemus = new LinkedList<Pair<JMenuItem, String>>();
		String[] all = new String[] { "GAE", "C4", "SPAC" };
		for (int i = 0; i < all.length; i++)
		{
			final String handlerName = all[i];
			final JMenuItem serviceItem = new JMenuItem(all[i]);
			serviceItem.setFont(new Font(null, Font.BOLD, 12));
			serviceItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String choice = handlerName;
					if (handlerName.equalsIgnoreCase("SPAC"))
					{
						choice = "Auto";
						SPAC.spacEnbale = true;
					}else{
						SPAC.spacEnbale = false;
					}
					SnovaConfiguration.getInstance().setProxyService(choice);
					SnovaConfiguration.getInstance().save();
					updateProxyServiceMenus(serviceMemus);
				}
			});
			serviceMemus.add(new Pair<JMenuItem, String>(serviceItem,
			        handlerName));
			updateProxyServiceMenus(serviceMemus);
			servicePop.add(serviceItem);
		}
		popup.add(servicePop);
		// item = new MenuItem("View Log");
		// item.setFont(new Font(null, Font.BOLD, 12));
		// item.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// try {
		// Desktop.getDesktop().browse(AppData.getLogHome().toURI());
		// } catch (IOException ex) {
		// Logger.getLogger(SysTray.class.getName()).log(Level.SEVERE, null,
		// ex);
		// }
		// }
		// });
		// popup.add(item);
		
		item = new JMenuItem("Exit", new ImageIcon(
		        MainFrame.class.getResource("/images/exit.png")));
		
		item.setFont(new Font(null, Font.BOLD, 12));
		item.addActionListener(new ActionListener()
		{
			
			public void actionPerformed(ActionEvent e)
			{
				System.exit(1);
			}
		});
		popup.add(item);
		try
		{
			tray.add(trayIcon);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		
		trayIcon.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!mainFrame.isVisible())
				{
					mainFrame.setVisible(true);
					mainFrame.setFocusable(true);
				}
			}
		});
		
	}
}
