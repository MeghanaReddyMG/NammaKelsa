package com.nammakelsa.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nammakelsa.R
import com.nammakelsa.data.model.Booking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingAdapter(
    private var bookings: List<Booking>,
    private val userType: String,
    private val onAction: (Booking) -> Unit,
    private val onDecline: (Booking) -> Unit,
    private val onRate: (Booking) -> Unit = {}
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPartyName: TextView = view.findViewById(R.id.tvPartyName)
        val tvDateTime: TextView = view.findViewById(R.id.tvBookingDateTime)
        val tvAddress: TextView = view.findViewById(R.id.tvBookingAddress)
        val tvPrice: TextView = view.findViewById(R.id.tvBookingPrice)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnAction: Button = view.findViewById(R.id.btnAction)
        val btnDecline: Button = view.findViewById(R.id.btnDecline)
        val btnRate: Button = view.findViewById(R.id.btnRate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        
        holder.tvPartyName.text = if (userType == "worker") "Customer: ${booking.customerId.takeLast(6)}" else "Worker: ${booking.workerId.takeLast(6)}"
        
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
        holder.tvDateTime.text = "${sdf.format(Date(booking.date))} (${booking.startTime} - ${booking.endTime})"
        holder.tvAddress.text = booking.address
        holder.tvPrice.text = "₹${booking.totalPrice}"
        holder.tvStatus.text = "Status: ${booking.status.uppercase()}"

        // Reset visibility
        holder.btnAction.visibility = View.GONE
        holder.btnDecline.visibility = View.GONE
        holder.btnRate.visibility = View.GONE

        if (userType == "worker") {
            if (booking.status == "pending") {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "ACCEPT"
                holder.btnDecline.visibility = View.VISIBLE
            } else if (booking.status == "accepted") {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "MARK COMPLETED"
            }
        } else {
            // Customer view
            if (booking.status == "completed") {
                holder.btnRate.visibility = View.VISIBLE
            } else if (booking.status == "pending") {
                holder.btnDecline.visibility = View.VISIBLE
                holder.btnDecline.text = "CANCEL"
            }
        }

        holder.btnAction.setOnClickListener { onAction(booking) }
        holder.btnDecline.setOnClickListener { onDecline(booking) }
        holder.btnRate.setOnClickListener { onRate(booking) }
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
