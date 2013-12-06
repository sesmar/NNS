package nNS;

import java.util.ArrayList;
import java.util.List;

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
	public static List<Firm> firms = new ArrayList<Firm>();
	public static List<Household> households = new ArrayList<Household>();
	
	public static int NumberOfFirms = 20;
	public static int NumberOfHouseholds = 200;
	public static int MaxNumberOfTradeConnections = 5;
	public static int Productivity = 3;
	public static int PeriodLength = 15;
	
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
		
		StatsAgent sa = new StatsAgent(space, grid, 0);
		context.add(sa);
		
		for(int i = 0; i <= NumberOfFirms; i++){
			Firm f =  new Firm(space, grid, RandomHelper.nextDoubleFromTo(2000, 2500), RandomHelper.nextDoubleFromTo(10, 25));
			context.add(f);
			firms.add(f);
		}
		
		for(int i = 0; i <= NumberOfHouseholds; i++){
			Household hh = new Household(space, grid, RandomHelper.nextDoubleFromTo(450, 500), RandomHelper.nextDoubleFromTo(10, 20)); 
			context.add(hh);
			households.add(hh);
		}
		
		for (Object obj : context){
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
		}
		
		for(Household hh : households){
			hh.seekEmployment(firms, context);
			hh.determineTradeConnections(firms, context);
		}
		
		return context;
	}
	
	public static double unemploymentRate(){
		double unemployedCount = 0;
		
		for(Household hh : households){
			if (!hh.isEmployed()){
				unemployedCount++;
			}
		}
		
		return (unemployedCount / households.size()) * 100;
	}
}