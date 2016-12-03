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
package com.lizhy.Sky.Main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.lizhy.Sky.R;
import com.lizhy.Sky.about.InfoActivity;
import com.lizhy.Sky.advanced.ControlActivity;
import com.lizhy.Sky.app.App;
import com.lizhy.Sky.aux_pid.AUXActivity;
import com.lizhy.Sky.aux_pid.PIDActivity;
import com.lizhy.Sky.aux_pid.ServosActivity;
import com.lizhy.Sky.config.ConfigActivity;
import com.lizhy.Sky.frsky.FrskyActivity;
import com.lizhy.Sky.graph.GraphsActivity;
import com.lizhy.Sky.helpers.Functions;
import com.lizhy.Sky.log.LogActivity;
//import MotorsActivity;
import com.lizhy.Sky.other.CalibrationActivity;
import com.lizhy.Sky.other.MiscActivity;
import com.lizhy.Sky.radio.RadioActivity;
import com.lizhy.Sky.raw.RawDataActivity;
import com.ezio.sec.Sec;

import communication.BT_New;
import util.LogUtil;

public class MainMultiWiiActivity extends SherlockActivity {
    private boolean killme = false;
    private App app;
    TextView TVInfo;
    ActionBarSherlock actionBar;

    private final Handler mHandler = new Handler();
    private final Handler mHandler1 = new Handler() {
        //BinaryFileAccess file = new BinaryFileAccess("/MultiWiiLogs/dump1.txt", true);

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_New.MESSAGE_STATE_CHANGE:
                    Log.i("ccc", "MESSAGE_STATE_CHANGE: " + msg.arg1);

                    switch (msg.arg1) {
                        case BT_New.STATE_CONNECTED:
                            // setStatus("Connected");
                            break;
                        case BT_New.STATE_CONNECTING:
                            setStatus(getString(R.string.Connecting));
                            break;
                        case BT_New.STATE_NONE:
                            break;
                    }

                    break;
                case BT_New.MESSAGE_WRITE:
                    break;
                case BT_New.MESSAGE_READ:
//				byte[] readBuf = (byte[]) msg.obj;
//				String readMessage = new String(readBuf, 0, msg.arg1);
//				//file.WriteBytes(readBuf);

                    break;
                case BT_New.MESSAGE_DEVICE_NAME:
                    String deviceName = msg.getData().getString(BT_New.DEVICE_NAME);
                    setStatus(getString(R.string.Connected) + "->" + deviceName);
//				Log.d("ccc", "Device Name=" + deviceName);
                    break;
                case BT_New.MESSAGE_TOAST:
//				Log.i("ccc", "MESSAGE_TOAST:" + msg.getData().getString(BT_New.TOAST));
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BT_New.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final void setStatus(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        app = (App) getApplication();

        app.commMW.SetHandler(mHandler1);
        app.commFrsky.SetHandler(mHandler1);

        requestWindowFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiwii_main_layout3);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        MyPagerAdapter adapter = new MyPagerAdapter(this);

        adapter.SetTitles(new String[]{getString(R.string.page1)});
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        adapter.AddView(inflater.inflate(R.layout.multiwii_main_layout3_1, (ViewGroup) null, false));//???????
        //adapter.AddView(inflater.inflate(R.layout.multiwii_main_layout3_2, (ViewGroup) null, false));
        //adapter.AddView(inflater.inflate(R.layout.multiwii_main_layout3_3, (ViewGroup) null, false));

        TVInfo = (TextView) adapter.views.get(0).findViewById(R.id.textViewInfoFirstPage);

        viewPager.setAdapter(adapter);


        getSupportActionBar().setDisplayShowTitleEnabled(false);

        app.AppStartCounter++;
        app.SaveSettings(true);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (getIntent() != null) {
            killme = true;
            mHandler.removeCallbacksAndMessages(null);
            startActivity(new Intent(getApplicationContext(), ControlActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        app.ForceLanguage();
        super.onResume();

        killme = false;

        String app_ver = "";
        int app_ver_code = 0;
        try {
            app_ver = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            app_ver_code = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }

        TVInfo.setText(getString(R.string.app_name) + " " + app_ver + "." + String.valueOf(app_ver_code));//版本号
        TVInfo.setText(R.string.app_name);//版本号

        if (app.commMW.Connected || app.commFrsky.Connected) {

            try {
                mHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {

            }

            mHandler.postDelayed(update, 100);
            // Log.d(BT_old.TAG, "OnResume if connected");

        }

        if (app.ConfigHasBeenChange_DisplayRestartInfo) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.PleaseRestart)).setCancelable(false).setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    EXIT();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    @Override
    public void onPause() {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();

    }

    public void Close() {
        try {
            mHandler.removeCallbacksAndMessages(null);
            if (app.commMW.Connected)
                app.commMW.Close();
            if (app.commFrsky.Connected)
                app.commFrsky.Close();
        } catch (Exception e) {

        }

    }

    public void Connect() {
        if (app.CommunicationTypeMW == App.COMMUNICATION_TYPE_BT || app.CommunicationTypeMW == App.COMMUNICATION_TYPE_BT_NEW) {
            if (!app.MacAddress.equals("")) {
                app.commMW.Connect(app.MacAddress, app.SerialPortBaudRateMW);
                app.Say(getString(R.string.menu_connect));
            } else {
                Toast.makeText(getApplicationContext(), "Wrong MAC address. Go to Config and select correct device", Toast.LENGTH_LONG).show();
            }
            try {
                mHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {
            }
        } else {
            app.commMW.Connect(app.MacAddress, app.SerialPortBaudRateMW);
            app.Say(getString(R.string.menu_connect));
        }
    }

    public void ConnectFrsky(String MacAddress) {
        if (app.CommunicationTypeFrSky == App.COMMUNICATION_TYPE_SERIAL_FTDI) {
            app.commMW.Connect(app.MacAddressFrsky, app.SerialPortBaudRateFrSky);
        }

        if (app.CommunicationTypeFrSky == App.COMMUNICATION_TYPE_BT) {
            if (!app.MacAddressFrsky.equals("")) {
                app.commFrsky.Connect(app.MacAddressFrsky, app.SerialPortBaudRateFrSky);
                app.Say(getString(R.string.Connect_frsky));
            } else {
                Toast.makeText(getApplicationContext(), "Wrong MAC address. Go to Config and select correct device", Toast.LENGTH_LONG).show();
            }
            try {
                mHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {

            }
        }
    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {

            app.mw.ProcessSerialData(app.loggingON);

            app.frskyProtocol.ProcessSerialData(false);
            if (app.commFrsky.Connected)
                setSupportProgress((int) Functions.map(app.frskyProtocol.TxRSSI, 0, 110, 0, 10000));

            String t = new String();
            if (app.mw.BaroPresent == 1)
                t += "[BARO] ";
            if (app.mw.GPSPresent == 1)
                t += "[GPS] ";
            if (app.mw.SonarPresent == 1)
                t += "[SONAR] ";
            if (app.mw.MagPresent == 1)
                t += "[MAG] ";
            if (app.mw.AccPresent == 1)
                t += "[ACC]";

            String t1 = "[" + app.mw.MultiTypeName[app.mw.multiType] + "] ";
            t1 += "MultiWii " + String.valueOf(app.mw.version / 100f);

            if (app.mw.multi_Capability.ByMis)
                t += " by Mi";

            t1 += "\n" + t + "\n";
            t1 += getString(R.string.SelectedProfile) + ":" + String.valueOf(app.mw.confSetting) + "\n";

            if (app.mw.ArmCount > 0)
                t1 += getString(R.string.ArmedCount) + ":" + String.valueOf(app.mw.ArmCount) + " " + getString(R.string.LiveTime) + ":" + String.valueOf(app.mw.LifeTime);

            if (app.commMW.Connected)
                TVInfo.setText(t1);
            else
                TVInfo.setText(getString(R.string.NotConnected));

            app.Frequentjobs();
            app.mw.SendRequest(app.MainRequestMethod);
            if (!killme)
                mHandler.postDelayed(update, app.RefreshRate);

            LogUtil.d(app.TAG, "loop " + this.getClass().getName());
        }

    };

    // //buttons/////////////////////////////////////

    public void RawDataOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), RawDataActivity.class));
    }

    public void RadioOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), RadioActivity.class));
    }

    public void ConfigOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), ConfigActivity.class));
    }

    public void LoggingOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), LogActivity.class));
    }

//
//    public void MotorsOnClick(View v) {
//        killme = true;
//        mHandler.removeCallbacksAndMessages(null);
//        startActivity(new Intent(getApplicationContext(), MotorsActivity.class));
//    }

    public void PIDOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), PIDActivity.class));
    }

    public void OtherOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), CalibrationActivity.class));
    }

    public void MiscOnClick(View v) {
        if (app.Protocol > 220) {
            killme = true;
            mHandler.removeCallbacksAndMessages(null);
            startActivity(new Intent(getApplicationContext(), MiscActivity.class));
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.OnlyWithMW23), Toast.LENGTH_LONG).show();
        }
    }

    public void FrskyOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), FrskyActivity.class));
    }

    public void AUXOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), AUXActivity.class));
    }

    public void AdvancedOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), ControlActivity.class));
    }


    public void AboutOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), InfoActivity.class));
    }

    public void GraphsOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(getApplicationContext(), GraphsActivity.class));
    }

    public void DonateOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=EZ88MU3VKXSGG&lc=GB&item_name=MultiWiiAllinOne&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
        startActivity(browserIntent);
    }

    public void RateOnClick(View v) {
        killme = true;
        mHandler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
        startActivity(intent);
    }


    public void ServosOnClick(View v) {
        if (app.Protocol > 220) {
            killme = true;
            mHandler.removeCallbacksAndMessages(null);
            startActivity(new Intent(getApplicationContext(), ServosActivity.class));
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.OnlyWithMW23), Toast.LENGTH_LONG).show();
        }
    }

    public void VarioSoundOnOffOnClick(View v) {
        if (Sec.VerifyDeveloperID(Sec.GetDeviceID(getApplicationContext()), Sec.TestersIDs) || Sec.Verify(getApplicationContext(), "D..3")) {
            app.VarioSound = !app.VarioSound;
        } else {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

            dlgAlert.setTitle(getString(R.string.Locked));
            dlgAlert.setMessage(getString(R.string.DoYouWantToUnlock));
            // dlgAlert.setPositiveButton(getString(R.string.Yes), null);
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.ezio.ez_gui_unlocker");
                        startActivity(LaunchIntent);
                    } catch (Exception e) {
                        Intent goToMarket = null;
                        goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ezio.ez_gui_unlocker"));
                        startActivity(goToMarket);
                    }
                    // finish();
                }
            });
            dlgAlert.setNegativeButton(getString(R.string.No), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // finish();
                }
            });

            dlgAlert.create().show();
        }

    }

    void EXIT() {


        if (app.DisableBTonExit) {
            app.commMW.Disable();
            app.commFrsky.Disable();
        }
        app.mw.CloseLoggingFile();
        app.notifications.Cancel(99);
        Close();
        System.exit(0);
    }

    // /////menu////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.menu_connect_frsky).setVisible(app.FrskySupport);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_exit) {
            EXIT();

            return true;
        }

        if (item.getItemId() == R.id.menu_connect) {

            Connect();

            mHandler.postDelayed(update, 100);
            return true;
        }

        if (item.getItemId() == R.id.menu_connect_frsky) {

            ConnectFrsky(app.MacAddressFrsky);

            mHandler.postDelayed(update, 100);

            setSupportProgressBarVisibility(true);

            return true;
        }

        if (item.getItemId() == R.id.menu_disconnect) {
            app.Say(getString(R.string.menu_disconnect));
            Close();
            return true;
        }

        return false;
    }

    // ///menu end//////

}