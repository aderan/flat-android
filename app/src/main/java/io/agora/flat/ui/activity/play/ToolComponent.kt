package io.agora.flat.ui.activity.play

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.herewhite.sdk.domain.ViewMode
import io.agora.flat.Constants
import io.agora.flat.R
import io.agora.flat.data.model.ClassModeType
import io.agora.flat.data.model.RoomStatus
import io.agora.flat.databinding.ComponentToolBinding
import io.agora.flat.event.RoomsUpdated
import io.agora.flat.ui.animator.SimpleAnimator
import io.agora.flat.ui.view.InviteDialog
import io.agora.flat.ui.view.OwnerExitDialog
import io.agora.flat.ui.viewmodel.ClassRoomEvent
import io.agora.flat.ui.viewmodel.ClassRoomState
import io.agora.flat.ui.viewmodel.ClassRoomViewModel
import io.agora.flat.util.FlatFormatter
import io.agora.flat.util.dp2px
import io.agora.flat.util.showToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class ToolComponent(
    activity: ClassRoomActivity,
    rootView: FrameLayout,
) : BaseComponent(activity, rootView) {
    companion object {
        val TAG = ToolComponent::class.simpleName
    }

    private lateinit var binding: ComponentToolBinding
    private lateinit var toolAnimator: SimpleAnimator

    private val viewModel: ClassRoomViewModel by activity.viewModels()

    private lateinit var cloudStorageAdapter: CloudStorageAdapter
    private lateinit var userListAdapter: UserListAdapter

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        initView()
        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.roomEvent.collect {
                when (it) {
                    is ClassRoomEvent.OperatingAreaShown -> handleAreaShown(it.areaId)
                    is ClassRoomEvent.StartRoomResult -> {; }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.messageUsers.collect {
                userListAdapter.setDataSet(it)
                binding.userlistDot.isVisible = it.find { user -> user.isRaiseHand } != null
            }
        }

        lifecycleScope.launch {
            viewModel.roomConfig.collect {
                binding.layoutSettings.switchVideo.isChecked = it.enableVideo
                binding.layoutSettings.switchAudio.isChecked = it.enableAudio
            }
        }

        lifecycleScope.launch {
            viewModel.state.filter { it != ClassRoomState.Init }.collect {
                binding.roomCtrlTool.isVisible = it.isOwner
                if (it.isOwner) {
                    binding.roomStart.isVisible = it.roomStatus == RoomStatus.Idle
                    binding.roomStateSetting.isVisible = it.roomStatus != RoomStatus.Idle

                    if (it.roomStatus == RoomStatus.Started) {
                        binding.layoutRoomStateSettings.recordDisplayingLy.isVisible = it.isRecording
                        binding.layoutRoomStateSettings.startRecord.isVisible = !it.isRecording
                        binding.layoutRoomStateSettings.stopRecord.isVisible = it.isRecording
                        if (it.recordState != null) {
                            binding.layoutRoomStateSettings.recordTime.text =
                                FlatFormatter.timeMS(it.recordState.recordTime * 1000)
                        }
                        binding.layoutRoomStateSettings.modeLayout.isVisible = it.showChangeClassMode
                    }

                    updateClassMode(it.classMode)
                }

                binding.viewfollow.isVisible = it.isWritable
                binding.viewfollow.isSelected = it.viewMode == ViewMode.Broadcaster

                binding.cloudservice.isVisible = it.isWritable
            }
        }

        lifecycleScope.launch {
            viewModel.cloudStorageFiles.collect {
                cloudStorageAdapter.setDataSet(it)
                binding.layoutCloudStorage.listEmpty.isVisible = it.isEmpty()
            }
        }

        lifecycleScope.launch {
            viewModel.messageAreaShown.collect {
                binding.message.isSelected = it
            }
        }

        lifecycleScope.launch {
            viewModel.messageCount.collect {
                binding.messageDot.isVisible = it > 0 && !viewModel.messageAreaShown.value
            }
        }
    }

    private fun handleAreaShown(areaId: Int) {
        if (areaId != ClassRoomEvent.AREA_ID_MESSAGE) {
            viewModel.setMessageAreaShown(false)
        }

        if (areaId != ClassRoomEvent.AREA_ID_SETTING) {
            hideSettingLayout()
        }

        if (areaId != ClassRoomEvent.AREA_ID_CLOUD_STORAGE) {
            hideCloudStorageLayout()
        }

        if (areaId != ClassRoomEvent.AREA_ID_USER_LIST) {
            hideUserListLayout()
        }

        if (areaId != ClassRoomEvent.AREA_ID_ROOM_STATE_SETTING) {
            hideRoomStateSettings()
        }
    }

    private fun hideRoomStateSettings() {
        binding.layoutRoomStateSettings.root.isVisible = false
        binding.roomStateSetting.isSelected = false
    }

    private fun showRoomStateSettings() {
        binding.layoutRoomStateSettings.root.isVisible = true
        binding.roomStateSetting.isSelected = true
    }

    private fun hideSettingLayout() {
        binding.layoutSettings.settingLayout.isVisible = false
        binding.setting.isSelected = false
    }

    private fun showSettingLayout() {
        binding.layoutSettings.settingLayout.isVisible = true
        binding.setting.isSelected = true
    }

    private fun hideCloudStorageLayout() {
        binding.layoutCloudStorage.root.isVisible = false
        binding.cloudservice.isSelected = false
    }

    private fun showCloudStorageLayout() {
        binding.layoutCloudStorage.root.isVisible = true
        binding.cloudservice.isSelected = true
    }

    private fun hideUserListLayout() {
        binding.layoutUserList.root.isVisible = false
        binding.userlist.isSelected = false
    }

    private fun showUserListLayout() {
        binding.layoutUserList.root.isVisible = true
        binding.userlist.isSelected = true
    }

    private fun initView() {
        binding = ComponentToolBinding.inflate(activity.layoutInflater, rootView, true)

        val map: Map<View, (View) -> Unit> = mapOf(
            binding.message to {
                binding.messageDot.isVisible = false
                val shown = !viewModel.messageAreaShown.value
                viewModel.setMessageAreaShown(shown)
                viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_MESSAGE, shown)
            },
            binding.cloudservice to {
                with(binding.layoutCloudStorage.root) {
                    if (isVisible) {
                        hideCloudStorageLayout()
                    } else {
                        showCloudStorageLayout()
                        viewModel.requestCloudStorageFiles()
                    }
                    viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_CLOUD_STORAGE, isVisible)
                }
            },
            binding.userlist to {
                with(binding.layoutUserList.root) {
                    if (isVisible) {
                        hideUserListLayout()
                    } else {
                        showUserListLayout()
                    }
                    viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_USER_LIST, isVisible)
                }
            },
            binding.invite to {
                showInviteDialog()
                binding.invite.isSelected = true
            },
            binding.setting to {
                with(binding.layoutSettings.settingLayout) {
                    if (isVisible) {
                        hideSettingLayout()
                    } else {
                        showSettingLayout()
                    }
                    viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_SETTING, isVisible)
                }
            },
            binding.collapse to { toolAnimator.hide() },
            binding.expand to { toolAnimator.show() },
            binding.layoutSettings.exit to { handleExit() },

            binding.roomStart to {
                viewModel.startClass()
            },
            binding.roomStateSetting to {
                with(binding.layoutRoomStateSettings.root) {
                    if (isVisible) {
                        hideRoomStateSettings()
                    } else {
                        showRoomStateSettings()
                    }
                    viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_ROOM_STATE_SETTING, isVisible)
                }
            },
            binding.layoutRoomStateSettings.startRecord to {
                viewModel.startRecord()
            },
            binding.layoutRoomStateSettings.stopRecord to {
                viewModel.stopRecord()
            },
            binding.layoutRoomStateSettings.classModeInteraction to {
                if (!it.isSelected) {
                    updateClassMode(ClassModeType.Interaction)
                    viewModel.updateClassMode(ClassModeType.Interaction)
                }
            },
            binding.layoutRoomStateSettings.classModeLecture to {
                if (!it.isSelected) {
                    updateClassMode(ClassModeType.Lecture)
                    viewModel.updateClassMode(ClassModeType.Lecture)
                }
            },
            binding.viewfollow to {
                val targetMode = if (it.isSelected) ViewMode.Freedom else ViewMode.Broadcaster
                viewModel.updateViewMode(targetMode)
            }
        )

        map.forEach { (view, action) -> view.setOnClickListener { action(it) } }

        toolAnimator = SimpleAnimator(
            onUpdate = ::onUpdateTool,
            onShowEnd = {
                binding.collapse.visibility = View.VISIBLE
                binding.expand.visibility = View.INVISIBLE
                resetToolsLayoutParams()
            },
            onHideEnd = {
                binding.collapse.visibility = View.INVISIBLE
                binding.expand.visibility = View.VISIBLE
            }
        )

        binding.layoutSettings.switchVideoArea.isChecked = viewModel.videoAreaShown.value
        binding.layoutSettings.switchVideoArea.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVideoAreaShown(isChecked)
            hideSettingLayout()
        }

        binding.layoutSettings.switchVideo.setOnCheckedChangeListener { _, isChecked ->
            viewModel.enableVideo(isChecked)
        }

        binding.layoutSettings.switchAudio.setOnCheckedChangeListener { _, isChecked ->
            viewModel.enableAudio(isChecked)
        }

        cloudStorageAdapter = CloudStorageAdapter()
        cloudStorageAdapter.setOnItemClickListener {
            viewModel.insertCourseware(it)
            hideCloudStorageLayout()
        }
        binding.layoutCloudStorage.cloudStorageList.adapter = cloudStorageAdapter
        binding.layoutCloudStorage.cloudStorageList.layoutManager = LinearLayoutManager(activity)

        userListAdapter = UserListAdapter(viewModel)
        binding.layoutUserList.userList.adapter = userListAdapter
        binding.layoutUserList.userList.layoutManager = LinearLayoutManager(activity)
    }

    private fun updateClassMode(classMode: ClassModeType) {
        binding.layoutRoomStateSettings.classModeInteraction.isSelected = classMode == ClassModeType.Interaction
        binding.layoutRoomStateSettings.classModeLecture.isSelected = classMode == ClassModeType.Lecture
    }

    private fun handleExit() {
        if (viewModel.state.value.needOwnerExitDialog) {
            showOwnerExitDialog()
        } else {
            viewModel.sendGlobalEvent(RoomsUpdated)
            activity.finish()
            // showAudienceExitDialog()
        }
    }

    private fun showOwnerExitDialog() {
        val dialog = OwnerExitDialog()
        dialog.setListener(object : OwnerExitDialog.Listener {
            override fun onClose() {

            }

            // 挂起房间
            override fun onLeftButtonClick() {
                viewModel.sendGlobalEvent(RoomsUpdated)
                activity.finish()
            }

            // 结束房间
            override fun onRightButtonClick() {
                lifecycleScope.launch {
                    if (viewModel.stopClass()) {
                        viewModel.sendGlobalEvent(RoomsUpdated)
                        activity.finish()
                    } else {
                        activity.showToast(R.string.room_class_stop_class_fail)
                    }
                }
            }

            override fun onDismiss() {
                viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_OWNER_EXIT_DIALOG, false)
            }
        })
        dialog.show(activity.supportFragmentManager, "OwnerExitDialog")
        viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_OWNER_EXIT_DIALOG, true)
    }

    private fun showAudienceExitDialog() {

    }

    private fun showInviteDialog() {
        val state = viewModel.state.value
        val inviteTitle = activity.getString(R.string.invite_title_format, state.userName)
        val roomTime =
            "${FlatFormatter.date(state.beginTime)} ${FlatFormatter.timeDuring(state.beginTime, state.endTime)}"
        val inviteLink = Constants.BASE_INVITE_URL + "/join/" + state.roomUUID

        val copyText = """
            |${activity.getString(R.string.invite_title_format, state.userName)}
            |${activity.getString(R.string.invite_room_name_format, state.title)}
            |${activity.getString(R.string.invite_begin_time_format, roomTime)}
            |${activity.getString(R.string.invite_room_number_format, state.roomUUID)}
            |${activity.getString(R.string.invite_join_link_format, inviteLink)}
            """.trimMargin()

        val dialog = InviteDialog().apply {
            arguments = Bundle().apply {
                putString(InviteDialog.INVITE_TITLE, inviteTitle)
                putString(InviteDialog.ROOM_TITLE, state.title)
                putString(InviteDialog.ROOM_NUMBER, state.roomUUID.substringAfterLast("-"))
                putString(InviteDialog.ROOM_TIME, roomTime)
            }
        }
        dialog.setListener(object : InviteDialog.Listener {
            override fun onCopy() {
                viewModel.onCopyText(copyText)
                activity.showToast(R.string.copy_success)
            }

            override fun onHide() {
                binding.invite.isSelected = false
                viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_INVITE_DIALOG, false)
            }
        })
        dialog.show(activity.supportFragmentManager, "InviteDialog")
        viewModel.notifyOperatingAreaShown(ClassRoomEvent.AREA_ID_INVITE_DIALOG, true)
    }

    private val collapseHeight = activity.dp2px(32)
    private val expandHeight: Int
        get() {
            val visibleCount = binding.extTools.children.count { it.isVisible }
            return activity.dp2px(32 * visibleCount)
        }

    private fun onUpdateTool(value: Float) {
        val layoutParams = binding.extTools.layoutParams
        layoutParams.height = collapseHeight + (value * (expandHeight - collapseHeight)).toInt()
        binding.extTools.layoutParams = layoutParams
    }

    // TODO free layoutParams height for visible change of items
    private fun resetToolsLayoutParams() {
        val layoutParams = binding.extTools.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.extTools.layoutParams = layoutParams
    }
}
