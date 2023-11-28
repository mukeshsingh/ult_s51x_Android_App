package com.ultrontech.s515liftconfigure

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeSimInformationBinding
import com.ultrontech.s515liftconfigure.fragments.EditSimFragment
import com.ultrontech.s515liftconfigure.models.PINNumber
import com.ultrontech.s515liftconfigure.models.SimType
import com.ultrontech.s515liftconfigure.models.Util
import com.ultrontech.s515liftconfigure.wheelpicker.LoopView

class ChangeSimInformationActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityChangeSimInformationBinding
    private lateinit var loopView: LoopView
    private lateinit var btnNoPin: MaterialButton
    private lateinit var btnPinRequired: MaterialButton
    private lateinit var btnConfirm: Button

    private var isPinRequired: Boolean = false
    private var pinLength: Int = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeSimInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loopView = binding.loopView
        btnNoPin = binding.btnNoPin
        btnPinRequired = binding.btnPinRequired
        btnConfirm = binding.btnConfirmSimPin

        btnNoPin.setOnClickListener {
            isPinRequired = false
        }

        btnPinRequired.setOnClickListener {
            isPinRequired = true
        }

        btnConfirm.setOnClickListener {
//
//            if (pin != null && pin.size == pinLength) {
//                BluetoothLeService.service?.setPin(PINNumber(pinLength, pin))
//            }

            Log.d(EditSimFragment.TAG, "========== Selected Item: ${loopView.selectedItem}")

            val item = loopView.selectedItem
            if (BluetoothLeService?.service?.device?.simType != Util.getSimType( item)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    BluetoothLeService.service?.setSimType(Util.getSimType( item ))
                }, 1000)
            }

            finish()
        }

        loopView.setArrayList(arrayListOf(
            Util.getSimTypeName(SimType.ModemSimTypeUnknown), Util.getSimTypeName(
                SimType.ModemSimTypeInstallerProvided), Util.getSimTypeName(SimType.ModemSimTypeUserContract), Util.getSimTypeName(
                SimType.ModemSimTypeUserPAYG)))
        loopView.selectedItem = BluetoothLeService.service?.device?.simType?.ordinal ?: 0
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_change_sim_information)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}