package com.example.kasisave4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExpenseAdapter(private val expenses: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDesc: TextView = view.findViewById(R.id.tvDescription)
        val tvAmt: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCategory: TextView = view.findViewById(R.id.txtCategory)
        val imgReceipt: ImageView = view.findViewById(R.id.imgReceipt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenses[position]
        holder.tvDesc.text = item.description
        holder.tvAmt.text = "R %.2f".format(item.amount)
        holder.tvDate.text = item.date
        holder.tvCategory.text = item.category

        val imageUrl = item.pictureUrl ?: item.receiptUrl

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.imgReceipt)
        } else {
            holder.imgReceipt.setImageResource(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount(): Int = expenses.size
}
