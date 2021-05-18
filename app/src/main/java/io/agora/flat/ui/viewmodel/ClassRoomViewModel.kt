package io.agora.flat.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.flat.Constants
import io.agora.flat.common.FlatException
import io.agora.flat.data.AppDatabase
import io.agora.flat.data.AppKVCenter
import io.agora.flat.data.ErrorResult
import io.agora.flat.data.Success
import io.agora.flat.data.model.*
import io.agora.flat.data.repository.RoomRepository
import io.agora.flat.di.AppModule
import io.agora.flat.di.interfaces.RtmEngineProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ClassRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val savedStateHandle: SavedStateHandle,
    private val database: AppDatabase,
    @AppModule.GlobalData private val appKVCenter: AppKVCenter,
    private val rtmApi: RtmEngineProvider
) : ViewModel() {
    private var _roomPlayInfo = MutableStateFlow<RoomPlayInfo?>(null)
    val roomPlayInfo = _roomPlayInfo.asStateFlow()

    private var _state: MutableStateFlow<ClassRoomState>
    val state: StateFlow<ClassRoomState>
        get() = _state

    // 缓存用户信息，降低web服务压力
    private var _usersCacheMap: MutableMap<String, RtcUser> = mutableMapOf()
    private var _usersMap = MutableStateFlow<Map<String, RtcUser>>(emptyMap())
    val usersMap = _usersMap.asStateFlow()

    private var _videoAreaShown = MutableStateFlow(true)
    val videoAreaShown = _videoAreaShown.asStateFlow()

    private var _roomEvent = MutableStateFlow<ClassRoomEvent?>(null)
    val roomEvent = _roomEvent
    val uuid = AtomicInteger(0)

    private var _roomConfig = MutableStateFlow(RoomConfig(""))
    val roomConfig = _roomConfig.asStateFlow()

    var roomUUID: String
    var currentUserUUID: String

    init {
        roomUUID = intentValue(Constants.IntentKey.ROOM_UUID)
        currentUserUUID = appKVCenter.getUserInfo()!!.uuid
        _state = MutableStateFlow(ClassRoomState(roomUUID = roomUUID, currentUserUUID = currentUserUUID))

        viewModelScope.launch {
            when (val result =
                roomRepository.joinRoom(roomUUID)) {
                is Success -> {
                    _roomPlayInfo.value = result.data
                }
                is ErrorResult -> {

                }
            }
        }

        viewModelScope.launch {
            when (val result = roomRepository.getOrdinaryRoomInfo(roomUUID)) {
                is Success -> result.data.run {
                    _state.value = _state.value.copy(
                        roomType = roomInfo.roomType,
                        ownerUUID = roomInfo.ownerUUID,
                        ownerName = roomInfo.ownerName,
                        title = roomInfo.title,
                        beginTime = roomInfo.beginTime,
                        endTime = roomInfo.endTime,
                        roomStatus = roomInfo.roomStatus,
                    )
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            _roomConfig.value = database.roomConfigDao().getConfigById(roomUUID) ?: RoomConfig(roomUUID)
        }
    }

    fun isVideoEnable(): Boolean {
        return _roomConfig.value.enableVideo
    }

    fun isAudioEnable(): Boolean {
        return _roomConfig.value.enableVideo
    }

    fun enableVideo(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = _roomConfig.value.copy(enableVideo = enable)

            database.roomConfigDao().insertOrUpdate(config)
            _roomConfig.value = config

            sendDeviceState(config)
        }
    }

    // 发送本地音视频状态更新消息
    private suspend fun sendDeviceState(config: RoomConfig) {
        val event = RTMEvent.DeviceState(
            DeviceStateValue(
                userUUID = appKVCenter.getUserInfo()!!.uuid,
                camera = config.enableVideo,
                mic = config.enableAudio
            )
        )
        rtmApi.sendChannelCommand(event)
        updateDeviceState(event.value)
    }

    fun enableAudio(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = _roomConfig.value.copy(enableAudio = enable)

            database.roomConfigDao().insertOrUpdate(config)
            _roomConfig.value = config

            sendDeviceState(config)
        }
    }

    suspend fun initRoomUsers(usersUUIDs: List<String>): Boolean = suspendCoroutine { cont ->
        viewModelScope.launch {
            when (val result = roomRepository.getRoomUsers(roomUUID, usersUUIDs)) {
                is Success -> {
                    result.data.forEach { (uuid, user) -> user.userUUID = uuid }
                    addToCache(result.data)
                    addToCurrent(result.data)
                    cont.resume(true)
                }
                is ErrorResult -> {
                    cont.resumeWithException(FlatException(result.error.code, result.error.message))
                }
            }
        }
    }

    fun onEvent(event: ClassRoomEvent) {
        viewModelScope.launch {
            _roomEvent.value = event
        }
    }

    fun onOperationAreaShown(areaId: Int) {
        onEvent(ClassRoomEvent.OperationAreaShown(areaId))
    }

    private fun addToCurrent(userMap: Map<String, RtcUser>) {
        val map = _usersMap.value.toMutableMap()
        userMap.forEach { (uuid, user) -> map[uuid] = user }
        _usersMap.value = map
    }

    private fun addToCache(userMap: Map<String, RtcUser>) {
        userMap.forEach { (uuid, user) -> _usersCacheMap[uuid] = user }
    }

    fun removeRtmMember(userUUID: String) {
        val map = _usersMap.value.toMutableMap()
        map.remove(userUUID)
        _usersMap.value = map
    }

    fun addRtmMember(userUUID: String) {
        if (_usersCacheMap.containsKey(userUUID)) {
            addToCurrent(mapOf(userUUID to _usersCacheMap[userUUID]!!))
            return
        }
        viewModelScope.launch {
            when (val result = roomRepository.getRoomUsers(roomUUID, listOf(userUUID))) {
                is Success -> {
                    result.data.forEach { (uuid, user) -> user.userUUID = uuid }
                    addToCache(result.data)
                    addToCurrent(result.data)
                }
                is ErrorResult -> {
                }
            }
        }
    }

    private fun intentValue(key: String): String {
        return savedStateHandle.get<String>(key)!!
    }

    fun setVideoShown(shown: Boolean) {
        _videoAreaShown.value = shown
    }

    // RTCCommand Handle
    private fun updateDeviceState(value: DeviceStateValue) {
        val user = _usersMap.value[value.userUUID]
        if (user != null) {
            val map = _usersMap.value.toMutableMap()
            map[value.userUUID] = user.copy(audioOpen = value.mic, videoOpen = value.camera)
            _usersMap.value = map
        }
    }

    private fun updateUserState(userUUID: String, state: RTMUserState) {
        val user = _usersMap.value[userUUID]
        if (user != null) {
            val map = _usersMap.value.toMutableMap()
            map[userUUID] = user.copy(
                audioOpen = state.mic,
                videoOpen = state.camera,
                name = state.name,
                isSpeak = state.isSpeak
            )
            _usersMap.value = map
        }
    }

    private fun updateChannelState(status: ChannelStatusValue) {
        val map = _usersMap.value.toMutableMap()
        status.uStates.forEach { (userId, s) ->
            map[userId]?.run {
                videoOpen = s.contains(RTMUserProp.Camera.flag, ignoreCase = true)
                audioOpen = s.contains(RTMUserProp.Mic.flag, ignoreCase = true)
                isSpeak = s.contains(RTMUserProp.IsSpeak.flag, ignoreCase = true)
                isRaiseHand = s.contains(RTMUserProp.IsRaiseHand.flag, ignoreCase = true)
            }
        }
        _usersMap.value = map
        _state.value = _state.value.copy(classMode = status.rMode, ban = status.ban, roomStatus = status.rStatus)
    }

    fun requestChannelStatus() {
        viewModelScope.launch {
            val user = _usersMap.value.values.firstOrNull { user ->
                user.userUUID != _state.value.currentUserUUID
            }
            user?.run {
                val roomUUID = roomUUID;
                val userInfo = appKVCenter.getUserInfo()!!
                val state = RTMUserState(
                    userInfo.name,
                    camera = _roomConfig.value.enableVideo,
                    mic = _roomConfig.value.enableAudio,
                    isSpeak = isRoomOwner(),
                )
                val value = RequestChannelStatusValue(roomUUID, listOf(userUUID), state)
                val event = RTMEvent.RequestChannelStatus(value)

                rtmApi.sendChannelCommand(event)
            }
        }
    }

    private fun isRoomOwner(): Boolean {
        return _state.value.ownerUUID == _state.value.currentUserUUID
    }

    fun onRTMEvent(event: RTMEvent, senderId: String) {
        when (event) {
            is RTMEvent.ChannelMessage -> {
                // TODO
            }
            is RTMEvent.ChannelStatus -> {
                updateChannelState(event.value)
            }
            is RTMEvent.RequestChannelStatus -> {
                updateUserState(event.value.roomUUID, event.value.user)
                if (event.value.userUUIDs.contains(currentUserUUID)) {
                    sendChannelStatus(senderId)
                }
            }
            is RTMEvent.DeviceState -> {
                updateDeviceState(event.value)
            }
            is RTMEvent.AcceptRaiseHand -> {
                if (senderId == _state.value.ownerUUID) {
                    val user = _usersMap.value[event.value.userUUID]
                    user?.run {
                        val map = _usersMap.value.toMutableMap()
                        map[senderId] = copy(isSpeak = event.value.accept)
                        _usersMap.value = map
                    }
                }
            }
            is RTMEvent.BanText -> _state.value = _state.value.copy(ban = event.v)
            is RTMEvent.CancelAllHandRaising -> {
                if (senderId == _state.value.ownerUUID) {
                    val map = _usersMap.value.toMutableMap()
                    map.forEach { (uuid, u) -> map[uuid] = u.copy(isRaiseHand = false) }
                    _usersMap.value = map
                }
            }
            is RTMEvent.ClassMode -> {
                if (senderId == _state.value.ownerUUID) {
                    _state.value = state.value.copy(classMode = event.classModeType)
                }
            }
            is RTMEvent.Notice -> {

            }
            is RTMEvent.RaiseHand -> {
                val user = _usersMap.value[senderId]
                user?.run {
                    val map = _usersMap.value.toMutableMap()
                    map[senderId] = copy(isRaiseHand = event.v)
                    _usersMap.value = map
                }
            }
            is RTMEvent.RoomStatus -> {
                if (senderId == _state.value.ownerUUID) {
                    _state.value = _state.value.copy(roomStatus = event.roomStatus)
                }
            }
            is RTMEvent.Speak -> {
                val user = _usersMap.value[senderId]
                user?.run {
                    val map = _usersMap.value.toMutableMap()
                    map[senderId] = copy(isSpeak = event.v)
                    _usersMap.value = map
                }
            }
        }
    }

    private fun sendChannelStatus(senderId: String) {
        viewModelScope.launch {
            val uStates = HashMap<String, String>()
            usersMap.value.values.forEach {
                uStates[it.userUUID] = StringBuilder().apply {
                    if (it.isSpeak) append(RTMUserProp.IsSpeak.flag)
                    if (it.isRaiseHand) append(RTMUserProp.IsRaiseHand.flag)
                    if (it.videoOpen) append(RTMUserProp.Camera.flag)
                    if (it.audioOpen) append(RTMUserProp.Mic.flag)
                }.toString()
            }
            var channelState = RTMEvent.ChannelStatus(
                ChannelStatusValue(
                    ban = _state.value.ban,
                    rStatus = _state.value.roomStatus,
                    rMode = _state.value.classMode,
                    uStates = uStates
                )
            )
            rtmApi.sendPeerCommand(channelState, senderId)
        }
    }
}

data class ClassRoomState(
    // 房间的 uuid
    val roomUUID: String = "",
    // 房间类型
    val roomType: RoomType = RoomType.SmallClass,
    // 房间状态
    val roomStatus: RoomStatus = RoomStatus.Idle,
    // 房间所有者
    val ownerUUID: String = "",
    // 当前用户
    val currentUserUUID: String = "",
    // 房间所有者的名称
    val ownerName: String? = null,
    // 房间标题
    val title: String = "",
    // 房间开始时间
    val beginTime: Long = 0L,
    // 结束时间
    val endTime: Long = 0L,
    // 禁用
    val ban: Boolean = true,
    // 交互模式
    val classMode: ClassModeType = ClassModeType.Lecture,
)

sealed class ClassRoomEvent {
    companion object {
        const val AREA_ID_APPLIANCE = 1
        const val AREA_ID_PAINT = 2
        const val AREA_ID_SETTING = 3
        const val AREA_ID_MESSAGE = 4
        const val AREA_ID_CLOUD_STORAGE = 4
    }

    object EnterRoom : ClassRoomEvent()
    object RtmChannelJoined : ClassRoomEvent()
    data class ChangeVideoDisplay(val id: Int) : ClassRoomEvent()
    data class OperationAreaShown(val areaId: Int) : ClassRoomEvent()
}