package nNS;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public abstract class Agent {
	protected ContinuousSpace<Object> _space;
	protected Grid<Object> _grid;
	
	protected double _liquidity;
	protected List<Agent> tradeConnections = new ArrayList<Agent>();
	
	public Agent(ContinuousSpace<Object> space, Grid<Object> grid, double liquidity){
		_space = space;
		_grid = grid;
		_liquidity = liquidity;
	}
	
	public void receiveIncome(double incomeAmount){
		_liquidity += incomeAmount;
	}
	
	public double getLiquidity(){
		return _liquidity;
	}
}
