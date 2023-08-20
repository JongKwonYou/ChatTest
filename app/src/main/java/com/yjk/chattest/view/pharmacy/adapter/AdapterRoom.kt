package com.yjk.chattest.view.pharmacy.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.yjk.chattest.data.ChatConstants
import com.yjk.chattest.data.chat.ChatData
import com.yjk.chattest.databinding.AdapterRoomBinding
import com.yjk.chattest.databinding.MessageNormalBinding

class AdapterRoom(val myId: String, val itemClick: (GroupChannel) -> Unit) :
    RecyclerView.Adapter<AdapterRoom.RoomItemHolder>() {

    private var list = mutableListOf<GroupChannel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomItemHolder {
        return RoomItemHolder(
            AdapterRoomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RoomItemHolder, position: Int) {
        holder.onBind(myId, list.get(position), itemClick)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addItem(item: GroupChannel) {
        this.list.add(item)
        notifyItemInserted(list.size)
    }

    fun addList(list: MutableList<GroupChannel>) {
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun setList(list: MutableList<GroupChannel>) {
        this.list = list
        notifyDataSetChanged()
    }

    class RoomItemHolder(val binding: AdapterRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(myId:String, data : GroupChannel, onClick : (GroupChannel) -> Unit) {
            // 상대방 유저 가져오기
            for(member in data.members){
                if(member.userId != myId){
                    binding.tvName.text = member.nickname
                }
            }

            binding.root.setOnClickListener {
                onClick(data)
            }
        }
    }



}