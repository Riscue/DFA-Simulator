package com.riscue.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

public class WorkingArea extends JPanel {
	private static final long serialVersionUID = 3344023255656230669L;
	private ArrayList<Connection> connections = new ArrayList<>();
	private boolean locked;
	private JPanel selectedState;
	private Connection selectedConnection;

	public void lockScreen() {
		locked = true;
	}

	public void unlockScreen() {
		locked = false;
		selectedState.setVisible(false);
		setSelectedConnection(null);
	}

	private class DragListener extends MouseInputAdapter {
		Point location;
		MouseEvent pressed;

		public void mousePressed(MouseEvent me) {
			if (locked)
				return;
			pressed = me;
		}

		public void mouseDragged(MouseEvent me) {
			if (locked)
				return;
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
			Component component = me.getComponent();
			location = component.getLocation(location);
			int x = location.x - pressed.getX() + me.getX();
			int y = location.y - pressed.getY() + me.getY();
			component.setLocation(x, y);
			moveConnections(((RoundPanel) component).getName());
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if (locked)
				return;
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (locked)
				return;
			if (e.getClickCount() == 2) {
				removeState((Component) e.getSource());
			}
		}
	}

	private void moveConnections(String name) {
		for (Connection connection : connections) {
			if (connection.find(name)) {
				connection.recalculate(getComponents());
				repaint();
			}
		}
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawConnections(g);
	}

	private void drawConnections(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (Connection connection : connections) {
			g2.setColor(selectedConnection == connection ? Color.RED : connection.color);
			g2.translate(connection.x, connection.y);
			g2.rotate(connection.angle);
			g2.setStroke(new BasicStroke(2));
			if (connection.straight) {
				g2.draw(new Line2D.Double(-connection.r / 2, 0, connection.r / 2, 0));
			} else {
				if (connection.r == 0)
					g2.draw(new Arc2D.Double(-30, -120, 60, 120, 0, 360, Arc2D.OPEN));
				else
					g2.draw(new Arc2D.Double(-connection.r / 2, -50, connection.r, 100, 0, -180, Arc2D.OPEN));
			}
			drawArrow(g2, connection);
			g2.rotate(-connection.angle);
			drawString(g2, connection);
			g2.translate(-connection.x, -connection.y);
		}
	}

	private void drawString(Graphics2D g2, Connection connection) {
		g2.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize() + 5));
		ArrayList<String> inputs = connection.inputs;
		String label = "";

		for (String key : inputs) {
			label += key + ", ";
		}
		label = label.length() > 1 ? label.substring(0, label.length() - 2) : label;
		if (connection.straight) {
			g2.drawString(label, 0, -10);
		} else {
			if (connection.r == 0) {
				g2.drawString(label, -15, -130);
			} else {
				g2.drawString(label, (int) (-70 * Math.sin(connection.angle)), (int) (70 * Math.cos(connection.angle)));
			}
		}
	}

	private void drawArrow(Graphics2D g2, Connection connection) {
		int y = 21;
		int x = -connection.r / 2 + y / 2;
		int angle = 30;

		if (connection.straight) {
			x = -connection.r / 2 + 23;
			y = 0;
			angle = 0;
		} else if (connection.r == 0) {
			y = -15;
			x = 19;
			angle = -55;
		}

		g2.translate(x, y);
		g2.rotate(Math.toRadians(angle));
		g2.fillPolygon(new int[] { 0, 10, 10 }, new int[] { 0, -10, 10 }, 3);
		g2.rotate(-Math.toRadians(angle));
		g2.translate(-x, -y);
	}

	public WorkingArea() {
		selectedState = new JPanel() {
			private static final long serialVersionUID = 8822662860881484315L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				graphics.setColor(Color.RED);
				graphics.setStroke(new BasicStroke(5));
				graphics.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, getHeight(), getHeight());
			}
		};
		selectedState.setSize(50, 50);
		selectedState.setVisible(false);
		add(selectedState);
		setLayout(null);
		setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	public void removeAll() {
		if (JOptionPane.showConfirmDialog(null, "Would you like to clear screen?", "Warning",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			connections.clear();
			super.removeAll();
		}
	}

	private void removeState(Component state) {
		state.getParent().remove(state);
		removeConnection(((RoundPanel) state).getName());
		repaint();
	}

	private void removeConnection(String name) {
		for (int i = connections.size() - 1; i >= 0; i--) {
			if (connections.get(i).find(name)) {
				connections.remove(i);
				repaint();
			}
		}
	}

	public void removeConnection(String sName, String dName, String input) {

		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i).find(sName, dName, input)) {
				connections.get(i).removeInput(input);
				if (connections.get(i).inputs.size() == 0) {
					connections.remove(i);
				}
				repaint();
			}
		}
	}

	public void addState(int x, int y, String name, boolean f, boolean l) {
		RoundPanel state = new RoundPanel(x, y, name, f, l);
		DragListener drag = new DragListener();
		state.addMouseListener(drag);
		state.addMouseMotionListener(drag);
		add(state);
	}

	private RoundPanel findComponent(String name) {
		for (Component comp : getComponents()) {
			if (comp.getClass() != RoundPanel.class)
				continue;
			if (name.equals(comp.getName()))
				return (RoundPanel) comp;
		}
		return null;
	}

	public void addConnection(String s) {
		connections.add(new Connection(findComponent(s), Color.BLACK));
	}

	public void addConnection(String s, Color c) {
		connections.add(new Connection(findComponent(s), c));
	}

	public void addConnection(String s, String d, Color c, String i, boolean b) {
		for (Connection connection : connections) {
			if (connection.find(s, d)) {
				connection.addInput(i);
				return;
			}
		}
		connections.add(new Connection(findComponent(s), findComponent(d), c, i, b));
	}

	public void addConnection(String s, String d, Color c, String i) {
		connections.add(new Connection(findComponent(s), findComponent(d), c, i, false));
	}

	public Map<String, String> getPaths(String name) {
		Map<String, String> paths = new HashMap<String, String>();
		for (Connection connection : connections) {
			if (connection.dName == null)
				continue;
			if (connection.findSource(name)) {
				for (String input : connection.inputs) {
					if (paths.keySet().contains(input))
						return null;
					paths.put(input, connection.dName);
				}
			}
		}
		return paths;
	}

	public Connection findStartConnection() {
		for (Connection connection : connections) {
			if (connection.straight && connection.dName == null)
				return connection;
		}
		return null;
	}

	public Connection findConnection(String state, String character) {
		for (Connection connection : connections) {
			if (connection.dName == null || !connection.sName.equals(state))
				continue;
			if (connection.inputs.contains(character))
				return connection;
		}
		return null;
	}

	public void setSelectedState(String state) {
		RoundPanel comp = findComponent(state);
		selectedState.setVisible(true);
		selectedState.setLocation(comp.getLocation());
	}

	public void setSelectedConnection(Connection connection) {
		selectedConnection = connection;
	}

	public boolean findConnection(String sName, String dName, String input) {
		for (Connection connection : connections) {
			if (connection.find(sName, dName, input))
				return true;
		}
		return false;
	}

	public RoundPanel findState(String name) {
		Component[] comps = getComponents();
		for (Component component : comps) {
			if (component.getClass() != RoundPanel.class)
				continue;
			if (name.equals(component.getName())) {
				return (RoundPanel) component;
			}
		}
		return null;
	}
}