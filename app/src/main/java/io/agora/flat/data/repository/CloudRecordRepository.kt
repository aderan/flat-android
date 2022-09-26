package io.agora.flat.data.repository

import io.agora.flat.data.Result
import io.agora.flat.data.model.*
import io.agora.flat.data.toResult
import io.agora.flat.http.api.CloudRecordService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudRecordRepository @Inject constructor(
    private val cloudRecordService: CloudRecordService,
) {
    suspend fun acquireRecord(roomUUID: String, expiredHour: Int = 24): Result<RecordAcquireRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.acquireRecord(
                RecordAcquireReq(
                    roomUUID, RecordAcquireReqData(RecordAcquireReqDataClientRequest(expiredHour, 0))
                )
            ).toResult()
        }
    }

    suspend fun startRecordWithAgora(
        roomUUID: String,
        resourceId: String,
    ): Result<RecordStartRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.startRecordWithAgora(
                RecordStartReq(
                    roomUUID,
                    AgoraRecordParams(resourceid = resourceId, mode = AgoraRecordMode.Individual),
                    AgoraRecordStartedData(
                        ClientRequest(
                            RecordingConfig(
                                subscribeUidGroup = 0,
                                maxIdleTime = 30,
                                channelType = 0,
                                streamMode = "standard",
                                videoStreamType = 1,
                            )
                        )
                    )
                )
            ).toResult()
        }
    }

    suspend fun queryRecordWithAgora(
        roomUUID: String,
        resourceId: String,
    ): Result<RecordQueryRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.queryRecordWithAgora(
                RecordReq(roomUUID, AgoraRecordParams(resourceId, AgoraRecordMode.Individual))
            ).toResult()
        }
    }

    suspend fun updateRecordLayoutWithAgora(
        roomUUID: String,
        resourceId: String,
        clientRequest: UpdateLayoutClientRequest,
    ): Result<RecordQueryRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.updateRecordLayoutWithAgora(
                RecordUpdateLayoutReq(
                    roomUUID,
                    AgoraRecordParams(resourceId, AgoraRecordMode.Mix),
                    AgoraRecordUpdateLayoutData(clientRequest)
                )
            ).toResult()
        }
    }

    suspend fun stopRecordWithAgora(
        roomUUID: String,
        resourceId: String,
        sid: String,
    ): Result<RecordStopRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.stopRecordWithAgora(
                RecordReq(
                    roomUUID,
                    AgoraRecordParams(resourceid = resourceId, mode = AgoraRecordMode.Individual, sid = sid),
                )
            ).toResult()
        }
    }

    suspend fun getRecordInfo(roomUUID: String): Result<RecordInfo> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.getRecordInfo(PureRoomReq(roomUUID)).toResult()
        }
    }
}