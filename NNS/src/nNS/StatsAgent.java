package nNS;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class StatsAgent extends Agent{

	public StatsAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double liquidity) {
		super(space, grid, liquidity);
		// TODO Auto-generated constructor stub
	}
	
	public double unemploymentRate(){
		return NNSBuilder.unemploymentRate();
	}
}
