package functionality.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import dataTypes.FunctionalityConditionContent;
import dataTypes.FunctionalityContent;
import dataTypes.ProgramElement;
import dataTypes.contentValueRepresentations.BooleanOrVariable;
import dataTypes.contentValueRepresentations.SelectableType;
import dataTypes.contentValueRepresentations.TextOrVariable;
import dataTypes.contentValueRepresentations.ValueOrVariable;
import dataTypes.contentValueRepresentations.VariableOnly;
import dataTypes.exceptions.NonExistingPinException;
import dataTypes.specialContentValues.DataList;
import dataTypes.specialContentValues.Term;
import dataTypes.specialContentValues.Variable;
import execution.Execution;
import main.functionality.Functionality;
import main.functionality.helperControlers.Parameters;
import main.functionality.helperControlers.hardware.analog.ProtocolHelpers;
import main.functionality.helperControlers.hardware.analog.RotaryDevice;
import main.functionality.helperControlers.hardware.analog.SensorDevice;
import productionGUI.sections.elements.VisualizableProgramElement;

public class Act07_Sensors extends Functionality {

	public static int POSITION = 7;
	public static String NAME = "Sensor Input";
	public static String IDENTIFIER = "ActAnalogNode";
	public static String DESCRIPTION = "Actions to retrieve the input (analog values) from sensors.";
	

	public static ProgramElement create_ElScanI2C()
	{
		Object[] params = new Object[1];
		return(new FunctionalityContent( "ElScanI2C",
				params,
				() -> {
					
					List<String> addresses;
					
					if (!SIMULATED)
						addresses = ProtocolHelpers.scanI2C();
					else
						addresses = new ArrayList<String>() {{ add("SIMULATED I2C 1"); add("SIMULATED I2C 2"); }};
		
					DataList list = null;
					if (!addresses.isEmpty())
					{
						list = DataList.externalSet((Variable) params[0], 0, new Term(0, addresses.get(0), Variable.textType), Variable.textType);
						if (addresses.size()>1)
						{
							for(int i = 1; i < addresses.size(); i++)
								list.set(i, addresses.get(i));
						}
					}
					else
					{
						list = DataList.externalSet((Variable) params[0], 0, new Term(0, "", Variable.textType), Variable.textType);
						list.clear(); // Todo: Just create an empty list
					}
					
					}));
	}
	public static ProgramElement visualize_ElScanI2C(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Scan I2C", "Searches for available I2C devices and fills them into a list.");
		vis.addParameter(0, new VariableOnly(true, true), "List Identifier", "List where the addresses will be placed in.");
		return(vis);
	}
	
	
	public static ProgramElement create_ElScanOneWire()
	{
		Object[] params = new Object[1];
		return(new FunctionalityContent( "ElScanOneWire",
				params,
				() -> {
					
					List<String> addresses;
					
					if (!SIMULATED)
						addresses = ProtocolHelpers.scanOneWire();
					else
						addresses = new ArrayList<String>() {{ add("SIMULATED 1-Wire 1"); add("SIMULATED 1-Wire 2"); }};
		
					DataList list = null;
					if (!addresses.isEmpty())
					{
						list = DataList.externalSet((Variable) params[0], 0, new Term(0, addresses.get(0), Variable.textType), Variable.textType);
						if (addresses.size()>1)
						{
							for(int i = 1; i < addresses.size(); i++)
								list.set(i, addresses.get(i));
						}
					}
					else
					{
						list = DataList.externalSet((Variable) params[0], 0, new Term(0, "", Variable.textType), Variable.textType);
						list.clear(); // Todo: Just create an empty list
					}
					
					}));
	}
	public static ProgramElement visualize_ElScanOneWire(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Scan One Wire", "Searches for available One Wire devices and fills them into a list.");
		vis.addParameter(0, new VariableOnly(true, true), "List Identifier", "List where the addresses will be placed in.");
		return(vis);
	}
	
	
	
	
	
	// Analog devices
	

	
	
	public static ProgramElement create_ElCreateINA219Device()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElCreateINA219Device",
				params,
				() -> {					
					
					int addr = parseI2CAddress((String) params[2]);
					if (addr < 0)
						return;
					
					int type = getINTparam(params[1]);
					
					SensorDevice device;
					try {
						device = SensorDevice.createINA219sensor(addr, type,
								(double) cont[0].getOptionalArgumentValueOR(0, 0.1d),
								(double) cont[0].getOptionalArgumentValueOR(1, 3d),
								cont[0].getOptionalArgumentValueOR(3, 8),
								cont[0].getOptionalArgumentValueOR(2, 32),
								cont[0].getOptionalArgumentValueOR(4, 12),
								cont[0].getOptionalArgumentValueOR(5, 12));
						
						initVariableAndSet(params[0], Variable.AnalogDevice, device);
					} catch (UnsupportedBusNumberException | IOException e)
					{
						Execution.setError("Error at accessing or initializing the I2C connected INA219 sensor.\nError type: " + e.getMessage(), false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElCreateINA219Device(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "INA219 Device", "Create an sensor device for accessing an I2C INA219 module to meassure power.\nThe output value can be accessed with the 'Read Sensor' action\nwhich reads it into a variable.\nAlternatingly it can be used in some events for sensor devices.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new SelectableType(new String[] {"Current in Ampere", "Shunt voltage in Volt", "Bus voltage in Volt", "Power in Watt"}), "Get value of", "This special multi-sensor can provide multiple values which you can read.\nNote you may re-use the same sensor for multiple value outputs!");
		vis.addParameter(2, new TextOrVariable("0x40"), "I2C Address", "The I2C address will be mentioned if you do the official Bash-based tutorial for such a board.\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		
		vis.addOptionalParameter(0, new ValueOrVariable("0.1"), "Resistor", "The shunt-resistor on the board.\nDefault is 0.1 Ohm. This senses up to 3A.\nYou can replace it to sense higher power. For example 0.01 Ohm for 30A!");
		vis.addOptionalParameter(1, new ValueOrVariable("3"), "Expected Max", "Max expected current. Default: 3 Ampere.");
		vis.addOptionalParameter(2, new ValueOrVariable("8"), "Gain", "An amplification factor in ahrdware. 1, 2, 4 or 8.");
		vis.addOptionalParameter(3, new ValueOrVariable("32"), "Voltage Range", "The basic voltage range. 16 or 32 (other values interpreted as 32).");
		vis.addOptionalParameter(4, new ValueOrVariable("12"), "Bus ADC accuracy", "Can be 9 - 12 but usually should be 12.");
		vis.addOptionalParameter(5, new ValueOrVariable("12"), "Shunt ADC accuracy", "Can be 9 - 12 but usually should be 12.");

		return(vis);
	}
	
	
	public static ProgramElement create_ElCreateBMP280Device()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElCreateBMP280Device",
				params,
				() -> {					
					
					int addr = parseI2CAddress((String) params[2]);
					if (addr < 0)
						return;	
					
					int type = getINTparam(params[1]);
					
					SensorDevice device;
					try {
						device = SensorDevice.createBMP280sensor(addr, type);
						initVariableAndSet(params[0], Variable.AnalogDevice, device);
					} catch (UnsupportedBusNumberException | IOException e)
					{
						Execution.setError("Error at accessing or initializing the I2C connected BMP280 sensor.\nError type: " + e.getMessage(), false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElCreateBMP280Device(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "BMP280 Device", "Create an sensor device for accessing an I2C BMP280 module.\nIt can provide air pressure, attitude and temperature.\nThe output value can be accessed with the 'Read Sensor' action\nwhich reads it into a variable.\nAlternatingly it can be used in some events for sensor devices.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new SelectableType(new String[] {"Pressure in mPa", "Attitude in Meters", "Temperature in °C", "Temperature in °F"}), "Get value of", "This special multi-sensor can provide multiple values which you can read.\nNote you may re-use the same sensor for multiple value outputs!");
		vis.addParameter(2, new TextOrVariable("0x76"), "I2C Address", "The I2C address will be mentioned if you do the official Bash-based tutorial for such a board.\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		return(vis);
	}

	
	public static ProgramElement create_ElCreateLSM303DDevice()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElCreateLSM303DDevice",
				params,
				() -> {					
					
					int addr = parseI2CAddress((String) params[2]);
					if (addr < 0)
						return;	
					
					int type = getINTparam(params[1]);
					
					SensorDevice device;
					try {
						device = SensorDevice.createLSM303Dsensor(addr, type);
						initVariableAndSet(params[0], Variable.AnalogDevice, device);
					} catch (UnsupportedBusNumberException | IOException e)
					{
						Execution.setError("Error at accessing or initializing the I2C connected GY-511/LSM303 sensor.\nError type: " + e.getMessage(), false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElCreateLSM303DDevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "GY-511/LSM303 Device", "Create an sensor device for accessing an I2C GY-511 module (also called LSM303D).\nIt can provide three axis of physical acceleration\nand three axis of magnet fields.\nThe output value can be accessed with the 'Read Sensor' action\nwhich reads it into a variable.\nAlternatingly it can be used in some events for sensor devices.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new SelectableType(new String[] {"Acceleration X-axis", "Acceleration Y-axis", "Acceleration Z-axis", "Magnetism X-axis", "Magnetism Y-axis", "Magnetism Z-axis"}), "Get value of", "This special multi-sensor can provide multiple values which you can read.\nNote you may re-use the same sensor for multiple value outputs!");
		vis.addParameter(2, new TextOrVariable("0x76"), "I2C Address", "The I2C address will be mentioned if you do the official Bash-based tutorial for such a board.\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		return(vis);
	}

	
	public static ProgramElement create_ElCreateMPU6050Device()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElCreateMPU6050Device",
				params,
				() -> {					
					
					int addr = parseI2CAddress((String) params[2]);
					if (addr < 0)
						return;	
					
					int type = getINTparam(params[1]);
					
					SensorDevice device;
					try {
						device = SensorDevice.createMPU6050sensor(addr, type);
						initVariableAndSet(params[0], Variable.AnalogDevice, device);
					} catch (UnsupportedBusNumberException | IOException e)
					{
						Execution.setError("Error at accessing or initializing the I2C connected MPU-6050 sensor.\nError type: " + e.getMessage(), false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElCreateMPU6050Device(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "MPU-6050 Device", "Create an sensor device for accessing an I2C MPU-6050 module.\nIt can provide three axis of physical acceleration\nand three axis of gravity (gyroscope).\nThis chip is used in quadrocopters and is fairly accurate.\nThe output value can be accessed with the 'Read Sensor' action\nwhich reads it into a variable.\nAlternatingly it can be used in some events for sensor devices.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new SelectableType(new String[] {"Acceleration X-axis", "Acceleration Y-axis", "Acceleration Z-axis", "Gravity X-axis", "Gravity Y-axis", "Gravity Z-axis"}), "Get value of", "This special multi-sensor can provide multiple values which you can read.\nNote you may re-use the same sensor for multiple value outputs!");
		vis.addParameter(2, new TextOrVariable("0x68"), "I2C Address", "The I2C address will be mentioned if you do the official Bash-based tutorial for such a board.\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		return(vis);
	}

	
	
	
	public static ProgramElement create_ElRotationSpeed()
	{
		Object[] params = new Object[5];
		return(new FunctionalityContent( "ElRotationSpeed",
				params,
				() -> {
					/*
					SensorDevice device;
					try {
						//device = SensorDevice.createRotImpulseSensor( /* Get args *//* ); // TODO
						initVariableAndSet(params[0], Variable.AnalogDevice, device);
					} catch (UnsupportedBusNumberException | IOException e)
					{
						Execution.setError("Error at accessing or initializing an Rotation Sensor.\nError type: " + e.getMessage(), false);
					}
					*/
					}));
	}
	public static ProgramElement visualize_ElRotationSpeed(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Rotation Sensor", "Create a rotation sensor using a normal GPIO that should be connected to an impulse provider. For example a magnet sensor. TODO");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new ValueOrVariable(), "Sensor GPIO", "The GPIO where the impulse sensor is attached to.");
		vis.addParameter(2, new BooleanOrVariable("False"), "GPIO Pull Up", "True if activating the pull up resistance or false if to activate the pull down.");
		vis.addParameter(3, new ValueOrVariable("1"), "Impulses/Rotation", "The GPIO where the impusle sensor is attached to.");
		vis.addParameter(4, new SelectableType(new String[] {"Revolutions per Minute", "Revolutions per Second", "Distance Traveled Since Last Read", "Distance Traveled Total"}), "Output in", "What exactly should be provided as an output.\nThe 'Distance Traveled' options require the first optional argument 'Circumference'.\nThat is useful for meassuring how far a car has traveled if the sensor is attached to a wheel.\nIf you palce a read with 'Distance Traveled Since Last Read' in a rhythm event, you can also compute its speed!");
		vis.addOptionalParameter(0, new ValueOrVariable(), "Circumference", "The circumference of a the wheel this sensor is attached to.\nOnly relevant if youw ant to compute the 'Distance Traveled'.");
		
		return(vis);
	}
	
	
	

	
	
	
	
	

	public static ProgramElement create_ElCreateBH1750Device()
	{
		Object[] params = new Object[2];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElCreateBH1750Device",
				params,
				() -> {					
					
					int addr = parseI2CAddress((String) params[1]);
					if (addr < 0)
						return;	
					
					SensorDevice device;
					try {
						device = SensorDevice.createBH1750sensor(addr);
						initVariableAndSet(params[0], Variable.AnalogDevice, device);
					} catch (UnsupportedBusNumberException | IOException e)
					{
						Execution.setError("Error at accessing or initializing the I2C connected BH1750 sensor.\nError type: " + e.getMessage(), false);
					}
					
					}));
	}
	public static ProgramElement visualize_ElCreateBH1750Device(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "BH1750 Device", "Create an sensor device for accessing an I2C BH1750 module providing britghtness.\nThe output value can be accessed with the 'Read Sensor' action\nwhich reads it into a variable.\nAlternatingly it can be used in some events for sensor devices.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new TextOrVariable("0x27"), "I2C Address", "The I2C address will be mentioned if you do the official Bash-based tutorial for such a board.\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		return(vis);
	}

	
	
	
	

	public static ProgramElement create_ElCreateDS18B20Device()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElCreateDS18B20Device",
				params,
				() -> {					
					

					SensorDevice device = null;
					//try {
						switch(((String) params[2]).toUpperCase())
						{
							case "DS18B20": device = SensorDevice.createDS18B20sensor(getINTparam(params[1]), (String) cont[0].getOptionalArgumentValueOR(0, ""), cont[0].getOptionalArgumentValueOR(1, 250), cont[0].getOptionalArgumentValueOR(0, 8));
						}
						initVariableAndSet(params[0], Variable.AnalogDevice, device);
					/*} catch (UnsupportedBusNumberException | IOException e)
					{
						Execution.setError("Error at accessing or initializing the sensor.\nError type: " + e.getMessage(), false);
					}*/
					
					}));
	}
	public static ProgramElement visualize_ElCreateDS18B20Device(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "One-Wire Device", "Create an sensor device for accessing a sensor via the '1-Wire-Protocol'.\nMost prominent example of such sensors is\nthe 'DS18B20' which meassures temperature.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new TextOrVariable(), "Device ID", "The Address of the data-source.\nLook into 'Tools' for analizing the available data-sources.");
		vis.addParameter(2, new TextOrVariable("DS18B20"), "Interpretation", "The mode how it will interpret the value (technically a text).\nUse one of the following keywords: 'DS18B20' -> Temperature sensor. Result will be a number.");
		vis.addOptionalParameter(0, new TextOrVariable(), "Not This", "Sometimes the '1-Wire-Protocol' fails to transmit a value on first try even if it is still figuring as connected.\nThe reason is not always clear or avoidable.\nTherefore many sensors provide a default value which is returned instead.\nIn case of the 'DS18B20' it is '80.0'.\nIf you provide such a default value here as the 'Not This' parameter,\nthe program will keep retrieving values until a valid one can be provided.\nUse the next two arguments to define this behavior more acurately.");
		vis.addOptionalParameter(1, new ValueOrVariable("250"), "Delay", "Time in milliseconds between two attempts to retrieve the value\nif the default 'Not This' value has been retrieved. Default delay is 250ms.");
		vis.addOptionalParameter(2, new ValueOrVariable("8"), "Max Attempts", "Limit of attempts to retrieve a valid value if the 'Not This' value has been returned.\nIf it ends without any valid value, the target variable is not changed.\nYou can use a very large value (~100000) to repeat endlessly.");
		
		return(vis);
	}

	
	/*
	public static ProgramElement create_ElOneWireValue()
	{
		Object[] params = new Object[3];
		ProgramContent[] content = new ProgramContent[1];
		return(content[0] = new ProgramContent( "ElOneWireValue",
				params,
				() -> {
					
					String valueText;
					
					int attempts = -1, delay = 250, maxAttempts = 8;
					if (content[0].hasOptionalArgument(0))
					{
						attempts = 0;
						if (content[0].hasOptionalArgument(1))
							delay = getIntegerVariable(content[0].getOptionalArgumentValue(1));
						if (content[0].hasOptionalArgument(2))
							maxAttempts = getIntegerVariable(content[0].getOptionalArgumentValue(2));
					}
					
					do
					{
						if (attempts > 0)
							OtherHelpers.sleepNonException(delay);
						
						try
						{
							valueText = OneWctrl.getDeviceValueById(getStringVariable( params[1] ));
						}
						catch (IOException e)
						{
							Execution.setError("The ID to an 1 Wire device is not valid!\nThe sensor might not be connected.\nGiven ID: " + (String) params[1] + "\n\n" + e.getMessage(), false);
							return;
						}
						
						attempts++;
					}
					while((attempts >= 1) && (attempts <= maxAttempts) && (valueText.equalsIgnoreCase(getStringVariable( content[0].getOptionalArgumentValue(0) ))) );
						
					if (attempts > maxAttempts)
						return;
					
					
					OneWctrl.interpretAndSet((String) params[2], valueText, params[0]);
					
					}));
	}
	public static ProgramElement visualize_ElOneWireValue(ProgramContent content)
	{
		VisualizableProgrammElement vis;
		vis = new VisualizableProgrammElement(content, "Read 1Wire Val", "Read a value from a device connected through the '1-Wire-Protocol'.\nMost prominent example of such sensors is the 'DS18B20' meassuring temperature.");
		vis.setArgumentDescription(0, new VariableOnly(true, true), "Target", "Variable where the result will be placed in.\nDepending on the 'Interpretation' it will be either a number or text.");
		vis.setArgumentDescription(1, new TextOrVariable(), "Device ID", "The Address of the data-source.\nLook into 'Tools' for analizing the available data-sources.");
		vis.setArgumentDescription(2, new TextOrVariable("DS18B20"), "Interpretation", "The mode how it will interpret the value (technically a text).\nUse one of the following keywords: 'DS18B20' -> Temperature sensor. Result will be a number.");
		vis.setOptionalArgument(0, new TextOrVariable(), "Not This", "Sometimes the '1-Wire-Protocol' fails to transmit a value on first try even if it is still figuring as connected.\nThe reason is not always clear or avoidable.\nTherefore many sensors provide a default value which is returned instead.\nIn case of the 'DS18B20' it is '80.0'.\nIf you provide such a default value here as the 'Not This' parameter,\nthe program will keep retrieving values until a valid one can be provided.\nUse the next two arguments to define this behavior more acurately.");
		vis.setOptionalArgument(1, new ValueOrVariable("250"), "Delay", "Time in milliseconds between two attempts to retrieve the value\nif the default 'Not This' value has been retrieved. Default delay is 250ms.");
		vis.setOptionalArgument(2, new ValueOrVariable("8"), "Max Attempts", "Limit of attempts to retrieve a valid value if the 'Not This' value has been returned.\nIf it ends without any valid value, the target variable is not changed.\nYou can use a very large value (~100000) to repeat endlessly.");
		
		return(vis);
	}
*/
	
	
	
	
	public static ProgramElement create_ElCreateAnalogADSDevice()
	{
		Object[] params = new Object[4];
		FunctionalityContent[] cont = new FunctionalityContent[1];
		
		return(cont[0] = new FunctionalityContent( "ElCreateAnalogADSDevice",
				params,
				() -> {					
					
					SensorDevice device = null;
					
					int channel = (int) (double) params[1];
					if (assertVal(channel >= 0, channel < 4, "The channel value is not between 0 and 3!\nGiven: " + channel))
						return;
					
					int addr = parseI2CAddress((String) params[3]);
					if (addr < 0)
						return;
					
					double gain = 1;
					if(cont[0].hasOptionalArgument(0))
						gain = (double) cont[0].getOptionalArgumentValue(0);
					
					switch((int) (double) params[2])
					{
					case 1015:
						device = SensorDevice.createDevice("ADS1015", 0, addr, channel, gain); break;
					case 1115:
						device = SensorDevice.createDevice("ADS1115", 0, addr, channel, gain); break;
					
					default:
						Execution.setError("The Type of the ADS1X15 may only be 1015 or 1115!\nGiven: " + (int) (double) params[2], false);
						return;
					}
					
					initVariableAndSet(params[0], Variable.AnalogDevice, device);
					
					}));
	}
	public static ProgramElement visualize_ElCreateAnalogADSDevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "ADS1X15 Device", "Create an sensor device for accessing analog (voltage) values\nthrough an ADS1015 or ADS1115 Analog -> Digital Converter chip (ADC).\nIt can be attached to the raspberries I2C interface and used for\nanalog sensors, meassuring voltages, potentiometers and mini-joysticks.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new ValueOrVariable(), "Channel", "Between 0 and 3. The device provides 4 different channels which can be read.");
		vis.addParameter(2, new SelectableType(new String[] {"ADS1015 with 12bit accuracy", "ADS1115 with 16bit accuracy"}), "Type", "The type of ADC you attached.");
		vis.addParameter(3, new TextOrVariable("0x40"), "I2C Address", "The I2C address will be mentioned if you do the official Bash-based tutorial for such a board.\nHowever look into the 'Tools' for some helpful features implemented in DRASP.\nAddress is in hexadecimal like it is also displayed by the system.");
		vis.addOptionalParameter(0, new ValueOrVariable("1"), "Gain Amplifier", "The ADS1X15 chips have a physical, built in amplifier.\nThat allows you to provide a factor\nand the input voltage will be amplified by this factor.\nAllowed factors are 0.66, 1, 2, 4, 8 and 16.");
		return(vis);
	}
	
	
	public static ProgramElement create_ElCreateAnalogMCPDevice()
	{
		Object[] params = new Object[4];
		return(new FunctionalityContent( "ElCreateAnalogMCPDevice",
				params,
				() -> {					
					
					SensorDevice device = null;
					
					int chipSelect = (int) (double) params[3];
					
					if (assertVal(chipSelect == 0 || chipSelect == 1, "The SPI Chip Select may only be 0 or 1!\nGiven: " + chipSelect))
						return;
					
					int channel = (int) (double) params[1];
					
					
					switch((int) params[2])
					{
					case 0:
						if (assertVal(channel >= 0, channel < 4, "The channel value is not between 0 and 3!\nGiven: " + channel))
							return;
						device = SensorDevice.createDevice("MCP3004", 0, chipSelect, channel, -1); break;
						
					case 1:
						if (assertVal(channel >= 0, channel < 8, "The channel value is not between 0 and 7!\nGiven: " + channel))
							return;
						device = SensorDevice.createDevice("MCP3008", 0, chipSelect, channel, -1); break;						
					}
					
					initVariableAndSet(params[0], Variable.AnalogDevice, device);
					
					}));
	}
	public static ProgramElement visualize_ElCreateAnalogMCPDevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "MCP300X Device", "Create an sensor device for accessing analog (voltage) values\nthrough an MCP3004 or MCP3008 Analog -> Digital Converter chip (ADC).\nIt can be attached to the raspberries SPI interface and used for\nanalog sensors, meassuring voltages, potentiometers and mini-joysticks.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new ValueOrVariable(), "Channel", "Between 0 and 3 or 7. An MCP3004 provides 4 different channels which can be read.\nAn MCP3008 provides 8.");
		vis.addParameter(2, new SelectableType(new String[] {"MCP3004 with 4 channels", "MCP3008 with 8 channels"}), "Type", "The type of ADC you attached.");
		//vis.setArgumentDescription(1, new ValueOrVariable(), "SPI Index", "0 or 1");
		vis.addParameter(3, new ValueOrVariable(), "SPI CS", "0 or 1. SPI Chip Select Using different values you can connect two chips to one raspberry.");
		return(vis);
	}
	
	
	public static ProgramElement create_ElCreateRotaryDevice()
	{
		Object[] params = new Object[3];
		FunctionalityContent[] content = new FunctionalityContent[1];
		return(content[0] = new FunctionalityContent( "ElCreateRotaryDevice",
				params,
				() -> {
					try
					{
						if (content[0].hasOptionalArgument(0))
							initVariableAndSet(params[0], Variable.rotaryDevice, new RotaryDevice((int) (double) params[1], (int) (double) params[2], (int) (double) content[0].getOptionalArgumentValue(0)) );
						else
							initVariableAndSet(params[0], Variable.rotaryDevice, new RotaryDevice((int) (double) params[1], (int) (double) params[2], 0));

					} catch (NonExistingPinException e)
					{
						e.callException();
					}
					}));
	}
	public static ProgramElement visualize_ElCreateRotaryDevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Rotary Encoder", "Initialize access to a simple rotary encoder that outputs 'graycode'.\nIt has to be attached to two pins input pins as well as 3.3V power (not 5V!!) and ground.\nNote that some rotary encoders provide a button when pressing the knob.\nSometimes that pin is marked with 'SW'.\nYou can use this button like any other button with the 'GPIO Changed' and 'GPIO State' events.\nNote that on the cheaper roatary encoders you might need to solder a 10K resitance.");
		vis.addParameter(0, new VariableOnly(true, true), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new ValueOrVariable(), "CLK Pin", "The GPIO pin connected to the so called 'CLK' pin of the encoder." + gpioPinReferenceText);
		vis.addParameter(2, new ValueOrVariable(), "DT Pin", "The GPIO pin connected to the so called 'DT' pin of the encoder." + gpioPinReferenceText);
		vis.addOptionalParameter(0, new ValueOrVariable("0"), "Debounce", "Some rotary encoders use physical switches. Those tend to 'bounce' when used.\nThat means for a tiny fraction of a second, the signal jumps up and down.\n'Debouncing' is a countermeassure which simply expects the signal to\nstay steadily for a number of milliseconds.\nA value between 5-10 is recommended but best check whether no rotation is missed.\nIf you have an optical encoder (not unlikely), you can use 0 (default).");
		return(vis);
	}
	
	
	
	
	
	public static ProgramElement create_ElAdjustAnalogDevice()
	{
		Object[] params = new Object[4];
		return(new FunctionalityContent( "ElAdjustAnalogDevice",
				params,
				() -> {
					
					((SensorDevice) params[0]).adjust((double) params[1], (double) params[2], (boolean) params[3]);
					
					}));
	}
	public static ProgramElement visualize_ElAdjustAnalogDevice(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Adjust Sensor", "Changes how sensor input from a created device is interpreted when retrieving a value.\nThe margins can be used to narrow the sensitive range of values.\nAdditionally, the output can be scaled to be always between 0 and 1.");
		vis.addParameter(0, new VariableOnly(), "Sensor Device", "Variable to reference to this device.\nIt needs to be used in the coresponding events and actions.");
		vis.addParameter(1, new ValueOrVariable("0"), "Bottom Margin", "Values below this margin are figuring as minimum.\nDepending on the 'Use Absolute' argument, either provide a value between 0 and 1 or absolute values.");
		vis.addParameter(2, new ValueOrVariable("1"), "Top Margin", "Values above this margin are figuring as maximum.\nDepending on the 'Use Absolute' argument, either provide a value between 0 and 1 or absolute values.");
		vis.addParameter(3, new BooleanOrVariable("False"), "Use Absolute", "If 'False', the values provided by the device will always be between 0 and 1 and you also have to provide\nthe the offsets this way.\nIf 'True', the devices will provide the absolute values of the chip instead. In the case of MCP3008, that means between 0 and 1023.");
		return(vis);
	}
	
	
	
	////////
	/*
	public static ProgrammElement create_ElGetAnalogInput()
	{
		
		FunctionalityContent functionality = new FunctionalityConditionContent();
		Parameters params = functionality.getParameters();
		
			
		return(functionality.setup(
			() -> {
					Parameters params = functionality.getParameters();
					
					
					initVariableAndSet(params.getVariable(1), Variable.doubleType, params.get(0, SensorDevice.class).getValue());
					
					
					loopDelay = params.getOptInt(0, -1);
					
					if (loopDelay > 0)
					{
						Variable var = params.getVariable(1);
						SensorDevice sensor = params.get(0, SensorDevice.class);

						Double ignore = params.getOptDouble(1, null);
						
						new Thread(() -> {
							while(Execution.isRunning())
							{
								double val = sensor.getValue();
								if (val != ignore)
									initVariableAndSet(var, Variable.doubleType, val);
								Execution.checkedSleep(loopDelay);
							} }).start();

						
						Timer timer = new Timer();
						timer.schedule(() -> {}, loopDelay, loopDelay);
					}
					
					}));
	}
	public static ProgrammElement visualize_ElGetAnalogInput(FunctionalityContent content)
	{
		VisualizableProgrammElement vis;
		vis = new VisualizableProgrammElement(content, "Read Sensor", "Retrieve the current value from a device.");
		vis.addParameter(0, new VariableOnly(), "Sensor Device", "Device providing the analog input.");
		vis.addParameter(1, new VariableOnly(true, true), "Target", "Variable where the result will be placed in.");
		vis.addOptionalParameter(0, new ValueOrVariable("-1"), "Auto Read Delay (ms)", "If providing a positive number X, the value will be read automatically every X milliseconds and palced int the given variable.\nTherefore you do not have to read manually anymore!\nNote that value reads are not always very fast. Therefore do not read more often than it's senseful.");
		vis.addOptionalParameter(1, new ValueOrVariable(), "Skip Value", "Some primitive sensors and drivers result in a dummy value if transmission of the value failed (happens sometimes).\nProvide this value here so the constantly updating loop ignores such values.\nNote that this means the value might not be actual anymore!");
		return(vis);
	}
	*//////
	
	
	
	public static ProgramElement create_ElGetAnalogAsJoystick()
	{
		Object[] params = new Object[5];
		return(new FunctionalityContent( "ElGetAnalogAsJoystick",
				params,
				() -> {
					
					//TODO: Make that this happens internally in the sensor device so the "get analog" element can be used directly afetr making an analog input a joystick
					
					double val = ((SensorDevice) params[0]).getValue() + ((double) params[2]);
					
					if (Math.abs(val) < ((double)params[3]))
						initVariableAndSet(params[1], Variable.doubleType, 0);
					else
						initVariableAndSet(params[1], Variable.doubleType, val* (double) params[4]);
					
					}));
	}
	public static ProgramElement visualize_ElGetAnalogAsJoystick(FunctionalityContent content)
	{
		VisualizableProgramElement vis;
		vis = new VisualizableProgramElement(content, "Joystick Value", "Retrieve the current analog value from a device\nand interpret it like a joystick.\nThe default values are optimal for the typical arduino joysticks.\nIn that case the resulting value will range from -1 and 1.");
		vis.addParameter(0, new VariableOnly(), "Sensor Device", "Device providing the analog input.\nCan be created with 'MCP300X Device' and 'ADS1X15 Device'");
		vis.addParameter(1, new VariableOnly(true, true), "Target", "Variable where the result will be placed in.");
		vis.addParameter(2, new ValueOrVariable("-0.5"), "Offset", "An offset applied to the result.\nFor a joystick that often is -0.5\nbecause the neutral point is in the middle and tehrefore 0\nhowever that can depend on the kind of input.");
		vis.addParameter(3, new ValueOrVariable("0.05"), "Zero Range", "If the true value with offset applied\nis smaller than 'Zero Range', 0 will be given out.\nAlso useful for analog joysticks to ignore small jitter.");
		vis.addParameter(4, new ValueOrVariable("2"), "Factor", "The final result will be multiplied with this factor.");
		return(vis);
	}
	
	
	

	
	
}
