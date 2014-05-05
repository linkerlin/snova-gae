/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PluginConfigDialog.java
 *
 * Created on 2010-8-21, 20:05:14
 */
package org.snova.framework.shell.swing;

/**
 * 
 * @author wqy
 */
public class PluginConfigDialog extends javax.swing.JDialog
{

	/** Creates new form PluginConfigDialog */
	public PluginConfigDialog(java.awt.Frame parent, ProxyGUIHolder plugin)
	{
		super(parent, true);
		this.plugin = plugin;
		this.pluginConfigPanel = plugin.getConfigPanel();
		myInitComponents();
	}

	private void myInitComponents()
	{
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(plugin.getName());
		setAlwaysOnTop(true);
		setLocationByPlatform(true);
		add(pluginConfigPanel);
		setSize(pluginConfigPanel.getSize());
		pack();
		setResizable(false);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{

		jPanel1 = this.pluginConfigPanel;

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(plugin.getName());
		setAlwaysOnTop(true);
		setLocationByPlatform(true);
		setResizable(false);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
		        jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(
		        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100,
		        Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(
		        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100,
		        Short.MAX_VALUE));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
		        getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
		        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		        .addGap(0, 400, Short.MAX_VALUE)
		        .addGroup(
		                layout.createParallelGroup(
		                        javax.swing.GroupLayout.Alignment.LEADING)
		                        .addGroup(
		                                layout.createSequentialGroup()
		                                        .addGap(0, 150, Short.MAX_VALUE)
		                                        .addComponent(
		                                                jPanel1,
		                                                javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                javax.swing.GroupLayout.DEFAULT_SIZE,
		                                                javax.swing.GroupLayout.PREFERRED_SIZE)
		                                        .addGap(0, 150, Short.MAX_VALUE))));
		layout.setVerticalGroup(layout
		        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		        .addGap(0, 300, Short.MAX_VALUE)
		        .addGroup(
		                layout.createParallelGroup(
		                        javax.swing.GroupLayout.Alignment.LEADING)
		                        .addGroup(
		                                layout.createSequentialGroup()
		                                        .addGap(0, 100, Short.MAX_VALUE)
		                                        .addComponent(
		                                                jPanel1,
		                                                javax.swing.GroupLayout.PREFERRED_SIZE,
		                                                javax.swing.GroupLayout.DEFAULT_SIZE,
		                                                javax.swing.GroupLayout.PREFERRED_SIZE)
		                                        .addGap(0, 100, Short.MAX_VALUE))));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private ProxyGUIHolder plugin;
	private javax.swing.JPanel pluginConfigPanel;
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	// End of variables declaration//GEN-END:variables
}
