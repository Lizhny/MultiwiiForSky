/*  MultiWii EZ-GUI
    Copyright (C) <2012>  Bartosz Szczygiel (eziosoft)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ezio.multiwii.mw;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import communication.Communication;

public class MultiWii230 extends MultirotorData {

	public MultiWii230(Communication bt) {
		EZGUIProtocol = "2.3";

		timer1 = 10; // used to send request every 10 requests
		timer2 = 0; // used to send requests once after connection

		this.communication = bt;

		PIDITEMS = 10;
		CHECKBOXITEMS = 0;
		byteP = new int[PIDITEMS];
		byteI = new int[PIDITEMS];
		byteD = new int[PIDITEMS];
		buttonCheckboxLabel = new String[0];
		init();
	}

	private void init() {
		CHECKBOXITEMS = buttonCheckboxLabel.length;
		activation = new int[CHECKBOXITEMS];
		ActiveModes = new boolean[CHECKBOXITEMS];
		Checkbox = new Boolean[CHECKBOXITEMS][12];
		ResetAllChexboxes();
	}

	private void ResetAllChexboxes() {
		for (int i = 0; i < buttonCheckboxLabel.length; i++) {
			for (int j = 0; j < 12; j++) {
				Checkbox[i][j] = false;
			}
		}
	}

	// send msp without payload
	public List<Byte> requestMSP(int msp) {
		return requestMSP(msp, null);
	}

	// send multiple msp without payload
	private List<Byte> requestMSP(int[] msps) {
		List<Byte> s = new LinkedList<Byte>();
		for (int m : msps) {
			s.addAll(requestMSP(m, null));
		}
		return s;
	}

	// send msp with payload
	public List<Byte> requestMSP(int msp, Character[] payload) {
		if (msp < 0) {
			return null;
		}
		List<Byte> bf = new LinkedList<Byte>();
		for (byte c : MSP_HEADER.getBytes()) {
			bf.add(c);
		}

		byte checksum = 0;
		byte pl_size = (byte) ((payload != null ? (int) (payload.length) : 0) & 0xFF);
		bf.add(pl_size);
		checksum ^= (pl_size & 0xFF);

		bf.add((byte) (msp & 0xFF));
		checksum ^= (msp & 0xFF);

		if (payload != null) {
			for (char c : payload) {
				bf.add((byte) (c & 0xFF));
				checksum ^= (c & 0xFF);
			}
		}
		bf.add(checksum);
		return (bf);
	}

	public void sendRequestMSP(List<Byte> msp) {
		byte[] arr = new byte[msp.size()];
		int i = 0;
		for (byte b : msp) {
			arr[i++] = b;
		}
		communication.Write(arr); // send the complete byte sequence in one go
	}

	public void evaluateCommand(byte cmd, int dataSize) {

		int i;
		int icmd = (int) (cmd & 0xFF);
		switch (icmd) {
		case MSP_IDENT:
			version = read8();
			multiType = read8();
			MSPversion = read8(); // MSP version
			multiCapability = read32();// capability
			if ((multiCapability & 1) > 0)
				multi_Capability.RXBind = true;
			if ((multiCapability & 4) > 0)
				multi_Capability.Motors = true;
			if ((multiCapability & 8) > 0)
				multi_Capability.Flaps = true;

			if ((multiCapability & 0x80000000) > 0)
				multi_Capability.ByMis = true;

			break;

		case MSP_STATUS:
			cycleTime = read16();
			i2cError = read16();
			SensorPresent = read16();
			mode = read32();
			confSetting = read8();

			if ((SensorPresent & 1) > 0)
				AccPresent = 1;
			else
				AccPresent = 0;

			if ((SensorPresent & 2) > 0)
				BaroPresent = 1;
			else
				BaroPresent = 0;

			if ((SensorPresent & 4) > 0)
				MagPresent = 1;
			else
				MagPresent = 0;

			if ((SensorPresent & 8) > 0)
				GPSPresent = 1;
			else
				GPSPresent = 0;

			if ((SensorPresent & 16) > 0)
				SonarPresent = 1;
			else
				SonarPresent = 0;

			for (i = 0; i < CHECKBOXITEMS; i++) {
				if ((mode & (1 << i)) > 0)
					ActiveModes[i] = true;
				else
					ActiveModes[i] = false;

			}

			break;
		case MSP_RAW_IMU:

			ax = read16();
			ay = read16();
			az = read16();

			gx = read16() / 8;
			gy = read16() / 8;
			gz = read16() / 8;

			magx = read16() / 3;
			magy = read16() / 3;
			magz = read16() / 3;
			break;

		case MSP_SERVO:
			for (i = 0; i < 8; i++)
				servo[i] = read16();
			break;
		case MSP_MOTOR:
			for (i = 0; i < 8; i++)
				mot[i] = read16();
			if (multiType == SINGLECOPTER)
				servo[7] = mot[0];
			if (multiType == DUALCOPTER) {
				servo[7] = mot[0];
				servo[6] = mot[1];
			}
			break;
		case MSP_RC:
			rcRoll = read16();
			rcPitch = read16();
			rcYaw = read16();
			rcThrottle = read16();
			rcAUX1 = read16();
			rcAUX2 = read16();
			rcAUX3 = read16();
			rcAUX4 = read16();
			break;
		case MSP_RAW_GPS:
			GPS_fix = read8();
			GPS_numSat = read8();
			GPS_latitude = read32();
			GPS_longitude = read32();
			GPS_altitude = read16();
			GPS_speed = read16();
			GPS_ground_course = read16();
			break;
		case MSP_COMP_GPS:
			GPS_distanceToHome = read16();
			GPS_directionToHome = read16();
			GPS_update = read8();
			break;
		case MSP_ATTITUDE:
			angx = read16() / 10;
			angy = read16() / 10;
			head = read16();
			break;
		case MSP_ALTITUDE:
			alt = ((float) read32() / 100) - AltCorrection;
			vario = read16();
			break;
		case MSP_ANALOG:
			bytevbat = read8();
			pMeterSum = read16();
			rssi = read16();
			amperage = read16();
			break;
		case MSP_RC_TUNING:
			byteRC_RATE = read8();
			byteRC_EXPO = read8();
			byteRollPitchRate = read8();
			byteYawRate = read8();
			byteDynThrPID = read8();
			byteThrottle_MID = read8();
			byteThrottle_EXPO = read8();
			break;
		case MSP_ACC_CALIBRATION:
			break;
		case MSP_MAG_CALIBRATION:
			break;
		case MSP_PID:
			for (i = 0; i < PIDITEMS; i++) {
				byteP[i] = read8();
				byteI[i] = read8();
				byteD[i] = read8();
			}
			break;
		case MSP_BOX:
			for (i = 0; i < CHECKBOXITEMS; i++) {
				activation[i] = read16();
				for (int aa = 0; aa < 12; aa++) {
					if ((activation[i] & (1 << aa)) > 0)
						Checkbox[i][aa] = true;
					else
						Checkbox[i][aa] = false;
				}
			}

			break;
		case MSP_BOXNAMES:
			buttonCheckboxLabel = new String(inBuf, 0, dataSize).split(";");
			Log.d("aaa", new String(inBuf, 0, dataSize));
			for (String s : buttonCheckboxLabel) {
				Log.d("aaa", s);
			}
			init();
			break;
		case MSP_PIDNAMES:
			PIDNames = new String(inBuf, 0, dataSize).split(";");
			break;

		case MSP_SERVO_CONF:
			// min:2 / max:2 / middle:2 / rate:1
			for (i = 0; i < 8; i++) {
				ServoConf[i].Min = read16();
				ServoConf[i].Max = read16();
				ServoConf[i].MidPoint = read16();
				ServoConf[i].Rate = read8();
			}
			break;
		case MSP_MISC:
			intPowerTrigger = read16(); // a

			minthrottle = read16();// b
			maxthrottle = read16();// c
			mincommand = read16();// d
			failsafe_throttle = read16();// e
			ArmCount = read16();// f
			LifeTime = read32();// g
			mag_decliniation = read16() / 10f;// h

			vbatscale = read8();// i
			vbatlevel_warn1 = (float) (read8() / 10.0f);// j
			vbatlevel_warn2 = (float) (read8() / 10.0f);// k
			vbatlevel_crit = (float) (read8() / 10.0f);// l
			if (ArmCount < 1)
				Log_Permanent_Hidden = true;
			break;

		case MSP_MOTOR_PINS:
			for (i = 0; i < 8; i++) {
				byteMP[i] = read8();
			}
			break;
		case MSP_DEBUG:
			debug1 = read16();
			debug2 = read16();
			debug3 = read16();
			debug4 = read16();
			break;
		case MSP_DEBUGMSG:
			while (dataSize-- > 0) {
				char c = (char) read8();
				if (c != 0) {
					DebugMSG += c;
				}
			}
			break;

		default:
			Log.e("aaa", "Error command - unknown replay " + String.valueOf(icmd));

		}
	}

	int c_state = IDLE;
	byte c;
	boolean err_rcvd = false;
	int offset = 0, dataSize = 0;
	byte checksum = 0;
	byte cmd;
	byte[] inBuf = new byte[256];
	int i = 0;
	int p = 0;

	int read32() {
		return (inBuf[p++] & 0xff) + ((inBuf[p++] & 0xff) << 8) + ((inBuf[p++] & 0xff) << 16) + ((inBuf[p++] & 0xff) << 24);
	}

	int read16() {
		return (inBuf[p++] & 0xff) + ((inBuf[p++]) << 8);
	}

	int read8() {
		return inBuf[p++] & 0xff;
	}

	private void ReadFrame() {
		DataFlow--;

		while (communication.dataAvailable()) {
			try {
				try {
					c = (communication.Read());
				} catch (Exception e) {
					c = 0;
					Log.e("aaa", "Read Error " + e.getMessage().toString());

					// TODO inform about refresh rate to low
					break;

				}
				// Log.d("21",String.valueOf(c));
				if (c_state == IDLE) {
					c_state = (c == '$') ? HEADER_START : IDLE;
				} else if (c_state == HEADER_START) {
					c_state = (c == 'M') ? HEADER_M : IDLE;
				} else if (c_state == HEADER_M) {
					if (c == '>') {
						c_state = HEADER_ARROW;
					} else if (c == '!') {
						c_state = HEADER_ERR;
					} else {
						c_state = IDLE;
					}
				} else if (c_state == HEADER_ARROW || c_state == HEADER_ERR) {
					/* is this an error message? */
					err_rcvd = (c_state == HEADER_ERR); /*
														 * now we are expecting
														 * the payload size
														 */
					dataSize = (c & 0xFF);
					/* reset index variables */
					p = 0;
					offset = 0;
					checksum = 0;
					checksum ^= (c & 0xFF);
					/* the command is to follow */
					c_state = HEADER_SIZE;
				} else if (c_state == HEADER_SIZE) {
					cmd = (byte) (c & 0xFF);
					checksum ^= (c & 0xFF);
					c_state = HEADER_CMD;
				} else if (c_state == HEADER_CMD && offset < dataSize) {
					checksum ^= (c & 0xFF);
					inBuf[offset++] = (byte) (c & 0xFF);
				} else if (c_state == HEADER_CMD && offset >= dataSize) {
					/* compare calculated and transferred checksum */
					if ((checksum & 0xFF) == (c & 0xFF)) {
						if (err_rcvd) {
							Log.e("Multiwii protocol", "Copter did not understand request type " + c);
						} else {
							/* we got a valid response packet, evaluate it */
							evaluateCommand(cmd, (int) dataSize);
							DataFlow = DATA_FLOW_TIME_OUT;
						}
					} else {
						Log.e("Multiwii protocol", "invalid checksum for command " + ((int) (cmd & 0xFF)) + ": " + (checksum & 0xFF) + " expected, got " + (int) (c & 0xFF));
						Log.e("Multiwii protocol", "<" + (cmd & 0xFF) + " " + (dataSize & 0xFF) + "> {");
						// for (i = 0; i < dataSize; i++) {
						// if (i != 0) {
						// Log.e("Multiwii protocol"," ");
						// }
						// Log.e("Multiwii protocol",(inBuf[i] & 0xFF));
						// }
						Log.e("Multiwii protocol", "} [" + c + "]");
						Log.e("Multiwii protocol", new String(inBuf, 0, dataSize));
					}
					c_state = IDLE;
				}
			} catch (Exception e) {
				Log.e("Multiwii protocol", "ReadFrame:" + e.getMessage());
			}
		}
	}

	@Override
	public void SendRequestMSP_PID_MSP_RC_TUNING() {
		int[] requests = { MSP_PID, MSP_RC_TUNING };
		sendRequestMSP(requestMSP(requests));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ezio.multiwii.mw.MultirotorData#SendRequestMSP_SET_MISC(int,
	 * int, int, int, int, float, byte, float, float, float)
	 */
	@Override
	public void SendRequestMSP_SET_MISC(int PowerTrigger, int minthrottle, int maxthrottle, int mincommand, int failsafe_throttle, float mag_decliniation, int vbatscale, float vbatlevel_warn1, float vbatlevel_warn2, float vbatlevel_crit) {

		// intPowerTrigger1 UNIT 16
		// conf.minthrottle UNIT 16
		// MAXTHROTTLE UNIT 16
		// MINCOMMAND UNIT 16
		// conf.failsafe_throttle UNIT 16
		// plog.arm UNIT 16
		// plog.lifetime UNIT 32
		// conf.mag_declination UNIT 16
		// conf.vbatscale UNIT 8
		// conf.vbatlevel_warn1 UNIT 8
		// conf.vbatlevel_warn2 UNIT 8
		// conf.vbatlevel_crit UNIT 8

		payload = new ArrayList<Character>();

		intPowerTrigger = (Math.round(PowerTrigger));
		payload.add((char) (intPowerTrigger % 256));
		payload.add((char) (intPowerTrigger / 256));

		payload.add((char) (minthrottle % 256));
		payload.add((char) (minthrottle / 256));

		payload.add((char) (maxthrottle % 256));
		payload.add((char) (maxthrottle / 256));

		payload.add((char) (mincommand % 256));
		payload.add((char) (mincommand / 256));

		payload.add((char) (failsafe_throttle % 256));
		payload.add((char) (failsafe_throttle / 256));

		payload.add((char) (0));// plog.arm (16bit) not used
		payload.add((char) (0));// plog.arm (16bit) not used
		payload.add((char) (0)); // plog.lifetime + (plog.armed_time / 1000000)
									// (32bit) not used
		payload.add((char) (0)); // plog.lifetime + (plog.armed_time / 1000000)
									// (32bit) not used
		payload.add((char) (0)); // plog.lifetime + (plog.armed_time / 1000000)
									// (32bit) not used
		payload.add((char) (0)); // plog.lifetime + (plog.armed_time / 1000000)
									// (32bit) not used

		int nn = Math.round(mag_decliniation * 10); // mag_decliniation
		payload.add((char) (nn - ((nn >> 8) << 8))); // mag_decliniation
		payload.add((char) (nn >> 8)); // mag_decliniation

		nn = Math.round(vbatscale);
		payload.add((char) (nn)); // VBatscale

		int q = (int) (vbatlevel_warn1 * 10);
		payload.add((char) (q));

		q = (int) (vbatlevel_warn2 * 10);
		payload.add((char) (q));

		q = (int) (vbatlevel_crit * 10);
		payload.add((char) (q));

		sendRequestMSP(requestMSP(MSP_SET_MISC, payload.toArray(new Character[payload.size()])));

		// MSP_EEPROM_WRITE
		sendRequestMSP(requestMSP(MSP_EEPROM_WRITE));

		// ///////////////////////////////////////////////////////////

	}

	@Override
	public void ProcessSerialData(boolean appLogging) {
		if (communication.Connected) {
			ReadFrame();
			if (appLogging)
				Logging();
		}
	}

	@Override
	public void SendRequestMSP_ACC_CALIBRATION() {
		sendRequestMSP(requestMSP(MSP_ACC_CALIBRATION));
	}

	@Override
	public void SendRequestMSP_MAG_CALIBRATION() {
		sendRequestMSP(requestMSP(MSP_MAG_CALIBRATION));
	}

	ArrayList<Character> payload = new ArrayList<Character>();

	@Override
	public void SendRequestMSP_SET_PID(float confRC_RATE, float confRC_EXPO, float rollPitchRate, float yawRate, float dynamic_THR_PID, float throttle_MID, float throttle_EXPO, float[] confP, float[] confI, float[] confD) {

		// MSP_SET_RC_TUNING
		payload = new ArrayList<Character>();
		payload.add((char) (Math.round(confRC_RATE * 100)));
		payload.add((char) (Math.round(confRC_EXPO * 100)));
		payload.add((char) (Math.round(rollPitchRate * 100)));
		payload.add((char) (Math.round(yawRate * 100)));
		payload.add((char) (Math.round(dynamic_THR_PID * 100)));
		payload.add((char) (Math.round(throttle_MID * 100)));
		payload.add((char) (Math.round(throttle_EXPO * 100)));
		sendRequestMSP(requestMSP(MSP_SET_RC_TUNING, payload.toArray(new Character[payload.size()])));

		// MSP_SET_PID
		payload = new ArrayList<Character>();
		for (int i = 0; i < PIDITEMS; i++) {
			byteP[i] = (int) (Math.round(confP[i] * 10));
			byteI[i] = (int) (Math.round(confI[i] * 1000));
			byteD[i] = (int) (Math.round(confD[i]));
		}

		// POS-4 POSR-5 NAVR-6 use different dividers
		byteP[4] = (int) (Math.round(confP[4] * 100.0));
		byteI[4] = (int) (Math.round(confI[4] * 100.0));

		byteP[5] = (int) (Math.round(confP[5] * 10.0));
		byteI[5] = (int) (Math.round(confI[5] * 100.0));
		byteD[5] = (int) ((Math.round(confD[5] * 10000.0)) / 10);

		byteP[6] = (int) (Math.round(confP[6] * 10.0));
		byteI[6] = (int) (Math.round(confI[6] * 100.0));
		byteD[6] = (int) ((Math.round(confD[6] * 10000.0)) / 10);

		for (i = 0; i < PIDITEMS; i++) {
			payload.add((char) (byteP[i]));
			payload.add((char) (byteI[i]));
			payload.add((char) (byteD[i]));
		}
		sendRequestMSP(requestMSP(MSP_SET_PID, payload.toArray(new Character[payload.size()])));

	}

	@Override
	public void SendRequestMSP_RESET_CONF() {
		sendRequestMSP(requestMSP(MSP_RESET_CONF));

	}

	@Override
	public void SendRequestMSP_MISC() {
		sendRequestMSP(requestMSP(MSP_MISC));

	}

	@Override
	public void SendRequestMSP_SET_RAW_GPS(byte GPS_FIX, byte numSat, int coordLAT, int coordLON, int altitude, int speed) {
		ArrayList<Character> payload = new ArrayList<Character>();
		payload.add((char) GPS_FIX);
		payload.add((char) numSat);
		payload.add((char) (coordLAT & 0xFF));
		payload.add((char) ((coordLAT >> 8) & 0xFF));
		payload.add((char) ((coordLAT >> 16) & 0xFF));
		payload.add((char) ((coordLAT >> 24) & 0xFF));

		payload.add((char) (coordLON & 0xFF));
		payload.add((char) ((coordLON >> 8) & 0xFF));
		payload.add((char) ((coordLON >> 16) & 0xFF));
		payload.add((char) ((coordLON >> 24) & 0xFF));

		payload.add((char) (altitude & 0xFF));
		payload.add((char) ((altitude >> 8) & 0xFF));

		payload.add((char) (speed & 0xFF));
		payload.add((char) ((speed >> 8) & 0xFF));

		sendRequestMSP(requestMSP(MSP_SET_RAW_GPS, payload.toArray(new Character[payload.size()])));
	}

	/**
	 * 0rcRoll 1rcPitch 2rcYaw 3rcThrottle 4rcAUX1 5rcAUX2 6rcAUX3 7rcAUX4
	 */
	@Override
	public void SendRequestMSP_SET_RAW_RC(int[] channels8) {
		ArrayList<Character> payload = new ArrayList<Character>();
		for (int i = 0; i < 8; i++) {
			payload.add((char) (channels8[i] & 0xFF));
			payload.add((char) ((channels8[i] >> 8) & 0xFF));
		}

		sendRequestMSP(requestMSP(MSP_SET_RAW_RC, payload.toArray(new Character[payload.size()])));

		sendRequestMSP(requestMSP(new int[] { MSP_RC }));

		// rcRoll = read16();
		// rcPitch = read16();
		// rcYaw = read16();
		// rcThrottle = read16();
		// rcAUX1 = read16();
		// rcAUX2 = read16();
		// rcAUX3 = read16();
		// rcAUX4 = read16();

		Log.d("aaa", "RC:" + String.valueOf(rcRoll) + " " + String.valueOf(rcPitch) + " " + String.valueOf(rcYaw) + " " + String.valueOf(rcThrottle) + " " + String.valueOf(rcAUX1) + " " + String.valueOf(rcAUX2) + " " + String.valueOf(rcAUX3) + " " + String.valueOf(rcAUX4));

	}

	@Override
	public void SendRequestMSP_BOX() {
		sendRequestMSP(requestMSP(MSP_BOX));
	}

	@Override
	public void SendRequestMSP_SET_BOX() {
		// MSP_SET_BOX
		payload = new ArrayList<Character>();
		for (i = 0; i < CHECKBOXITEMS; i++) {
			activation[i] = 0;
			for (int aa = 0; aa < 12; aa++) {
				activation[i] += (int) (((int) (Checkbox[i][aa] ? 1 : 0)) * (1 << aa));
				// activation[i] += (int) (checkbox[i].arrayValue()[aa] * (1 <<
				// aa));

			}
			payload.add((char) (activation[i] % 256));
			payload.add((char) (activation[i] / 256));
		}
		sendRequestMSP(requestMSP(MSP_SET_BOX, payload.toArray(new Character[payload.size()])));

	}

	@Override
	public void SendRequestMSP_EEPROM_WRITE() {
		// MSP_EEPROM_WRITE
		sendRequestMSP(requestMSP(MSP_EEPROM_WRITE));
	}

	// //Main Request//////////////////////////////////////////////////

//	public void SendRequest1() {
//		if (communication.Connected) {
//			int[] requests;
//
//			// this is fired only once////////
//			if (timer2 < 5) {
//				timer2++;
//			} else {
//				if (timer2 != 10) {
//
//					requests = new int[] { MSP_BOXNAMES };
//					sendRequestMSP(requestMSP(requests));
//					timer2 = 10;
//					return;
//				}
//			}
//			// ///////////////////////////////////////
//
//			timer1++;
//			if (timer1 > 10) { // fired every 10 requests
//				requests = new int[] { MSP_ANALOG, MSP_IDENT, MSP_MISC, MSP_RC_TUNING };
//				sendRequestMSP(requestMSP(requests));
//				timer1 = 0;
//
//				if (CHECKBOXITEMS == 0)
//					timer2 = 0;
//				return;
//			}
//
//			requests = new int[] { MSP_STATUS, MSP_RAW_IMU, MSP_SERVO, MSP_MOTOR, MSP_RC, MSP_RAW_GPS, MSP_COMP_GPS, MSP_ALTITUDE, MSP_ATTITUDE, MSP_DEBUG };
//			sendRequestMSP(requestMSP(requests));
//
//		} else {
//			timer1 = 10;
//			timer2 = 0;
//		}
//	}

	// NEW Main requests///////////////////////////////////////////////

	int timer3 = -1;

	int[] requests = new int[] { 0, MSP_ATTITUDE, MSP_ALTITUDE, MSP_RAW_GPS, MSP_BOX, MSP_RAW_IMU };
	final int[] requestsOnce = new int[] { MSP_IDENT, MSP_BOXNAMES, MSP_PID, MSP_BOX, MSP_MISC };
	int[] requestsPeriodical = new int[] { MSP_STATUS, MSP_COMP_GPS, MSP_ANALOG, MSP_SERVO, MSP_MOTOR, MSP_RC, MSP_DEBUG };

	public void SendRequest2() {

		if (communication.Connected) {

			// MSP_WP - in App.java

			if (CHECKBOXITEMS == 0)
				timer3 = -1;

			// Log.d("aaa", "timer3=" + String.valueOf(timer3));
			switch (timer3) {
			case -1:
				sendRequestMSP(requestMSP(requestsOnce));
				break;

			default:
				requests[0] = (requestsPeriodical[timer3]);
				sendRequestMSP(requestMSP(requests));

				break;
			}

			timer3++;
			if (timer3 >= requestsPeriodical.length)
				timer3 = 0;

		}

	}

	// /////////////////////////////END NEW requests\\\\\\\\\\\\\\\\\\\\\\\\\\

	@Override
	public void SendRequestMSP_WP(int Number) {
		ArrayList<Character> payload = new ArrayList<Character>();
		payload.add((char) Number);
		sendRequestMSP(requestMSP(MSP_WP, payload.toArray(new Character[payload.size()])));
		Log.d("aaa", "MSP_WP (SendRequestGetWayPoint) " + String.valueOf(Number));
	}

	// ////////Extra functions/////////////////
	@Override
	public void SendRequestMSP_SET_SERIAL_BAUDRATE(int baudRate) {
		ArrayList<Character> payload = new ArrayList<Character>();

		payload.add((char) (baudRate & 0xFF));
		payload.add((char) ((baudRate >> 8) & 0xFF));
		payload.add((char) ((baudRate >> 16) & 0xFF));
		payload.add((char) ((baudRate >> 24) & 0xFF));

		sendRequestMSP(requestMSP(MSP_SET_SERIAL_BAUDRATE, payload.toArray(new Character[payload.size()])));

		Log.d("aaa", "MSP_SET_SERIAL_BAUDRATE " + String.valueOf(baudRate));
	}

	@Override
	public void SendRequestMSP_ENABLE_FRSKY() {
		sendRequestMSP(requestMSP(MSP_ENABLE_FRSKY));
		Log.d("aaa", "MSP_ENABLE_FRSKY");

	}

	// ///////////End of Extra Functions////////////

	@Override
	public void SendRequestMSP_SELECT_SETTING(int setting) {

		payload = new ArrayList<Character>();
		payload.add((char) setting);
		sendRequestMSP(requestMSP(MSP_SELECT_SETTING, payload.toArray(new Character[payload.size()])));
		Log.d("aaa", "MSP_SELECT_SETTING");
	}

	@Override
	public void SendRequestMSP_BIND() {
		sendRequestMSP(requestMSP(MSP_BIND));
		Log.d("aaa", "MSP_BIND");
	}


	@Override
	public void SendRequestMSP_SET_HEAD(int heading) {
		payload = new ArrayList<Character>();
		payload.add((char) (heading & 0xFF));
		payload.add((char) ((heading >> 8) & 0xFF));
		sendRequestMSP(requestMSP(MSP_SET_HEAD, payload.toArray(new Character[payload.size()])));
		Log.d("aaa", "MSP_SET_HEAD " + String.valueOf(heading));

	}

	@Override
	public void SendRequestMSP_SET_MOTOR(byte motorTogglesByte) {
		int motEnable[] = new int[8];
		payload = new ArrayList<Character>();
		motorTogglesByte = (byte) (motEnable[0] + motEnable[1] * 2 + motEnable[2] * 4 + motEnable[3] * 8 + motEnable[4] * 16 + motEnable[5] * 32 + motEnable[6] * 64 + motEnable[7] * 128);
		payload.add((char) (motorTogglesByte));
		// toggleMotor=false;
		sendRequestMSP(requestMSP(MSP_SET_MOTOR, payload.toArray(new Character[payload.size()])));
		Log.d("aaa", "MSP_SET_MOTOR " + String.valueOf(motorTogglesByte));
	}

	@Override
	public void SendRequest(int MainRequestMethod) {
//		if (MainRequestMethod == 1)
//			SendRequest1();
		if (MainRequestMethod == 2)
			SendRequest2();

	}

	@Override
	public void SendRequestMSP_SERVO_CONF() {
		sendRequestMSP(requestMSP(MSP_SERVO_CONF));
	}

	@Override
	public void SendRequestMSP_SET_SERVO_CONF() {
		ArrayList<Character> payload = new ArrayList<Character>();

		for (int i = 0; i < ServoConf.length; i++) {
			payload.add((char) (ServoConf[i].Min & 0xFF));
			payload.add((char) ((ServoConf[i].Min >> 8) & 0xFF));

			payload.add((char) (ServoConf[i].Max & 0xFF));
			payload.add((char) ((ServoConf[i].Max >> 8) & 0xFF));

			payload.add((char) (ServoConf[i].MidPoint & 0xFF));
			payload.add((char) ((ServoConf[i].MidPoint >> 8) & 0xFF));

			payload.add((char) ServoConf[i].Rate);
		}
		sendRequestMSP(requestMSP(MSP_SET_SERVO_CONF, payload.toArray(new Character[payload.size()])));
	}

}
