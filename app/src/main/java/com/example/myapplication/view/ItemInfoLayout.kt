package com.example.myapplication.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.databinding.ItemInfoLayoutBinding


/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/1/6 13:32
 *    author : Roy
 *    version: 1.0
 */
class ItemInfoLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr)  {

    var _binding : ItemInfoLayoutBinding?=null
    val binding get() = _binding!!

    init {
        initView()
    }

    private fun initView() {
        _binding =  ItemInfoLayoutBinding.inflate(LayoutInflater.from(context), this)
//        _binding?.loadingPagImageView?.composition = PAGFile.Load(context.assets, PAG_REST)
//        _binding

        binding.tvAsterisk  //星号
        binding.tvName //名称
        binding.tvId  //身份证
        binding.ivAvatar //头像
        binding.ivArrow //箭头


      /*  Glide.with(imageView).load(url).placeholder(R.mipmap.ic_default_member_avatar)
            .error(R.mipmap.ic_default_member_avatar).into(imageView)*/
    }
}