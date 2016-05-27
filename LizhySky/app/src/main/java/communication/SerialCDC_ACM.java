///*  MultiWii EZ-GUI
//    Copyright (C) <2012>  Bartosz Szczygiel (eziosoft)
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
package communication;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.widget.Toast;

import com.lizhy.LizhySky.R;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import util.LogUtil;

public class SerialCDC_ACM extends Communication {

	private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	private SerialInputOutputManager mSerialIoManager;
	private UsbManager mUsbManager;
	UsbSerialDriver mSerial;
	SimpleQueue<Integer> fifo = new SimpleQueue<Integer>();

	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {

		@Override
		public void onRunError(Exception e) {
			LogUtil.d(TAG, "Runner stopped.");
		}

		@Override
		public void onNewData(final byte[] data) {
			for (int i = 0; i < data.length; i++)
				fifo.put(Integer.valueOf(data[i]));

			mHandler.obtainMessage(MESSAGE_READ, data.length, -1, data).sendToTarget();
			// LogUtil.d("aaa", "FiFo count:" + String.valueOf(fifo.size()));

		}
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public SerialCDC_ACM(Context context) {
		super(context);

		Enable();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
	public void Enable() {

		Toast.makeText(context, "Starting Serial", Toast.LENGTH_SHORT).show();

		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		mSerial = UsbSerialProber.findFirstDevice(mUsbManager);

		LogUtil.d(TAG, "Resumed, mSerialDevice=" + mSerial);
		if (mSerial == null) {
			Toast.makeText(context, "No serial device.", Toast.LENGTH_LONG).show();
		} else {
			try {
				mSerial.open();

			} catch (IOException e) {
				LogUtil.e(TAG, "Error setting up device: " + e.getMessage(), e);
				Toast.makeText(context, "Error opening device: " + e.getMessage(), Toast.LENGTH_LONG).show();
				Connected = false;
				try {
					mSerial.close();
					Connected = false;
				} catch (IOException e2) {
					// Ignore.
				}
				mSerial = null;
				return;
			}
			// Toast.makeText(context, "Serial device: " + mSerial,
			// Toast.LENGTH_LONG).show();
			onDeviceStateChange();
		}
	}

	@Override
	public void Connect(String address, int speed) {

		try {
			mSerial.setParameters(speed, UsbSerialDriver.DATABITS_8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
			Connected = true;
			sendDeviceName("Serial Port " + String.valueOf(speed));
			setState(STATE_CONNECTED);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Connected = false;
			setState(STATE_NONE);
			sendMessageToUI_Toast(context.getString(R.string.Unabletoconnect));
		}

	}

	@Override
	public synchronized boolean dataAvailable() {
		return !fifo.isEmpty();
	}

	@Override
	public synchronized byte Read() {
		return (byte) (fifo.get() & 0xff);

	}

	@Override
	public synchronized void Write(byte[] arr) {

		if (Connected) {
			try {
				mSerial.write(arr, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// Toast.makeText(context,
			// "Serial port Write error - not connected",
			// Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void Close() {
		stopIoManager();
		if (mSerial != null) {
			try {
				mSerial.close();
			} catch (IOException e) {
				// Ignore.
			}
			// mSerial = null;
		}
		Connected = false;
		sendMessageToUI_Toast(context.getString(R.string.Disconnected));
		setState(STATE_NONE);
	}

	@Override
	public void Disable() {
		Close();
	}

	private void stopIoManager() {
		if (mSerialIoManager != null) {
			LogUtil.i(TAG, "Stopping io manager ..");
			mSerialIoManager.stop();
			mSerialIoManager = null;
		}
	}

	private void startIoManager() {
		if (mSerial != null) {
			LogUtil.i(TAG, "Starting io manager ..");
			mSerialIoManager = new SerialInputOutputManager(mSerial, mListener);
			mExecutor.submit(mSerialIoManager);
		}
	}

	private void onDeviceStateChange() {
		stopIoManager();
		startIoManager();
	}

}
