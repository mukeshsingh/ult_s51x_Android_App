package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.setName
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeLiftNameBinding

class ChangeLiftNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeLiftNameBinding
    private var liftId: String? = null
    private lateinit var edtName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeLiftNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)
        edtName = binding.editTextLiftName

        if (liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(liftId!!)
            Log.e("", ">>>>>>>>>>>>>>>> ${lift?.liftName ?: ""}")
            edtName.setText(lift?.liftName ?: "")
        }

        binding.btnChangeName.setOnClickListener {
            val name = edtName.text.toString()
            BluetoothLeService.service?.setName(name)
            finish()
        }
        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }
}