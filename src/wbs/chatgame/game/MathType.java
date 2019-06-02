package wbs.chatgame.game;

import java.util.HashMap;

public class MathType {
	
	public double chance;
	public int maxValue;
	public int min;
	public int max;
	public boolean addition = true;
	public boolean subtraction= true;
	public boolean multiplication= false;
	public boolean division= false;

	
	public MathType(double chance, int maxValue, int min, int max, boolean addition, boolean subtraction, boolean multiplication, boolean division) {
		this.chance = chance;
		this.maxValue = maxValue;
		this.min = min;
		this.max = max;
		this.addition = addition;
		this.subtraction = subtraction;
		this.multiplication = multiplication;
		this.division = division;
	}
}
