/**
 * 音檔格式轉換工具
 * 使用 Web Audio API + lamejs 將 webm 轉換為 mp3
 * 
 * LINE Messaging API 只支援 mp3 和 m4a 格式
 * 瀏覽器錄音（MediaRecorder）預設產生 webm 格式
 */

import lamejs from '@breezystack/lamejs'

/**
 * 將 AudioBuffer 轉換為 MP3 Blob
 * @param {AudioBuffer} audioBuffer - Web Audio API 的 AudioBuffer
 * @param {number} bitRate - MP3 位元率 (預設 128 kbps)
 * @returns {Blob} MP3 格式的 Blob
 */
function audioBufferToMp3(audioBuffer, bitRate = 128) {
  const channels = audioBuffer.numberOfChannels
  const sampleRate = audioBuffer.sampleRate
  const samples = audioBuffer.length

  // 取得音訊資料（轉換為 Int16）
  const leftChannel = audioBuffer.getChannelData(0)
  const rightChannel = channels > 1 ? audioBuffer.getChannelData(1) : leftChannel

  // 轉換 Float32 到 Int16
  const leftInt16 = floatTo16BitPCM(leftChannel)
  const rightInt16 = floatTo16BitPCM(rightChannel)

  // 建立 MP3 編碼器
  const mp3encoder = new lamejs.Mp3Encoder(channels, sampleRate, bitRate)

  const mp3Data = []
  const blockSize = 1152 // lamejs 建議的區塊大小

  for (let i = 0; i < samples; i += blockSize) {
    const leftChunk = leftInt16.subarray(i, i + blockSize)
    const rightChunk = rightInt16.subarray(i, i + blockSize)

    let mp3buf
    if (channels === 1) {
      mp3buf = mp3encoder.encodeBuffer(leftChunk)
    } else {
      mp3buf = mp3encoder.encodeBuffer(leftChunk, rightChunk)
    }

    if (mp3buf.length > 0) {
      mp3Data.push(mp3buf)
    }
  }

  // 完成編碼
  const mp3End = mp3encoder.flush()
  if (mp3End.length > 0) {
    mp3Data.push(mp3End)
  }

  return new Blob(mp3Data, { type: 'audio/mp3' })
}

/**
 * Float32Array 轉換為 Int16Array
 */
function floatTo16BitPCM(float32Array) {
  const int16Array = new Int16Array(float32Array.length)
  for (let i = 0; i < float32Array.length; i++) {
    const s = Math.max(-1, Math.min(1, float32Array[i]))
    int16Array[i] = s < 0 ? s * 0x8000 : s * 0x7FFF
  }
  return int16Array
}

/**
 * 將 Blob 轉換為 ArrayBuffer
 */
function blobToArrayBuffer(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsArrayBuffer(blob)
  })
}

/**
 * 將 webm/ogg 音檔 Blob 轉換為 mp3
 * @param {Blob} audioBlob - 原始音檔 Blob (webm/ogg 格式)
 * @param {Function} onProgress - 進度回調 (可選)
 * @returns {Promise<Blob>} MP3 格式的 Blob
 */
export async function convertToMp3(audioBlob, onProgress) {
  try {
    // 1. 建立 AudioContext 解碼音訊
    if (onProgress) onProgress(20, '解碼音訊...')
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const arrayBuffer = await blobToArrayBuffer(audioBlob)

    // 3. 解碼音訊資料
    if (onProgress) onProgress(40, '處理音訊資料...')
    const audioBuffer = await audioContext.decodeAudioData(arrayBuffer)

    // 4. 編碼為 MP3
    if (onProgress) onProgress(60, '轉換為 MP3...')
    const mp3Blob = audioBufferToMp3(audioBuffer)

    // 5. 關閉 AudioContext
    await audioContext.close()

    if (onProgress) onProgress(100, '轉換完成')
    return mp3Blob

  } catch (error) {
    console.error('音檔轉換失敗:', error)
    throw new Error('音檔轉換失敗: ' + (error.message || '未知錯誤'))
  }
}

/**
 * 判斷是否需要轉換格式
 * @param {string} mimeType - MIME 類型
 * @returns {boolean}
 */
export function needsConversion(mimeType) {
  if (!mimeType) return false
  const type = mimeType.toLowerCase()
  // webm, opus, ogg 等瀏覽器常見格式都需要轉換
  return type.includes('webm') || type.includes('opus') || type.includes('ogg')
}

/**
 * 判斷是否為 LINE 支援的格式
 * @param {string} mimeType - MIME 類型
 * @returns {boolean}
 */
export function isLineSupportedFormat(mimeType) {
  if (!mimeType) return false
  const type = mimeType.toLowerCase()
  return type.includes('mp3') || type.includes('mpeg') || type.includes('m4a') || type.includes('mp4')
}
