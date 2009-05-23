package com.moneybender.snarf.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AboutDialog extends JDialog {
		private static Logger log = Logger.getLogger(AboutDialog.class);

		public AboutDialog(Frame owner) {
			FormLayout layout = new FormLayout("center:200dlu",
				"2dlu, center:pref, 1dlu, center:pref, 10dlu, center:pref, 1dlu, center:pref, 10dlu, center:pref, 1dlu, center:pref");
			CellConstraints cc = new CellConstraints();
			layout.setRowGroups(new int[][]{{2, 4, 6, 8, 10, 12}});
			
			JPanel panel = new JPanel(layout);

			panel.add(new JLabel("SnarfNOAA, by Don Branson"), cc.xy (1, 2));
			panel.add(new JLabel("http://moneybender.com"), cc.xy (1, 4));

			panel.add(new JLabel("Uses JGoodlies Looks and Forms"), cc.xy (1, 6));
			panel.add(new JLabel("http://www.jgoodies.com"), cc.xy (1, 8));
			
			panel.add(new JLabel("Uses OpenMap"), cc.xy (1, 10));
			panel.add(new JLabel("http://openmap.bbn.com"), cc.xy (1, 12));
			
			
		    add(panel);
		    setTitle("About");
		    setSize(300, 160);
		    locateOnScreen(this);
		    setVisible(true);
		}

	    private void locateOnScreen(Dialog dialog) {
	        Dimension paneSize   = dialog.getSize();
	        Dimension screenSize = dialog.getToolkit().getScreenSize();
	        dialog.setLocation(
	            (screenSize.width  - paneSize.width)  / 2,
	            (screenSize.height - paneSize.height) / 2);
	    }

}
