package nNS;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Household extends Agent {
	private double _reservationWage;
	private double _currentWage;
	private boolean _employeed;
	
	private Firm _employer = null;
	
	public Household(ContinuousSpace<Object> space, Grid<Object> grid, double liquidity, double reservationWage){
		super(space, grid, liquidity);
		
		this._reservationWage = reservationWage;
		this._currentWage = this._reservationWage * RandomHelper.nextDoubleFromTo(1, 2);
		_employeed = false;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		Context<Object> context = ContextUtils.getContext(this);
		List<Firm> firms = new ArrayList<Firm>();
		
		for(Object obj : _grid.getObjects()){
			if (obj instanceof Firm){
				firms.add((Firm)obj);
			}
		}
		
		if (!_employeed || _currentWage < _reservationWage){	
			if (firms.size() > 0){
				int index = RandomHelper.nextIntFromTo(0, firms.size() - 1);
				Firm firm = firms.get(index);
				
				if (firm.getWage() > _reservationWage){
					Network<Object> net = (Network<Object>)context.getProjection("employment network");
					net.addEdge(firm, this);
					
					_employer = firm;
					_employeed = true;
					_currentWage = firm.getWage();
				}
			}
		}
		
		while (tradeConnections.size() < 7){
			if (firms.size() > 0){
				int index = RandomHelper.nextIntFromTo(0, firms.size() - 1);
				Firm firm = firms.get(index);
				
				if (!tradeConnections.contains(firm)){
					Network<Object> net = (Network<Object>)context.getProjection("trade network");
					net.addEdge(this, firm);
				
					tradeConnections.add(firm);
				}
			}
		}
	}
}