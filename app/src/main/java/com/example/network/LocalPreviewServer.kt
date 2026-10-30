package com.example.network

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lightweight local HTTP preview server running on 127.0.0.1 with an ephemeral,
 * random port assigned by the OS (e.g. 63823). Avoids collisions with standard 8080/3000 ports.
 * Shuts down immediately when closed or dismissed.
 */
class LocalPreviewServer {
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var executor: ExecutorService? = null

    var port: Int = 0
        private set

    @Synchronized
    fun start(htmlContentProvider: () -> String): Int {
        stop()
        try {
            // Port 0 tells the operating system kernel to bind an available random ephemeral port (49152-65535)
            val socket = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
            serverSocket = socket
            port = socket.localPort
            isRunning.set(true)
            val pool = Executors.newCachedThreadPool()
            executor = pool

            pool.execute {
                while (isRunning.get() && !socket.isClosed) {
                    try {
                        val client = socket.accept()
                        pool.execute {
                            handleClient(client, htmlContentProvider)
                        }
                    } catch (e: Exception) {
                        if (!isRunning.get()) break
                    }
                }
            }
            Log.i("LocalPreviewServer", "Local preview server started on random port: $port")
            return port
        } catch (e: Exception) {
            Log.e("LocalPreviewServer", "Failed to start local preview server", e)
            return 0
        }
    }

    private fun handleClient(socket: Socket, htmlContentProvider: () -> String) {
        try {
            socket.use { s ->
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val requestLine = reader.readLine() ?: return
                // Consume remaining headers
                var headerLine = reader.readLine()
                while (!headerLine.isNullOrEmpty()) {
                    headerLine = reader.readLine()
                }

                val currentHtml = htmlContentProvider()
                val bodyBytes = currentHtml.toByteArray(Charsets.UTF_8)

                val out: OutputStream = s.getOutputStream()
                val responseHeaders = buildString {
                    append("HTTP/1.1 200 OK\r\n")
                    append("Content-Type: text/html; charset=utf-8\r\n")
                    append("Content-Length: ${bodyBytes.size}\r\n")
                    append("Connection: close\r\n")
                    append("Cache-Control: no-cache, no-store, must-revalidate\r\n")
                    append("Pragma: no-cache\r\n")
                    append("Expires: 0\r\n")
                    append("Access-Control-Allow-Origin: *\r\n\r\n")
                }
                out.write(responseHeaders.toByteArray(Charsets.UTF_8))
                out.write(bodyBytes)
                out.flush()
            }
        } catch (_: Exception) {
            // Normal client socket teardown
        }
    }

    @Synchronized
    fun stop() {
        if (isRunning.getAndSet(false)) {
            val oldPort = port
            try {
                serverSocket?.close()
            } catch (_: Exception) {}
            serverSocket = null
            try {
                executor?.shutdownNow()
            } catch (_: Exception) {}
            executor = null
            port = 0
            Log.i("LocalPreviewServer", "Local preview server on port $oldPort closed successfully.")
        }
    }
}
