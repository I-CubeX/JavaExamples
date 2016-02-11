/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  SpiExample.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2015 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

// Jan 2016: modified and extended for 8 channel MCP3008 interfacing
// johnty@infusionsytems.com

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.io.IOException;

public class SpiExample {

    // SPI device
    public static SpiDevice spi = null;

    // SPI operations

    public static void main(String args[]) throws InterruptedException, IOException {
        
        // 
        // This SPI example is using the Pi4J SPI interface to communicate with
        // the SPI hardware interface connected to a MCP23S17 I/O Expander.
        //   -- MODIFIED to work with MCP3008 8-ch A2D
        //
        // Please note the following command are required to enable the SPI driver on
        // your Raspberry Pi:
        // >  sudo modprobe spi_bcm2708
        // >  sudo chown `id -u`.`id -g` /dev/spidev0.*
        //
        // this source code was adapted from:
        // https://github.com/thomasmacpherson/piface/blob/master/python/piface/pfio.py
        //
        // see this blog post for additional details on SPI and WiringPi
        // http://wiringpi.com/reference/spi-library/
        //
        // see the link below for the data sheet on the MCP23S17 chip:
        // http://ww1.microchip.com/downloads/en/devicedoc/21952b.pdf
        

        System.out.println("<--Pi4J--> SPI test program using MCP3008 AtoD Chip");

        // create SPI object instance for SPI for communication
        spi = SpiFactory.getInstance(SpiChannel.CS0,
                                     SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                                     SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0

        // infinite loop
        while(true) {
            System.out.print("ADC: ");
            for (int i=0; i<8; i++)
               System.out.format("%4d   ", read(i));
            System.out.println("");
            Thread.sleep(10);
        }
    }

    public static int read(int ch) throws IOException {
        // send test ASCII message
        byte packet[] = new byte[3];
        packet[0] = (byte) 0x01;  // 7 zeros then start bit 
	//then we have the following byte to pack:
	// SGL/DIF = 1, D2, D1, D0 == ch
	// 1 [d2] [d1] [d0] [x] [x] [x] [x]
        int d = (0x80 | ((ch & 0x07)<<4)) & 0xFF;
        packet[1] = (byte)d; 
        packet[2] = 0x00;
           
        byte[] result = spi.write(packet);       //10-bit data is in second and third bytes
        int val = (result[1]<<8) & 0b1100000000; //first byte contains upper 2 bits
        val = val | result[2] & 0xFF;            //second byte contains lower 8 bits
        return val;
    }
    
    
    public static String bytesToBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j];
            sb.append(Integer.toBinaryString(v));
        }
        return sb.toString();
    }    

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }    
}
