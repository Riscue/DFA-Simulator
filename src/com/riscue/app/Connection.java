package com.riscue.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

public class Connection extends Point {
	private static final long serialVersionUID = 450679808063844960L;
	public int r;
	public double angle;
	public boolean direction;
	public String id;
	public Color color;
	public boolean straight = false;
	public ArrayList<String> inputs;
	public String sName, dName;

	public Connection(RoundPanel s, Color c) {
		this.x = (int) (s.getBounds().getCenterX() - 50);
		this.y = (int) s.getBounds().getCenterY();
		r = 100;
		angle = Math.PI;
		color = c;
		straight = true;
		inputs = new ArrayList<String>();
		this.sName = ((RoundPanel) s).getName();
	}

	public Connection(RoundPanel s, RoundPanel d, Color c, String i, boolean b) {
		this.x = (int) (s.getBounds().getCenterX() + d.getBounds().getCenterX()) / 2;
		this.y = (int) (s.getBounds().getCenterY() + d.getBounds().getCenterY()) / 2;
		double distX = s.getBounds().getCenterX() - d.getBounds().getCenterX();
		double distY = s.getBounds().getCenterY() - d.getBounds().getCenterY();
		r = (int) Math.sqrt(distX * distX + distY * distY);
		angle = Math.atan2(distY, distX);
		color = c;
		straight = b;
		inputs = new ArrayList<String>(Arrays.asList(new String[] { i }));
		this.sName = ((RoundPanel) s).getName();
		this.dName = ((RoundPanel) d).getName();
	}

	public void recalculate(Component[] comps) {
		Component s = null, d = null;
		for (Component component : comps) {
			if (component.getClass() != RoundPanel.class)
				continue;
			if (((RoundPanel) component).getName() == sName) {
				s = component;
			}
			if (((RoundPanel) component).getName() == dName) {
				d = component;
			}
		}
		if (s == null)
			return;
		if (d == null) {
			this.x = (int) (s.getBounds().getCenterX() - 50);
			this.y = (int) s.getBounds().getCenterY();
		} else {
			this.x = (int) (s.getBounds().getCenterX() + d.getBounds().getCenterX()) / 2;
			this.y = (int) (s.getBounds().getCenterY() + d.getBounds().getCenterY()) / 2;
			double distX = s.getBounds().getCenterX() - d.getBounds().getCenterX();
			double distY = s.getBounds().getCenterY() - d.getBounds().getCenterY();
			r = (int) Math.sqrt(distX * distX + distY * distY);
			angle = Math.atan2(distY, distX);
		}
	}

	public boolean findSource(String name) {
		return sName == name;
	}

	public boolean find(String name) {
		return sName == name || dName == name;
	}

	public boolean find(String sName, String dName) {
		return this.sName == sName && this.dName == dName;
	}

	public boolean find(String sName, String dName, String input) {
		return this.sName == sName && this.dName == dName && inputs.contains(input);
	}

	public void addInput(String i) {
		inputs.add(i);
	}

	public void removeInput(String input) {
		for (int i = 0; i < inputs.size(); i++) {
			if (input.equals(inputs.get(i))) {
				inputs.remove(i);
			}
		}
	}
}