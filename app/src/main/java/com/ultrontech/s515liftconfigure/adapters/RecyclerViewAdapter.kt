package com.ultrontech.s515liftconfigure.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ultrontech.s515liftconfigure.*
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.models.Device
import com.ultrontech.s515liftconfigure.models.UserLift

class RecyclerViewAdapter(private val context: Context, private val data: List<UserLift>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.engineer_lifts_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        val viewHolder = holder as ViewHolder

        viewHolder.liftName.text = item.liftName
        with(BluetoothLeService.service) {
            val device = this?.find(item.liftId)

            if (device != null) {
                viewHolder.imgOffline.visibility = View.GONE
                viewHolder.imgOnline.visibility = View.VISIBLE
            } else {
                viewHolder.imgOffline.visibility = View.VISIBLE
                viewHolder.imgOnline.visibility = View.GONE
            }

            if (item.liftType != "") {
                when (item.liftType) {
                    "elevator" -> {
                        viewHolder.imgElevator.visibility = View.VISIBLE
                        viewHolder.imgVerticalLift.visibility = View.GONE
                        viewHolder.imgInclinedLift.visibility = View.GONE
                        viewHolder.imgStairLift.visibility = View.GONE
                    }
                    "verticalLift" -> {
                        viewHolder.imgElevator.visibility = View.GONE
                        viewHolder.imgVerticalLift.visibility = View.VISIBLE
                        viewHolder.imgInclinedLift.visibility = View.GONE
                        viewHolder.imgStairLift.visibility = View.GONE
                    }
                    "inclinedLift" -> {
                        viewHolder.imgElevator.visibility = View.GONE
                        viewHolder.imgVerticalLift.visibility = View.GONE
                        viewHolder.imgInclinedLift.visibility = View.VISIBLE
                        viewHolder.imgStairLift.visibility = View.GONE
                    }
                    "stairLift" -> {
                        viewHolder.imgElevator.visibility = View.GONE
                        viewHolder.imgVerticalLift.visibility = View.GONE
                        viewHolder.imgInclinedLift.visibility = View.GONE
                        viewHolder.imgStairLift.visibility = View.VISIBLE
                    }
                    else -> {
                        viewHolder.imgElevator.visibility = View.GONE
                        viewHolder.imgVerticalLift.visibility = View.GONE
                        viewHolder.imgInclinedLift.visibility = View.GONE
                        viewHolder.imgStairLift.visibility = View.GONE
                    }
                }
            }

            viewHolder.view.setOnClickListener {
                val lift = BluetoothLeService.service?.find(item.liftId)
                if (lift?.modelNumber != null && lift.modelNumber!!.isNotEmpty()) {
                    val intent = Intent(context, UserLiftSettingsActivity::class.java)
                    intent.putExtra(HomeActivity.INTENT_LIFT_ID, item.liftId)
                    context.startActivity(intent)
                } else {
                    context.let { it1 ->
                        S515LiftConfigureApp.instance.basicAlert(
                            it1, context.resources.getString(R.string.lift_not_connected_msg)
                        ){}
                    }
                }
            }

            viewHolder.btnRemove.setOnClickListener {
                viewHolder.btnRemove.visibility = View.GONE
                (context as EngineerHomeActivity).showRemovePopup(item)
            }
            viewHolder.btnConnect.setOnClickListener {
                viewHolder.btnConnect.visibility = View.GONE
                (context as EngineerHomeActivity).showConnectPopup(item)
            }
        }
    }

    private fun linkDevice (item: UserLift) {
        if (item.liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(item.liftId!!)
            if (lift != null) {
                var device = Device(lift = lift)
                BluetoothLeService.service?.link(device)

//                liftName.text = lift?.liftName ?: ""
            }
        }
    }

    fun getItem(position: Int): UserLift {
        return data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val view: View = view
        val liftName: TextView = view.findViewById(R.id.txt_lift_name)
        val imgStairLift: ImageView = view.findViewById(R.id.img_stair_lift)
        val imgInclinedLift: ImageView = view.findViewById(R.id.img_inclined_lift)
        val imgVerticalLift: ImageView = view.findViewById(R.id.img_vertical_lift)
        val imgElevator: ImageView = view.findViewById(R.id.img_elevator)
        val imgOnline: ImageView = view.findViewById(R.id.img_online_icon)
        val imgOffline: ImageView = view.findViewById(R.id.img_offline_icon)
        val btnRemove: LinearLayout = view.findViewById(R.id.llBtnRemove)
        val btnConnect: LinearLayout = view.findViewById(R.id.llBtnConnect)
    }
}