package com.ultrontech.s515liftconfigure.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ultrontech.s515liftconfigure.FindLiftActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.S515LiftConfigureApp
import com.ultrontech.s515liftconfigure.bluetooth.ScanDisplayItem
import java.util.*

class LiftListAdapter(private val context: Context,
                      private val lifts: List<ScanDisplayItem>
) : BaseAdapter() {
    var findLiftActivity: FindLiftActivity = context as FindLiftActivity
    var selectedId: UUID? = null
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return lifts.size
    }

    override fun getItem(position: Int): Any {
        return lifts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val liftView = inflater.inflate(R.layout.lift_list_item, parent, false)
        val txtLiftName = liftView.findViewById<TextView>(R.id.txt_lift_name)
        val btnAddLift = liftView.findViewById<ImageButton>(R.id.btnAddLift)

        val userLift = S515LiftConfigureApp.profileStore.find(lifts[position].id)
        if (userLift != null) {
            btnAddLift.visibility = View.GONE
        } else {
            btnAddLift.visibility = View.VISIBLE
        }

        btnAddLift.setOnClickListener {
            findLiftActivity.goToAddLift(lifts[position])
        }
        txtLiftName.text = lifts[position].name

        return liftView
    }
}