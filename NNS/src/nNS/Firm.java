package nNS;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Firm extends Agent {
	
	private double _inventory;
	private double _price;
	private double _reservationPrice;
	private double _wageRate;
	private double _markup;
	private double _productivity = 1.125;
	private List<Household> _employees = new ArrayList<Household>();
	
	public Firm(ContinuousSpace<Object> space, Grid<Object> grid, double liquidity, double reservationPrice) {
		super(space, grid, liquidity);
		
		this._reservationPrice = reservationPrice;
		this._price = this._reservationPrice * RandomHelper.nextDoubleFromTo(1, 2);
		this._markup = RandomHelper.nextDoubleFromTo(.001, .5);
		
		this._wageRate = this._price - (this._price * this._markup);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		double tickCount = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		if (tickCount % 30 == 0){
			payEmployees();
			produceGoods();
		}
	}
	
	protected void payEmployees(){
		for(Household hh : _employees){
			if (_liquidity > _wageRate){
				hh.receiveIncome(_wageRate);
				_liquidity -= _wageRate;
			}
		}
	}
	
	protected void produceGoods(){
		_inventory += (_productivity * _employees.size());
	}
	
	public double getWage(){
		return _wageRate;
	}
	
	public double getPrice(){
		return _price;
	}
	
	public boolean hireEmployee(Household employee){
		if (!_employees.contains(employee)){
			_employees.add(employee);
			return true;
		}
		
		return false;
	}
	
	public boolean purchaseGood(){
		if (_inventory > 0){
			this.receiveIncome(_price);
			_inventory--;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString(){
		return "Firm";
	}
}