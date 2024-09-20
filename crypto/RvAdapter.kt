package com.example.crypto
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.crypto.databinding.RvItemBinding


class RvAdapter(val context:Context, var data:ArrayList<Model>):RecyclerView.Adapter<RvAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: RvItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= RvItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setAnimation(holder.itemView)
        holder.binding.name.text=data.get(position).name
        holder.binding.symbol.text=data.get(position).symbol
        holder.binding.price.text=data.get(position).price
    }
    fun setAnimation (view: View){
        val anim= AlphaAnimation(0.0f,1.0f)
        anim.duration=1000
        view.startAnimation(anim)
    }

    fun changeData(filterData: ArrayList<Model>) {
        data=filterData
        notifyDataSetChanged()

    }
}