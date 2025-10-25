package com.aiteacher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.aiteacher.ai.agent.SecretaryAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

/**
 * Agent后台服务
 * 维护Agent实例的全生命周期
 */
class AgentService : Service() {

    private val secretaryAgent: SecretaryAgent by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "AgentService"
        const val ACTION_START_AGENTS = "com.aiteacher.action.START_AGENTS"
        const val ACTION_STOP_AGENTS = "com.aiteacher.action.STOP_AGENTS"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AgentService created")
        initializeAgents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_AGENTS -> {
                startAgents()
            }
            ACTION_STOP_AGENTS -> {
                stopAgents()
                stopSelf()
            }
        }
        return START_STICKY // 服务被杀死后自动重启
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AgentService destroyed")
        serviceScope.cancel()
    }

    private fun initializeAgents() {
        try {
            // 初始化所有Agent
            Log.d(TAG, "Initializing agents...")
            
            // Agent通过Hilt注入，无需手动初始化
            Log.d(TAG, "Agents initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize agents", e)
        }
    }

    private fun startAgents() {
        Log.d(TAG, "Starting agents...")
        // 启动Agent的业务逻辑
    }

    private fun stopAgents() {
        Log.d(TAG, "Stopping agents...")
        // 停止Agent的业务逻辑
    }

    /**
     * 获取SecretaryAgent实例
     */
    fun getSecretaryAgentInstance(): SecretaryAgent = secretaryAgent
}
