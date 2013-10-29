package nNS;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Firm extends Agent {
	
	private int _inventory;
	private double _price;
	private double _reservationPrice;
	private double _wageRate;
	private double _markup;
	
	public Firm(ContinuousSpace<Object> space, Grid<Object> grid, double liquidity, double reservationPrice) {
		super(space, grid, liquidity);
		
		this._reservationPrice = reservationPrice;
		this._price = this._reservationPrice * RandomHelper.nextDoubleFromTo(1, 2);
		this._markup = RandomHelper.nextDoubleFromTo(.001, .5);
		
		this._wageRate = this._price - (this._price * this._markup);
	}
	
	public double getWage(){
		return _wageRate;
	}
}