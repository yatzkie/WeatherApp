package com.rpc.weatherapp.weather.history

import android.view.View
import android.view.ViewParent
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rpc.weatherapp.R
import com.rpc.weatherapp.databinding.ItemHistoryBinding

@EpoxyModelClass
abstract class WeatherDataModel: EpoxyModelWithHolder<WeatherDataModel.Holder>() {

    @EpoxyAttribute
    var icon: String? = null

    @EpoxyAttribute
    var temperature: Int? = null

    @EpoxyAttribute
    var description: String? = null

    @EpoxyAttribute
    var location: String? = null

    override fun getDefaultLayout(): Int = R.layout.item_history

    override fun createNewHolder(parent: ViewParent): Holder {
        return Holder(ItemHistoryBinding::bind)
    }

    override fun bind(holder: Holder) {
        val binding = holder.binding ?: return
        Glide
            .with(binding.root)
            .load(icon)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.app_icon_white)
                    .error(R.drawable.app_icon_white)
            )
            .into(binding.weatherIcon)

        binding.cityName.text = location
        binding.weatherDescription.text = description
        binding.temperature.text = "$temperature \u2103"
    }

    class Holder(private val bind: (View) -> ItemHistoryBinding):  EpoxyHolder() {

        var binding: ItemHistoryBinding? = null
        override fun bindView(itemView: View) {
            binding = bind(itemView)
        }

    }
}