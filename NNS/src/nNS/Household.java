package nNS;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
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
	private RepastEdge<Object> _employmentEdge = null;
	
	protected List<TradeConnection> tradeConnections = new ArrayList<TradeConnection>();
	protected List<Firm> tradingFirms = new ArrayList<Firm>();
	
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
		
		if (tickCount % NNSBuilder.PeriodLength == 0){
			determineTradeConnections(firms, context);
		}
		
		seekEmployment(hiringFirms, context);
		purchaseGoods(context);
	}
	
	public void seekEmployment(List<Firm> firms, Context<Object> context){
		if (!_employed || _currentWage < _reservationWage){	
			if (firms.size() > 0){
				int index = RandomHelper.nextIntFromTo(0, firms.size() - 1);
				Firm firm = firms.get(index);
				
				if (!_employed || firm.getWage() > _reservationWage){
					Network<Object> net = (Network<Object>)context.getProjection("employment network");
					_employmentEdge = net.addEdge(firm, this);
					
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
		while (tradeConnections.size() < NNSBuilder.MaxNumberOfTradeConnections){
			if (firms.size() > 0){
				int index = RandomHelper.nextIntFromTo(0, firms.size() - 1);
				Firm firm = firms.get(index);
				
				if (!tradingFirms.contains(firm)){
					TradeConnection tradeConnection = new TradeConnection();
					Network<Object> net = (Network<Object>)context.getProjection("trade network");
					
					tradeConnection.edge = net.addEdge(this, firm);					
					tradeConnection.firm = firm;

					tradingFirms.add(firm);
					tradeConnections.add(tradeConnection);
				}
			}
		}
	}
	
	protected void purchaseGoods(Context<Object> context){
		int numberOfGoods = RandomHelper.nextIntFromTo(0, NNSBuilder.DemandForGoods);
		
		for(int i = 0; i < numberOfGoods; i++){
			if (tradeConnections.size() > 0){
				int index = RandomHelper.nextIntFromTo(0, tradeConnections.size() - 1);
				TradeConnection tc = tradeConnections.get(index); 
				Firm firm = tc.firm;
			
				if(_liquidity > firm.getPrice()){
					if (firm.purchaseGood()){
						_liquidity -= firm.getPrice();
					} else {
						Network<Object> net = (Network<Object>)context.getProjection("trade network");
						net.removeEdge(tc.edge);
						tradeConnections.remove(tc);
						tradingFirms.remove(firm);
					}
				}
			}
		}
	}
	
	public void fire(){
		if (_employmentEdge != null){
			Context<Object> context = ContextUtils.getContext(this);
			
			Network<Object> net = (Network<Object>)context.getProjection("employment network");
			net.removeEdge(_employmentEdge);
			_employmentEdge = null;
		}
		
		_employer = null;
		_employed = false;
		_currentWage = 0;
	}
	
	@Override
	public String toString(){
		return "Household";
	}
	
	public boolean isEmployed(){
		return _employed;
	}
	
	public double getWage(){
		return _currentWage;
	}
}