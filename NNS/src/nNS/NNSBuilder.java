package nNS;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class NNSBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("NNS");
		
		NetworkBuilder<Object> netEmploymentBuilder = new NetworkBuilder<Object>("employment network", context, true);
		netEmploymentBuilder.buildNetwork();
		
		NetworkBuilder<Object> netTradeBuilder = new NetworkBuilder<Object>("trade network", context, true);
		netTradeBuilder.buildNetwork();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", 
				context, 
				new RandomCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.WrapAroundBorders(),  500, 500);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid(
				"grid", 
				context,
				new GridBuilderParameters<Object>(
					new WrapAroundBorders(),
					new SimpleGridAdder<Object>(),
					true, 500, 500)
				);
		
		int firmCount = 20;
		
		for(int i = 0; i <= firmCount; i++){
			context.add(new Firm(space, grid, RandomHelper.nextDoubleFromTo(100, 500), RandomHelper.nextDoubleFromTo(10, 40)));
		}
		
		int householdCount = 200;
		
		for(int i = 0; i <= householdCount; i++){
			context.add(new Household(space, grid, RandomHelper.nextDoubleFromTo(100, 300), RandomHelper.nextDoubleFromTo(5, 20)));
		}
		
		for (Object obj : context){
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
		}
		
		return context;
	}
}