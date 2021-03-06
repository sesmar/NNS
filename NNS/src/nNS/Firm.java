package nNS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Firm extends Agent {
	
	private UUID _id;
	private double _inventory;
	private double _price;
	private double _reservationPrice;
	private double _wageRate;
	private double _markup;
	private double _productivity = NNSBuilder.Productivity;
	private boolean _isHiring = false;
	private List<Household> _employees = new ArrayList<Household>();
	private double _soldCount = 0;
	private double _requestCount = 0;
	private int excessMonths = 0;
	private int shortageMonths = 0;
	private int seekingEmploymentMonthCount = 0;
	
	
	private int _periodLength = NNSBuilder.PeriodLength;
	
	public Firm(ContinuousSpace<Object> space, Grid<Object> grid, double liquidity, double reservationPrice) {
		super(space, grid, liquidity);
		
		_id = UUID.randomUUID();
		
		this._reservationPrice = reservationPrice;
		this._price = this._reservationPrice;
		this._markup = RandomHelper.nextDoubleFromTo(.001, .5);
		
		this._wageRate = this._price - (this._price * this._markup);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		double tickCount = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		
		if (tickCount == 5500){
			NNSBuilder.DemandForGoods = 16;
		}
		
		//if (tickCount == 4000){
		//	NNSBuilder.Productivity = 2;
		//}
		
		if (tickCount % _periodLength == 0){
			if (_inventory > 0 && (((_soldCount/_inventory) - 1) < -0.1)){
				excessMonths++;
			} else if (_soldCount < _requestCount){
				shortageMonths++;
			}
		}
		
		if (excessMonths > 3){
			fireEmployee();
			excessMonths = 0;
		}
		
		if (shortageMonths > 3){
			_isHiring = true;
			shortageMonths = 0;
		}
		
		if (_isHiring){
			seekingEmploymentMonthCount++;
		}
		
		if (seekingEmploymentMonthCount > 3){
			this._wageRate += 1;
		}
		
		if (tickCount % _periodLength == 0 || tickCount == 1){
			payEmployees();
			payDividends();
			produceGoods();
			
			_soldCount = 0;
			_requestCount = 0;
		}
	}
	
	protected void payEmployees(){
		for(Household hh : _employees){
			if (_liquidity > (hh.getWage() * _periodLength)){
				hh.receiveIncome(hh.getWage() * _periodLength);
				_liquidity -= (hh.getWage()* _periodLength);
			}else {
				hh.fire();
			}
		}
	}
	
	protected void payDividends(){
		if (_liquidity > 0){
			List<Household> houseHolds = new ArrayList<Household>();
			
			for(Object obj : _grid.getObjects()){
				if (obj instanceof Household){
					houseHolds.add((Household)obj);
				}
			}
			
			double dividend = _liquidity / houseHolds.size();
			
			for(Household hh : houseHolds){
				hh.receiveIncome(dividend);
				_liquidity = Math.max((_liquidity - dividend), 0);
			}
		}
	}
	
	protected void fireEmployee(){
		if (_employees.size() > 0){
			int index = RandomHelper.nextIntFromTo(0, _employees.size() - 1);
			Household hh = _employees.get(index);
			
			_employees.remove(hh);
			hh.fire();
		}
	}
	
	protected boolean isHiring(){
		return _isHiring;
	}
	
	protected void produceGoods(){
		_inventory += (_productivity * _employees.size()) * _periodLength;
	}
	
	public String getIdString(){
		return _id.toString();
	}
	
	public double getWage(){
		return _wageRate;
	}
	
	public double getPrice(){
		return _price;
	}
	
	public double getMarkup(){
		return (1 - _wageRate/_price) + 1;
	}
	
	public double getCurrentInventory(){
		return _inventory;
	}
	
	public int getEmployeeCount(){
		return _employees.size();
	}
	
	public boolean hireEmployee(Household employee){
		if (!_employees.contains(employee)){
			_employees.add(employee);
			_isHiring = false;
			seekingEmploymentMonthCount = 0;
			return true;
		}
		
		return false;
	}
	
	public boolean purchaseGood(){
		_requestCount++;
		
		if (_inventory >= 1){
			_soldCount++;
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