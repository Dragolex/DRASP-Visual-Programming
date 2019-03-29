package main.electronic.attributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import staticHelpers.StringHelpers;

public class BigDecimalAttribute extends ComponentAttribute {
	
	static private Map<Object, BigDecimal> bigDecimals = new HashMap<>();
	
	
	public BigDecimalAttribute(double val, String unit)
	{
		super(BigDecimal.valueOf(val), unit);
	}
	public BigDecimalAttribute(double val, String unit, float baseFactor)
	{
		super(BigDecimal.valueOf(val), unit, baseFactor);
	}
	
	public BigDecimal get()
	{
		return((BigDecimal) super.get());
	}

	@Override
	protected String getAsStr()
	{
		BigDecimal num = ((BigDecimal) super.get()).multiply(BigDecimal.valueOf(baseFactor));
		
		String preUnit = "";
		
		double d = 153.5;
		BigDecimal bigg = BigDecimal.valueOf(d);


		if (largerThan(num, 1000))
			if (largerThan(num, 1000000))
				if (largerThan(num, 1000000000))
				{
					preUnit = "G"; // giga
					num = divBy(num, 1000000000);
				}
				else
				{
					preUnit = "M"; // mega
					num = divBy(num, 1000000);
				}
			else
			{
				preUnit = "k"; // kilo
				num = divBy(num, 1000);
			}
		else
			if (smallerThan(num, 0.001))
				if (smallerThan(num, 0.000001))
					if (smallerThan(num, 0.000000001))
						if (smallerThan(num, 0.000000000001))
						{
							preUnit = "n"; // nano
							num = mulBy(num, 1000000000);
							num = mulBy(num, 1000);
						}
						else
						{
							preUnit = "p"; // picp
							num = mulBy(num, 1000000000);
						}
					else
					{
						preUnit = "u"; // micro
						num = mulBy(num, 1000000);
					}
				else
				{
					preUnit = "m"; // milli
					num = mulBy(num, 1000);
				}

			
			
		return(StringHelpers.smoothDouble(num.doubleValue()) + preUnit);
	}
	
	
	private boolean largerThan(BigDecimal num, int comp)
	{
		if (bigDecimals.containsKey(comp))
		{
			return(num.compareTo(bigDecimals.get(comp)) != -1);
		}
		else
		{
			BigDecimal cmp = BigDecimal.valueOf(comp);
			bigDecimals.put(comp, cmp);
			return(num.compareTo(cmp) == 1);
		}
	}
	private boolean smallerThan(BigDecimal num, double comp)
	{
		if (bigDecimals.containsKey(comp))
		{
			return(num.compareTo(bigDecimals.get(comp)) != 1);
		}
		else
		{
			BigDecimal cmp = BigDecimal.valueOf(comp);
			bigDecimals.put(comp, cmp);
			return(num.compareTo(cmp) == -1);
		}
	}
	
	private BigDecimal mulBy(BigDecimal num, int comp)
	{
		if (bigDecimals.containsKey(comp))
		{
			return(num.multiply(bigDecimals.get(comp)));
		}
		else
		{
			BigDecimal cmp = BigDecimal.valueOf(comp);
			bigDecimals.put(comp, cmp);
			return(num.multiply(cmp));
		}
	}
	private BigDecimal divBy(BigDecimal num, int comp)
	{
		if (bigDecimals.containsKey(comp))
		{
			return(num.divide(bigDecimals.get(comp)));
		}
		else
		{
			BigDecimal cmp = BigDecimal.valueOf(comp);
			bigDecimals.put(comp, cmp);
			return(num.divide(cmp));
		}
	}
}
	