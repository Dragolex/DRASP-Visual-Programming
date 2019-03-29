package main.functionality.helperControlers.spline;

import java.util.List;


public class SplineCreator
{
	
	private final List<Float> x;
	private final List<Float> y;
	private final float[] m;

	private SplineCreator(List<Float> x, List<Float> y, float[] m) {
		this.x = x;
		this.y = y;
		this.m = m;
	}
	
	/**
	 * Creates a monotone cubic spline from a given set of control points.
	 * 
	 * The spline is guaranteed to pass through each control point exactly. Moreover, assuming the control points are
	 * monotonic (Y is non-decreasing or non-increasing) then the interpolated values will also be monotonic.
	 * 
	 * This function uses the Fritsch-Carlson method for computing the spline parameters.
	 * http://en.wikipedia.org/wiki/Monotone_cubic_interpolation
	 * 
	 * @param x
	 *            The X component of the control points, strictly increasing.
	 * @param y
	 *            The Y component of the control points
	 * @return
	 * 
	 * @throws IllegalArgumentException
	 *             if the X or Y arrays are null, have different lengths or have fewer than 2 values.
	 */
	public static SplineCreator createMonotoneCubicSpline(List<Float> x, List<Float> y) {
		
		if (x.size() == 0)
			return(new SplineCreator(x, y, null));
		
		if (x.size() == 1)
			return(new SplineCreator(x, y, new float[] {0}));		
		
		
		final int n = x.size();
		float[] d = new float[n - 1]; // could optimize this out
		float[] m = new float[n];

		// Compute slopes of secant lines between successive points.
		for (int i = 0; i < n - 1; i++)
		{
			float h = x.get(i+1) - x.get(i);
			
			if (h <= 0f)
			{
				throw new IllegalArgumentException("The control points must all have strictly increasing X values!\nLast val: "+ x.get(i) + " Next val: " + x.get(i+1) );
			}
			
			d[i] = (y.get(i+1) - y.get(i)) / h;
		}

		// Initialize the tangents as the average of the secants.
		m[0] = d[0];
		for (int i = 1; i < n - 1; i++)
		{
			m[i] = (d[i - 1] + d[i]) * 0.5f;
		}
		m[n - 1] = d[n - 2];

		// Update the tangents to preserve monotonicity.
		for (int i = 0; i < n - 1; i++)
		{
			if (d[i] == 0f)
			{ // successive Y values are equal
				m[i] = 0f;
				m[i + 1] = 0f;
			}
			else
			{
				float a = m[i] / d[i];
				float b = m[i + 1] / d[i];
				
				if (a <= 0)
                    m[i] = 0.0f;
				
   				if (b <= 0)
   					m[i+1] = 0.0f;
                
   				float h = (float) Math.hypot(a, b);
	            if (h > 3)
	            {
	            	float t = 3f / h;
	                m[i] = t * a * d[i];
	                m[i + 1] = t * b * d[i];
	            }
			}
		}
		
		
		return new SplineCreator(x, y, m);
	}
	
	
	public void updateAround(int ind)
	{
		int start = ind-1;
		int end = ind+2;
		
		final int n = x.size();
		float[] d = new float[n]; // could optimize this out
		
		// Compute slopes of secant lines between successive points.
		for (int i = start; i < end; i++)
		{
			float h = x.get(i+1) - x.get(i);
			
			if (h <= 0f)
			{
				throw new IllegalArgumentException("The control points must all have strictly increasing X values!\nLast val: "+ x.get(i) + " Next val: " + x.get(i+1) );
			}
			
			d[i] = (y.get(i+1) - y.get(i)) / h;
		}

		// Initialize the tangents as the average of the secants.
		for (int i = start; i < end; i++)
		{
			m[i] = (d[i - 1] + d[i]) * 0.5f;
		}

		// Update the tangents to preserve monotonicity.
		for (int i = start; i < end; i++)
		{
			if (d[i] == 0f)
			{ // successive Y values are equal
				m[i] = 0f;
				m[i + 1] = 0f;
			}
			else
			{
				float a = m[i] / d[i];
				float b = m[i + 1] / d[i];
				
				if (a <= 0)
                    m[i] = 0.0f;
				
   				if (b <= 0)
   					m[i+1] = 0.0f;
                
   				float h = (float) Math.hypot(a, b);
	            if (h > 3)
	            {
	            	float t = 3f / h;
	                m[i] = t * a * d[i];
	                m[i + 1] = t * b * d[i];
	            }
			}
		}
		
		
		
		
	}
	
	
	
	/**
	 * Interpolates the value of Y = f(X) for given X. Clamps X to the domain of the spline.
	 * 
	 * @param ind
	 *            The X value.
	 * @return The interpolated Y = f(X) value.
	 */
	public float interpolate(float ind) {
		if (m == null)
			return(0);
		if (x.size() == 1)
			return(y.get(0));
		
		// Handle the boundary cases.
		final int n = x.size();
		if (Float.isNaN(ind)) {
			return ind;
		}
		if (ind <= x.get(0)) {
			return y.get(0);
		}
		if (ind >= x.get(n - 1)) {
			return y.get(n - 1);
		}

		// Find the index 'i' of the last point with smaller X.
		// We know this will be within the spline due to the boundary tests.
		int i = 0;
		while (ind >= x.get(i+1))
		{
			i += 1;
			if (ind == x.get(i))
				return y.get(i);
		}

		// Perform cubic Hermite spline interpolation.
		float h = x.get(i+1) - x.get(i);
		float t = (ind - x.get(i)) / h;
		return (y.get(i) * (1 + 2 * t) + h * m[i] * t) * (1 - t) * (1 - t)
				+ (y.get(i+1) * (3 - 2 * t) + h * m[i + 1] * (t - 1)) * t * t;
	}

	// For debugging.
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		final int n = x.size();
		str.append("[");
		for (int i = 0; i < n; i++) {
			if (i != 0) {
				str.append(", ");
			}
			str.append("(").append(x.get(i));
			str.append(", ").append(y.get(i));
			str.append(": ").append(m[i]).append(")");
		}
		str.append("]");
		return str.toString();
	}
	
	
	
}

