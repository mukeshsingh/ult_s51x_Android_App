package com.ultrontech.s515liftconfigure

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeSimInformationBinding
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

    private lateinit var llSimType: LinearLayout
    private lateinit var llPinLength: LinearLayout
    private lateinit var llSimPin: LinearLayout

    private var isPinRequired: Boolean = false
    private var pinLength: Int = 6
    private var currentView: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeSimInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loopView = binding.loopView
        btnNoPin = binding.btnNoPin
        btnPinRequired = binding.btnPinRequired
        btnConfirm = binding.btnConfirmSimPin

        llSimType = binding.llSimType
        llPinLength = binding.llPinLength
        llSimPin = binding.llSimPin

        btnNoPin.setOnClickListener {
            isPinRequired = false
            it.visibility = View.GONE
            binding.btnNoPinEnable.visibility = View.VISIBLE

            binding.btnPinRequired.visibility = View.GONE
            binding.btnPinRequiredDisabled.visibility = View.VISIBLE
        }

        binding.btnPinRequiredDisabled.setOnClickListener {
            isPinRequired = true
            it.visibility = View.GONE
            binding.btnPinRequired.visibility = View.VISIBLE

            binding.btnNoPin.visibility = View.VISIBLE
            binding.btnNoPinEnable.visibility = View.GONE
        }

        btnConfirm.setOnClickListener { it ->
            when (currentView) {
                1 -> {
                    val item = loopView.selectedItem
                    if (BluetoothLeService?.service?.device?.simType != Util.getSimType(item)) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            BluetoothLeService.service?.setSimType(Util.getSimType(item))
                        }, 1000)
                    }

                    if (isPinRequired) {
                        llSimType.visibility = View.GONE
                        llPinLength.visibility = View.VISIBLE
                        currentView = 2
                    } else {
                        finish()
                    }
                }
                2 -> {
                    llPinLength.visibility = View.GONE
                    llSimPin.visibility = View.VISIBLE
                    currentView = 3
                }
                3 -> {
                    var pin = binding.editTextEnterPin.text.toString()
                    var confirmPin = binding.editTextConfirmPin.text.toString()

                    if (pin == confirmPin && pin.length == pinLength) {
                        var pinArray = pin.map {
                            it.digitToInt()
                        }.toIntArray()

                        BluetoothLeService.service?.setPin(PINNumber(pinLength, pinArray))
                        finish()
                    } else if (pin.length != pinLength) {
                        Snackbar.make(it, "Pin length must be $pinLength digit.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    } else if (pin != confirmPin) {
                        Snackbar.make(it, "New Pin and Confirm Pin must be match.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    }
                }
            }
        }

        binding.simSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO Auto-generated method stub
                binding.txtPinLength.text = progress.toString()
                pinLength = progress
//                Toast.makeText(applicationContext, progress.toString(), Toast.LENGTH_LONG).show()
            }
        })

        loopView.setArrayList(arrayListOf(
            Util.getSimTypeName(SimType.ModemSimTypeUnknown), Util.getSimTypeName(
                SimType.ModemSimTypeInstallerProvided), Util.getSimTypeName(SimType.ModemSimTypeUserContract), Util.getSimTypeName(
                SimType.ModemSimTypeUserPAYG)))
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>> SIM Type: ${BluetoothLeService.service?.device}")
        loopView.selectedItem = BluetoothLeService.service?.device?.simType?.ordinal ?: 0
        binding.txtPinLength.text = pinLength.toString()

        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.footer.btnBack.setOnClickListener {
            when (currentView) {
                1 -> finish()
                2 -> {
                    llPinLength.visibility = View.GONE
                    llSimType.visibility = View.VISIBLE
                    currentView = 1
                }
                3 -> {
                    llSimPin.visibility = View.GONE
                    llPinLength.visibility = View.VISIBLE
                    currentView = 2
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_change_sim_information)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    companion object {
        private val TAG = "ChangeSimInformationActivity"
    }
}