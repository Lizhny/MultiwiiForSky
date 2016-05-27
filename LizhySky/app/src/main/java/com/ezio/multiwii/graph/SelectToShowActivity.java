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
package com.ezio.multiwii.graph;

import android.os.Bundle;
import android.widget.CheckBox;

import com.actionbarsherlock.app.SherlockActivity;
import com.ezio.multiwii.R;
import com.ezio.multiwii.app.App;

public class SelectToShowActivity extends SherlockActivity {
	App app;

	CheckBox checkBoxAccRoll;
	CheckBox checkBoxAccPitch;
	CheckBox checkBoxAccZ;

	CheckBox checkBoxGyroRoll;
	CheckBox checkBoxGyroPitch;
	CheckBox checkBoxGyroYaw;

	CheckBox checkBoxMagRoll;
	CheckBox checkBoxMagPitch;
	CheckBox checkBoxMagYaw;

	CheckBox checkBoxAlt;
	CheckBox checkBoxHead;

	CheckBox checkBoxDebug1;
	CheckBox checkBoxDebug2;
	CheckBox checkBoxDebug3;
	CheckBox checkBoxDebug4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_show_layout);

		app = (App) getApplication();

		checkBoxAccRoll = (CheckBox) findViewById(R.id.checkBoxAccRoll);
		checkBoxAccPitch = (CheckBox) findViewById(R.id.checkBoxAccPitch);
		checkBoxAccZ = (CheckBox) findViewById(R.id.checkBoxAccZ);

		checkBoxGyroRoll = (CheckBox) findViewById(R.id.checkBoxGyroRoll);
		checkBoxGyroPitch = (CheckBox) findViewById(R.id.checkBoxGyroPitch);
		checkBoxGyroYaw = (CheckBox) findViewById(R.id.checkBoxGyroYaw);

		checkBoxMagRoll = (CheckBox) findViewById(R.id.checkBoxMagRoll);
		checkBoxMagPitch = (CheckBox) findViewById(R.id.checkBoxMagPitch);
		checkBoxMagYaw = (CheckBox) findViewById(R.id.checkBoxMagYaw);

		checkBoxAlt = (CheckBox) findViewById(R.id.checkBoxAlt);
		checkBoxHead = (CheckBox) findViewById(R.id.checkBoxHead);

		checkBoxDebug1 = (CheckBox) findViewById(R.id.checkBoxDEBUG1);
		checkBoxDebug2 = (CheckBox) findViewById(R.id.checkBoxDEBUG2);
		checkBoxDebug3 = (CheckBox) findViewById(R.id.checkBoxDEBUG3);
		checkBoxDebug4 = (CheckBox) findViewById(R.id.checkBoxDEBUG4);

	}

	@Override
	protected void onResume() {
		super.onResume();

		checkBoxAccRoll.setChecked(app.GraphsToShow.contains(app.ACCROLL));
		checkBoxAccPitch.setChecked(app.GraphsToShow.contains(app.ACCPITCH));
		checkBoxAccZ.setChecked(app.GraphsToShow.contains(app.ACCZ));

		checkBoxGyroRoll.setChecked(app.GraphsToShow.contains(app.GYROROLL));
		checkBoxGyroPitch.setChecked(app.GraphsToShow.contains(app.GYROPITCH));
		checkBoxGyroYaw.setChecked(app.GraphsToShow.contains(app.GYROYAW));

		checkBoxMagRoll.setChecked(app.GraphsToShow.contains(app.MAGROLL));
		checkBoxMagPitch.setChecked(app.GraphsToShow.contains(app.MAGPITCH));
		checkBoxMagYaw.setChecked(app.GraphsToShow.contains(app.MAGYAW));

		checkBoxAlt.setChecked(app.GraphsToShow.contains(app.ALT));
		checkBoxHead.setChecked(app.GraphsToShow.contains(app.HEAD));

		checkBoxDebug1.setChecked(app.GraphsToShow.contains(app.DEBUG1));
		checkBoxDebug2.setChecked(app.GraphsToShow.contains(app.DEBUG2));
		checkBoxDebug3.setChecked(app.GraphsToShow.contains(app.DEBUG3));
		checkBoxDebug4.setChecked(app.GraphsToShow.contains(app.DEBUG4));

	}

	@Override
	protected void onPause() {
		super.onPause();

		app.GraphsToShow = "";
		if (checkBoxAccRoll.isChecked())
			app.GraphsToShow += app.ACCROLL + ";";
		if (checkBoxAccPitch.isChecked())
			app.GraphsToShow += app.ACCPITCH + ";";
		if (checkBoxAccZ.isChecked())
			app.GraphsToShow += app.ACCZ + ";";

		if (checkBoxGyroRoll.isChecked())
			app.GraphsToShow += app.GYROROLL + ";";
		if (checkBoxGyroPitch.isChecked())
			app.GraphsToShow += app.GYROPITCH + ";";
		if (checkBoxGyroYaw.isChecked())
			app.GraphsToShow += app.GYROYAW + ";";

		if (checkBoxMagRoll.isChecked())
			app.GraphsToShow += app.MAGROLL + ";";
		if (checkBoxMagPitch.isChecked())
			app.GraphsToShow += app.MAGPITCH + ";";
		if (checkBoxMagYaw.isChecked())
			app.GraphsToShow += app.MAGYAW + ";";

		if (checkBoxAlt.isChecked())
			app.GraphsToShow += app.ALT + ";";
		if (checkBoxHead.isChecked())
			app.GraphsToShow += app.HEAD + ";";

		if (checkBoxDebug1.isChecked())
			app.GraphsToShow += app.DEBUG1 + ";";
		if (checkBoxDebug2.isChecked())
			app.GraphsToShow += app.DEBUG2 + ";";
		if (checkBoxDebug3.isChecked())
			app.GraphsToShow += app.DEBUG3 + ";";
		if (checkBoxDebug4.isChecked())
			app.GraphsToShow += app.DEBUG4 + ";";

		if (app.GraphsToShow.substring(app.GraphsToShow.length() - 1, app.GraphsToShow.length()).equals(";"))
			app.GraphsToShow = app.GraphsToShow.substring(0, app.GraphsToShow.length() - 1);

		app.SaveSettings(false);

	}

}
