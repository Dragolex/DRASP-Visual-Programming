/*
 *  Copyright (C) 2015 Marcus Hirt
 *                     www.hirt.se
 *
 * This software is free:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESSED OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) Marcus Hirt, 2015
 */
package main.functionality.helperControlers.hardware.PWMboard;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import execution.Execution;

/**
 * This class represents an Adafruit 16 channel I2C PWM driver board.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("unused")
// Not using all commands - yet.
public class PCA9685device implements PWMdevice {
	private final int address;
	private final I2CDevice i2cDevice;
	private final int bus;

	private final static int MODE1 = 0x00;
	private final static int MODE2 = 0x01;
	private final static int SUBADR1 = 0x02;
	private final static int SUBADR2 = 0x03;
	private final static int SUBADR13 = 0x04;
	private final static int PRESCALE = 0xFE;
	private final static int OUT0_ON_L = 0x06;
	private final static int OUT0_ON_H = 0x07;
	private final static int OUT0_OFF_L = 0x08;
	private final static int OUT0_OFF_H = 0x09;
	private final static int ALL_OUT_ON_L = 0xFA;
	private final static int ALL_OUT_ON_H = 0xFB;
	private final static int ALL_OUT_OFF_L = 0xFC;
	private final static int ALL_OUT_OFF_H = 0xFD;

	// Bits
	private final static int RESTART = 0x80;
	private final static int SLEEP = 0x10;
	private final static int ALLCALL = 0x01;
	private final static int INVRT = 0x10;
	private final static int OUTDRV = 0x04;

	/**
	 * Constructs a PWM device using the default settings. (I2CBUS.BUS_1, 0x40)
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 * @throws UnsupportedBusNumberException 
	 */
	/*
	public PWMcore() throws IOException, UnsupportedBusNumberException
	{
		// 0x40 is the default address used by the AdaFruit PWM board.
		this(I2CBus.BUS_1, 0x40);
	}
	*/

	/**
	 * Creates a software interface to an Adafruit 16 channel I2C PWM Driver
	 * Board (PCA9685).
	 * 
	 * @param bus
	 *            the I2C bus to use.
	 * @param address
	 *            the address to use.
	 * 
	 * @see I2CBus
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 * @throws UnsupportedBusNumberException 
	 */
	public PCA9685device(int bus, int address) throws IOException, UnsupportedBusNumberException
	{
		this.bus = bus;
		this.address = address;
		
		if (Execution.isSimulated())
		{
			i2cDevice = null;
			return;
		}
		
		i2cDevice = I2CFactory.getInstance(bus).getDevice(address);
		initialize();
	}

	/**
	 * Sets all PWM channels to the provided settings.
	 * 
	 * @param on
	 *            when to turn on the signal [0, 4095]
	 * @param off
	 *            when to turn off the signal [0, 4095]
	 * 
	 * @throws IOException
	 *             if there was a problem communicating with the device.
	 */
	public void setAllPWM(int on, int off) throws IOException
	{
		on = Math.max(Math.min(PWMcontrol.maximum_pwm_value, on), PWMcontrol.minimum_pwm_value);
		off = Math.max(Math.min(PWMcontrol.maximum_pwm_value, off), PWMcontrol.minimum_pwm_value);
		
		synchronized(i2cDevice)
		{
			i2cDevice.write(ALL_OUT_ON_L, (byte) (on & 0xFF));
			i2cDevice.write(ALL_OUT_ON_H, (byte) (on >> 8));
			i2cDevice.write(ALL_OUT_OFF_L, (byte) (off & 0xFF));
			i2cDevice.write(ALL_OUT_OFF_H, (byte) (off >> 8));
		}
	}

	
	public void setChannelPWM(int channelIndex, int on, int off) throws IOException
	{
		on = Math.max(Math.min(PWMcontrol.maximum_pwm_value, on), PWMcontrol.minimum_pwm_value);
		off = Math.max(Math.min(PWMcontrol.maximum_pwm_value, off), PWMcontrol.minimum_pwm_value);
		
		synchronized(i2cDevice)
		{
			i2cDevice.write(OUT0_ON_L + 4 * channelIndex, (byte) (on & 0xFF));
			i2cDevice.write(OUT0_ON_H + 4 * channelIndex, (byte) (on >> 8));
			i2cDevice.write(OUT0_OFF_L + 4 * channelIndex, (byte) (off & 0xFF));
			i2cDevice.write(OUT0_OFF_H + 4 * channelIndex, (byte) (off >> 8));
		}
	}
	
	
	
	/**
	 * Sets the PWM frequency to use. This is common across all channels. For
	 * controlling RC servos, 50Hz is a good starting point.
	 * 
	 * @param frequency
	 *            the PWM frequency to use, in Hz.
	 * @throws IOException
	 *             if a problem occurred accessing the device.
	 */
	public void setPWMFreqency(double frequency) throws IOException {
		double prescaleval = 25000000.0;
		prescaleval /= 4096.0;
		prescaleval /= frequency;
		prescaleval -= 1.0;
		
		if (Execution.isSimulated())
			return;
		
		synchronized(i2cDevice)
		{
			double prescale = Math.floor(prescaleval + 0.5);
			int oldmode = i2cDevice.read(MODE1);
			int newmode = (oldmode & 0x7F) | 0x10;
			i2cDevice.write(MODE1, (byte) newmode);
			i2cDevice.write(PRESCALE, (byte) (Math.floor(prescale)));
			i2cDevice.write(MODE1, (byte) oldmode);
			sleep(80);
			i2cDevice.write(MODE1, (byte) (oldmode | 0x80));
		}
	}
	


	/**
	 * Returns the address used when communicating with this PWM device.
	 * 
	 * @return the address used when communicating with this PWM device.
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * Returns the bus used when communicating with this PWM device.
	 * 
	 * @return the bus used when communicating with this PWM device.
	 */
	public int getBus() {
		return bus;
	}


	
	
	
	private void sleep(int millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {}
	}
	
	private void initialize() throws IOException {
		setAllPWM(0, 0);
		synchronized(i2cDevice)
		{
			i2cDevice.write(MODE2, (byte) OUTDRV);
			i2cDevice.write(MODE1, (byte) ALLCALL);
			sleep(50);
			int mode1 = i2cDevice.read(MODE1);
			mode1 = mode1 & ~SLEEP;
			i2cDevice.write(MODE1, (byte) mode1);
			sleep(50);
		}
	}
}
