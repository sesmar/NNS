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
	private boolean _employed;
	
	private Firm _employer = null;
	
	public Household(ContinuousSpace<Object> space, Grid<Object> grid, double liquidity, double reservationWage){
		super(space, grid, liquidity);
		
		this._reservationWage = reservationWage;
		this._currentWage = 0;
		_employed = false;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		double tickCount = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		Context<Object> context = ContextUtils.getContext(this);
		List<Firm> firms = new ArrayList<Firm>();
		List<Firm> hiringFirms = new ArrayList<Firm>();
		
		for(Object obj : _grid.getObjects()){
			if (obj instanceof Firm){
				if (((Firm)obj).isHiring()){
					hiringFirms.add((Firm)obj);
				}
				
				firms.add((Firm)obj);
			}
		}
		
		if (tickCount % 30 == 0){
			seekEmployment(hiringFirms, context);
			determineTradeConnections(firms, context);
		}
		
		purchaseGoods();
	}
	
	public void seekEmployment(List<Firm> firms, Context<Object> context){
		if (!_employed || _currentWage < _reservationWage){	
			if (firms.size() > 0){
				int index = RandomHelper.nextIntFromTo(0, firms.size() - 1);
				Firm firm = firms.get(index);
				
				if (!_employed || firm.getWage() > _reservationWage){
					Network<Object> net = (Network<Object>)context.getProjection("employment network");
					net.addEdge(firm, this);
					
					if (firm.hireEmployee(this))
					{
						_employer = firm;
						_employed = true;
						_currentWage = firm.getWage();
					}
				}
			}
		}
	}
	
	public void determineTradeConnections(List<Firm> firms, Context<Object> context){
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
			} else {
				tradeConnections.remove(firm);
			}
		}
	}
	
	@Override
	public String toString(){
		return "Household";
	}
}