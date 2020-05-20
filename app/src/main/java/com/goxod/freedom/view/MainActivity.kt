package com.goxod.freedom.view


import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.goxod.freedom.BuildConfig
import com.goxod.freedom.R
import com.goxod.freedom.config.ApiConstants
import com.goxod.freedom.config.type.ApiItem
import com.goxod.freedom.config.type.SecondaryItem
import com.goxod.freedom.data.adapter.ItemAdapter
import com.goxod.freedom.data.db.LocalVideo
import com.goxod.freedom.data.entity.CategoryEntity
import com.goxod.freedom.data.entity.PageEntity
import com.goxod.freedom.data.event.ErrorEvent
import com.goxod.freedom.utils.JsEngine
import com.goxod.freedom.utils.S
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.permissions.OnPermission
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.XPopupImageLoader
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.view.IconicsImageView
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.AbstractDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.x_player.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


class MainActivity : AppCompatActivity(){

    private var mRefresher: SmartRefreshLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter = ItemAdapter()
    private var skeleton: SkeletonScreen? = null
    private var mPage = 1
    private var currentApi = ApiItem.values().first()
    private var mTextColor: ColorStateList? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private var loopAnimation: Animation? = null
    private var allowClick = true
    private var dialog: MaterialDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        immersionBar {
            titleBar(toolbar)
        }
        XXPermissions.with(this).permission(Permission.Group.STORAGE)
            .request(object : OnPermission {
                override fun noPermission(denied: MutableList<String>?, quick: Boolean) {
                    S.postError("文件访问授权失败", "将可能无法使用部分功能")
                }

                override fun hasPermission(granted: MutableList<String>?, all: Boolean) {

                }
            })
        initDrawerView()
        initDataView()
        initNormalView()
        initVideoView()
    }

    private fun initVideoView() {
        x_player.apply {
            back.setOnClickListener {
                pauseAndHideVideo()
            }
            switch_size.visibility = View.GONE
            change_speed.visibility = View.GONE
            change_rotate.visibility = View.GONE
            setIsTouchWiget(false)
            fullscreen.setOnClickListener {
                val seekTo = x_player.currentPositionWhenPlaying.toLong();
                pauseAndHideVideo()
                VideoActivity.start(
                    this@MainActivity,
                    pageItem,
                    seekTo
                )
            }
        }
    }

    private fun initNormalView() {
        dialog = XDialog.tips(this)
    }


    private fun initDrawerView() {
        loopAnimation = AnimationUtils.loadAnimation(
            this,
            R.anim.loop_anim
        )
        mTextColor = resources.getColorStateList(R.color.color_drawer, null)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setTitle(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            root,
            toolbar,
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close
        )
        toolbar.doubleClick {
            refreshAndChangeTitle()
        }
        val line = SectionDrawerItem().apply {
            nameText = resources.getString(R.string.app_name) + "   v" + BuildConfig.VERSION_NAME
            textColor = mTextColor
        }
        val items = mutableListOf<IDrawerItem<*>>()
        ApiItem.values().map {
            items.add(generatePrimary(it))
        }
        items.add(line)
        SecondaryItem.values().map {
            items.add(generateSecondary(it))
        }
        slider.apply {
            itemAdapter.add(
                items
            )
            onDrawerItemClickListener = { _, drawerItem, _ ->
                clickDrawerItemAction(drawerItem)
                false
            }
            setSelection(currentApi.hashCode().toLong())
        }
    }

    private fun clickDrawerItemAction(drawerItem: IDrawerItem<*>) {
        if (drawerItem is AbstractDrawerItem<*, *>) {
            if (drawerItem.tag is ApiItem) {
                clickPrimaryItem(drawerItem.tag as ApiItem)
            } else if (drawerItem.tag is SecondaryItem) {
                clickSecondaryItem(drawerItem.tag as SecondaryItem)
            }
        }
    }


    private fun generatePrimary(item: ApiItem): PrimaryDrawerItem {
        return PrimaryDrawerItem().apply {
            item.api.fetchRealHost()
            tag = item
            identifier = item.hashCode().toLong()
            nameText = item.title
            textColor = resources.getColorStateList(R.color.color_drawer_title, null)
        }
    }

    private fun generateSecondary(item: SecondaryItem): SecondaryDrawerItem {
        return SecondaryDrawerItem().apply {
            tag = item
            identifier = item.hashCode().toLong()
            iconicsIcon = item.icon
            iconColor = mTextColor
            textColor = mTextColor
            nameText = item.name
            isSelectable = false
        }
    }

    private fun clickPrimaryItem(item: ApiItem) {
        if (currentApi == item) {
            return
        }
        currentApi = item
        currentApi.api.categoryIndex = 0
        refreshAndChangeTitle()
    }

    private fun clickSecondaryItem(item: SecondaryItem) {
        when (item) {
            SecondaryItem.SETTINGS -> {
                XDialog.settings(this)
            }
            SecondaryItem.ABOUT -> {
                XDialog.about(this)
            }
            SecondaryItem.CLOSE -> {
                XDialog.close(this)
            }
        }
    }


    private fun refreshAndChangeTitle() {
        pauseAndHideVideo()
        mRefresher?.setNoMoreData(false)
        mAdapter.setNewData(arrayListOf())
        supportActionBar?.title =
            currentApi.title + " - " + currentApi.api.categories[currentApi.api.categoryIndex].category
        mRefresher!!.autoRefresh()
    }

    private fun initDataView() {
        mRefresher = refreshLayout.apply {
            setEnableLoadMoreWhenContentNotFull(false)
            setOnRefreshListener { onRefresh() }
            setOnLoadMoreListener { onLoadMore(false) }

        }
        mRecyclerView = recyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter.apply {
                addChildClickViewIds(
                    R.id.btn_play,
                    R.id.iv_cover,
                    R.id.btn_preview,
                    R.id.btn_favorite
                )
                setOnItemChildClickListener { _, view, position ->
                    if (allowClick) {
                        when (view.id) {
                            R.id.btn_play -> {
                                loadAndPlayVideos(view, position, false)
                            }
                            R.id.iv_cover -> {
                                showImage(view as ImageView,  mAdapter.getItem(position))
                            }
                            R.id.btn_preview -> {
                                loadAndPlayVideos(view, position, true)
                            }
                            R.id.btn_favorite -> {
                                collection(position)
                            }
                        }
                    }
                }
                val emptyView = layoutInflater.inflate(R.layout.empty_view, null)
                emptyView.setOnClickListener {
                    onRefresh()
                }
                setEmptyView(emptyView)
            }
        }
        skeleton = Skeleton.bind(mRecyclerView).adapter(mAdapter).apply {
            adapter(mAdapter)
            load(R.layout.item_video)
            count(5)
        }.show()
        refreshAndChangeTitle()
    }

    private fun collection(position: Int) {
        XDialog.collection(
            this@MainActivity,
            mAdapter.getItem(position)
        )
    }


    private fun showImage(v: ImageView, entity: PageEntity) {
        var url = entity.cover
        S.log("showImage thumb = $url")
        if(entity.apiId == ApiItem.API_40001.apiId){
            url = url.replace("320x180/1","preview")
            S.log("showImage preview = $url")
        }
        XPopup.Builder(this@MainActivity)
            .asImageViewer(v, url, object : XPopupImageLoader {
                override fun loadImage(position: Int, uri: Any, imageView: ImageView) {
                    Glide.with(imageView).load(uri).into(imageView)
                }

                override fun getImageFile(context: Context, uri: Any): File? {
                    try {
                        return Glide.with(context).downloadOnly().load(uri).submit().get()
                    } catch (e: Exception) {
                        S.log("" + e.localizedMessage)
                    }
                    return null
                }
            })
            .isShowSaveButton(false)
            .show()
    }

    private fun loadAndPlayVideos(view: View, position: Int, isPreview: Boolean) {
        val item = mAdapter.getItem(position)
        if (isPreview) {
            x_player.visibility = View.VISIBLE
            x_player.setUp(item, true)
            return
        } else {
            /**
             * 不为空且文件存在时，直接播放
             * @see com.goxod.freedom.service.ApiAbstract.checkFavoriteAndGoods
             */
            if(item.goods.isNotEmpty()){
                S.log("item.goods[0].url = " + item.goods[0].definition + " / " + item.goods[0].url)
                if(File(item.goods[0].url).exists()) {
                    VideoActivity.start(this, item, 0)
                }
//                else{
//                    MaterialDialog(this).show {
//                        title(text = "本地文件损坏")
//                        message(text = "本地文件已被删除或损坏,请重新点击进行在线播放或下载")
//                        noAutoDismiss()
//                        positiveButton(text = "好的"){
//                            LocalVideo(item.url).apply {
//                                favoriteType = -1
//                            }.deleteAndNotify(item)
//                            dismiss()
//                        }
//                    }
//                }
                return
            }
            if (!S.checkInternetAvailable(this)) {
                return
            }
            loadingAnimation(view, true)
            GlobalScope.launch(Dispatchers.Default) {
                item.goods.clear()
                item.goods.addAll(currentApi.api.loadGoodsItems(this@MainActivity, item))
                runOnUiThread {
                    loadingAnimation(view, false)
                    if (item.goods.isEmpty()) {
                        S.error(this@MainActivity,"暂无视频")
                    } else {
                        x_player.visibility = View.VISIBLE
                        x_player.setUp(item, false)
                    }
                }
            }
        }
    }

    private fun loadingAnimation(v: View, loading: Boolean) {
        if (v is IconicsImageView) {
            if (loading) {
                v.apply {
                    icon = IconicsDrawable(v.context, FontAwesome.Icon.faw_spinner.name)
                    icon?.colorRes = R.color.color_favorite_selector
                    startAnimation(loopAnimation)
                    isClickable = false
                    isEnabled = false
                }
                allowClick = false
            } else {
                v.apply {
                    icon = IconicsDrawable(v.context, FontAwesome.Icon.faw_play_circle.name)
                    icon?.colorRes = R.color.color_favorite_selector
                    clearAnimation()
                    isClickable = true
                    isEnabled = true
                }
                allowClick = true
            }
        }
    }

    private fun onRefresh() {
        mPage = currentApi.api.originPage()
        onLoadMore(true)
    }

    private fun showSkeleton(show: Boolean) {
        if (mAdapter.data.isEmpty()) {
            if (show) {
                skeleton?.show()
            } else {
                skeleton?.hide()
            }
        }
    }

    private fun onLoadMore(refresh: Boolean) {
        if (!S.checkInternetAvailable(this)) {
            mRefresher?.finishLoadMoreWithNoMoreData()
            showSkeleton(false)
            return
        }
        showSkeleton(true)
        GlobalScope.launch(Dispatchers.Default) {
            val list: MutableList<PageEntity>? = currentApi.api.loadPageItems(mPage++)
            runOnUiThread {
                showSkeleton(false)
                if (list != null && list.isNotEmpty()) {
                    if (refresh) {
                        mAdapter.setNewData(list)
                        if(list.size < currentApi.api.pageSize()){
                            mRefresher!!.finishRefreshWithNoMoreData()

                        }else{
                            mRefresher!!.finishRefresh()
                        }
                        return@runOnUiThread
                    }
                    mAdapter.addData(list)
                    if(list.size < currentApi.api.pageSize()){
                        mRefresher!!.finishLoadMoreWithNoMoreData()
                    }else{
                        mRefresher!!.finishLoadMore()
                    }
                    return@runOnUiThread
                }
                if (refresh) {
                    mRefresher!!.finishRefreshWithNoMoreData()
                    return@runOnUiThread
                } else {
                    mRefresher!!.finishLoadMoreWithNoMoreData()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.category_btn -> {
                XPopup.Builder(this)
                    .atView(toolbar)
                    .asAttachList(
                        currentApi.api.categories.map(CategoryEntity::category).toTypedArray(),
                        null
                    ) { index, _ ->
                        if(ApiConstants.SEARCH == currentApi.api.categories[index].category){
                            MaterialDialog(this).show {
                                title(text = ApiConstants.SEARCH)
                                message(text = "如果沒有結果，关键字中間加個空格:D")
                                input(hint = "請輸入車牌或關鍵字",allowEmpty = false){
                                        _, txt ->
                                    currentApi.api.searchKey = txt.toString().trim()
                                    if(currentApi.api.searchKey.isNotBlank()){
                                        currentApi.api.categoryIndex = index
                                        refreshAndChangeTitle()
                                        dismiss()
                                    }else{
                                        getInputField().setText("")
                                    }
                                }
                                noAutoDismiss()
                                positiveButton(text = "搞快點")
                                negativeButton(text = "取消"){
                                    dismiss()
                                }
                            }
                        }else{
                            if (currentApi.api.categoryIndex == index) return@asAttachList
                            currentApi.api.categoryIndex = index
                            refreshAndChangeTitle()
                        }
                    }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (x_player.visibility == View.VISIBLE) {
                    pauseAndHideVideo()
                    return true
                }
                return S.quitClick(this, event, "再按一次返回桌面", true)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object ViewClickDelay {
        private var lastClickTime: Long = 0L
        private const val DOUBLE_CLICK_DELAY: Long = 1000L
    }

    private infix fun View.doubleClick(clickAction: () -> Unit) {
        this.setOnClickListener {
            if (lastClickTime == 0L) {
                lastClickTime = System.currentTimeMillis()
            } else {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < DOUBLE_CLICK_DELAY) {
                    lastClickTime = 0L
                    clickAction()
                } else {
                    lastClickTime = System.currentTimeMillis()
                }
            }
        }
    }

    private fun pauseAndHideVideo() {
        GSYVideoManager.onPause()
        GSYVideoManager.releaseAllVideos()
        x_player.visibility = View.GONE
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onError(event: ErrorEvent) {
        S.log("ERROR = " + event.title + " / " + event.message)
        dialog?.apply {
            title(text = event.title)
            message(text = event.message)
        }
        dialog?.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            GSYVideoManager.releaseAllVideos()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        JsEngine.unRegister()
        EventBus.getDefault().unregister(this)
    }

    override fun onStop() {
        super.onStop()
        try {
            GSYVideoManager.onPause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            GSYVideoManager.onPause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            GSYVideoManager.onResume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
