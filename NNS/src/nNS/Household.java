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
import repast.simphony.engine.environment.RunEnvironment;

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
		double tickCount = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		Context<Object> context = ContextUtils.getContext(this);
		List<Firm> firms = new ArrayList<Firm>();
		
		for(Object obj : _grid.getObjects()){
			if (obj instanceof Firm){
				firms.add((Firm)obj);
			}
		}
		
		if (tickCount % 30 == 0 || tickCount == 1){
			seekEmployment(firms, context);
		
			determineTradeConnections(firms, context);
		}
		
		purchaseGoods();
	}
	
	protected void seekEmployment(List<Firm> firms, Context<Object> context){
		if (!_employeed || _currentWage < _reservationWage){	
			if (firms.size() > 0){
				int index = RandomHelper.nextIntFromTo(0, firms.size() - 1);
				Firm firm = firms.get(index);
				
				if (firm.getWage() > _reservationWage){
					Network<Object> net = (Network<Object>)context.getProjection("employment network");
					net.addEdge(firm, this);
					
					if (firm.hireEmployee(this))
					{
						_employer = firm;
						_employeed = true;
						_currentWage = firm.getWage();
					}
				}
			}
		}
	}
	
	protected void determineTradeConnections(List<Firm> firms, Context<Object> context){
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
	
	protected void purchaseGoods(){
		int index = RandomHelper.nextIntFromTo(0, tradeConnections.size() - 1);
		Firm firm = (Firm)tradeConnections.get(index);
		
		if(_liquidity > firm.getPrice()){
			if (firm.purchaseGood()){
				_liquidity -= firm.getPrice();
			}
		}
	}
	
	@Override
	public String toString(){
		return "Household";
	}
}