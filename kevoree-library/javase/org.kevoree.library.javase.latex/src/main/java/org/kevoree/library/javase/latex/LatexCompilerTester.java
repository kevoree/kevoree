/*
package org.kevoree.library.javase.latex;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

*/
/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/11/11
 * Time: 20:51
 *
 * @author Erwan Daubert
 * @version 1.0
 *//*


@Provides({
		@ProvidedPort(name = "callback", type = PortType.MESSAGE)
})
@Requires({
		@RequiredPort(name = "compile", type = PortType.MESSAGE, optional = true),
		@RequiredPort(name = "clean", type = PortType.MESSAGE, optional = true)
})
@Library(name = "JavaSE")
@ComponentType
public class LatexCompilerTester extends AbstractComponentType {
	private MyFrame frame = null;

	@Start
	public void start () {
		frame = new MyFrame("compile", "clean");
		frame.setVisible(true);
	}

	@Stop
	public void stop () {
		frame.dispose();
		frame = null;
	}

	@Update
	public void update () {
	}

	@Port(name = "callback")
	public void callback (Object message) {
		if (message instanceof StdKevoreeMessage) {
			for (String key : ((StdKevoreeMessage) message).getKeys()) {
				System.out.println(key + ":" + ((StdKevoreeMessage) message).getValue(key));
			}
		}
	}

	private class MyFrame extends JFrame {

		private JButton on, off;
		private String onText;
		private String offText;
//		private JTextPane screen;
//		private JTextArea inputTextField;

		public MyFrame (final String onText, final String offText) {

			this.onText = onText;
			this.offText = offText;
			//setPreferredSize(new Dimension(SWITCH_WIDTH, SWITCH_HEIGHT));
			//setLayout(new FlowLayout());
			on = new JButton(onText);
			on.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed (ActionEvent e) {
					if (isPortBinded("compile")) {
						StdKevoreeMessage msg = new StdKevoreeMessage();
						msg.putValue("id", UUID.randomUUID());
						msg.putValue("file", "CCGrid2012.tex");
						msg.putValue("folder", "/home/edaubert/Documents/these/articles/CCGrid2012");
						getPortByName("compile", MessagePort.class).process(msg);
					}
				}
			});

			off = new JButton(offText);
			off.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed (ActionEvent e) {
					if (isPortBinded("clean")) {
						StdKevoreeMessage msg = new StdKevoreeMessage();
						msg.putValue("id", UUID.randomUUID());
						msg.putValue("file", "CCGrid2012.tex");
						msg.putValue("folder", "/home/edaubert/Documents/these/articles/CCGrid2012");
						getPortByName("clean", MessagePort.class).process(msg);
					}
				}
			});
			ButtonGroup bg = new ButtonGroup();
			bg.add(on);
			bg.add(off);


			*/
/*setPreferredSize(new Dimension(600, 800));
			setLayout(new BorderLayout());

			screen = new JTextPane();
			screen.setFocusable(false);
			screen.setEditable(false);
			StyledDocument doc = screen.getStyledDocument();
			Style def = StyleContext.getDefaultStyleContext().
					getStyle(StyleContext.DEFAULT_STYLE);
			Style system = doc.addStyle("system", def);
			StyleConstants.setForeground(system, Color.GRAY);

			Style incoming = doc.addStyle("incoming", def);
			StyleConstants.setForeground(incoming, Color.BLUE);

			Style outgoing = doc.addStyle("outgoing", def);
			StyleConstants.setForeground(outgoing, Color.GREEN);*//*



			setLayout(new FlowLayout());
			add(on);
			add(off);
//			add(screen);

			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			pack();
			setVisible(true);
		}

		@Override
		public void repaint () {
			on.setText(onText);
			off.setText(offText);
			super.repaint();
		}

		*/
/**
		 * @param onText the onText to set
		 *//*

		public final void setOnText (String onText) {
			this.onText = onText;
		}

		*/
/**
		 * @param offText the offText to set
		 *//*

		public final void setOffText (String offText) {
			this.offText = offText;
		}
	}
}
*/
