package dataTypes;

import java.util.ArrayList;
import java.util.List;

import dataTypes.minor.GridLoc;
import main.electronic.Electronics;
import main.electronic.Pin;
import productionGUI.sections.electronic.VisualizedComponent;
import productionGUI.sections.electronic.WirePoint;

public class Scheme {
	
	public static Scheme FromVisualizedStructures(List<VisualizedComponent> components,
			List<WirePoint> globalWirePoints) {
		
		//StringBuilder data = new StringBuilder();
		List<String> componentsData = new ArrayList<>();
		List<String> WirePointsData = new ArrayList<>();
		List<byte[]> connectionsData = new ArrayList<>();
		byte[] pinWirePointData;
		


		short idCounter = 0;
		for (WirePoint WirePoint: globalWirePoints)
		{
			WirePoint.tempID = idCounter; 
			idCounter++;
		}

		int totalPins = 0;
		for (VisualizedComponent component: components)
			totalPins += component.getComponentContent().getPins().size();
			
		pinWirePointData = new byte[totalPins*2];
		
		int ind = 0;
		for (VisualizedComponent component: components)
		{
			componentsData.add(component.toString());
			
			for(Pin pin: component.getComponentContent().getPins())
			{
				pinWirePointData[ind] = (byte)(pin.getAssociatedWirePoint(component).tempID >> 8); // make two bytes out of a short
				pinWirePointData[ind+1] = (byte)(pin.getAssociatedWirePoint(component).tempID);
						
				ind += 2;
			}
		}
		
		
		for (WirePoint WirePoint: globalWirePoints)
		{
			//data.append(WirePoint.ID);
			//data.append("\n");
			WirePointsData.add(String.valueOf(WirePoint.getLocation().getIndX()));
			WirePointsData.add(String.valueOf(WirePoint.getLocation().getIndY()));

			byte len = (byte) WirePoint.getDirectlyConnectedPoints().size();
			byte[] byteDat = new byte[len*2+1];
			byteDat[0] = len;
			
			ind = 1;
			for (WirePoint con: WirePoint.getDirectlyConnectedPoints())
			{
				byteDat[ind] = (byte)(con.tempID >> 8); // make two bytes out of a short
				byteDat[ind+1] = (byte)(con.tempID);
				ind += 2;
			}
			connectionsData.add(byteDat);
		}	
		
		return new Scheme(componentsData, WirePointsData, connectionsData, pinWirePointData);
		
	}

	
	
	private List<String> componentsData;
	private List<String> WirePointsData;
	private List<byte[]> connectionsData;
	private byte[] pinWirePointData;

	public Scheme(List<String> componentsData, List<String> WirePointsData, List<byte[]> connectionsData, byte[] pinWirePointData)
	{
		this.componentsData = componentsData;
		this.WirePointsData = WirePointsData;
		this.connectionsData = connectionsData;
		this.pinWirePointData = pinWirePointData;
	}
	
	public String toString()
	{
		StringBuilder data = new StringBuilder();
		
		data.append("COMPONENTS");
		for(String str: componentsData)
		{
			data.append(str);
			data.append("\n");
		}
		
		data.append("WIREPOINTS\n");
		for(String str: WirePointsData)
		{
			data.append(str);
			data.append("\n");
		}

		data.append("CONNECTIONS\n");
		for(byte[] bytes: connectionsData)
		{
			for(byte b: bytes)
				data.append(b);
			data.append("\n");
		}
		
		data.append("PINBYWIREPOINTS\n");
		for(byte bt: pinWirePointData)
		{
			data.append(bt);
		}
		
		return(data.toString());
	}
	
	
	public void reproduce()
	{
		//Electronics.clearAllComponents(); // todo: Remove this to allow combining multiple schemes by just adding them ontop but currently marked so they can be placed
		//Electronics.clearAllWirePoints(); // todo: Remove this to allow combining multiple schemes by just adding them ontop but currently marked so they can be placed
		
		boolean isX = true;
		int curX = 0;
		short idCounter = 0;
		
		List<WirePoint> newWirePoints = new ArrayList<>();
		
		for (String coord: WirePointsData)
		{
			if (isX)
				curX = Integer.valueOf(coord);
			else
			{
				WirePoint newWP = new WirePoint(GridLoc.fromInd(curX, Integer.valueOf(coord)));
				newWP.ID = idCounter;
				newWirePoints.add(newWP);
				idCounter++;
			}
			
			isX = !isX;
		}
		
		int ind = 0;
		for(byte[] bytes: connectionsData)
		{
			WirePoint wp = newWirePoints.get(ind);
			short btl = (short) (bytes[0]*2+1);
			for(short i = 1; i < btl; i += 2)
			{
				short targetId = (short)(((bytes[i] & 0xFF) << 8) | (bytes[i+1] & 0xFF));
				wp.addConnectionTo(newWirePoints.get(targetId), false);
			}
			
			ind += 1;
		}
		
		
		ind = 0;
		for(String str: componentsData)
		{
			VisualizedComponent comp = VisualizedComponent.fromString(str);
			Electronics.addComponent(comp);
			
			for(Pin pin: comp.getComponentContent().getPins())
			{
				short targetId = (short)(((pinWirePointData[ind] & 0xFF) << 8) | (pinWirePointData[ind+1] & 0xFF));
				WirePoint wp = newWirePoints.get(targetId);
				pin.associatePin(wp);
				wp.show();
				
						
				ind += 2;
			}
		}

			
	}
	
}
