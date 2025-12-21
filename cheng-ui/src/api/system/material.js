import request from '@/utils/request'

export function listMaterialAudio(query) {
  return request({
    url: '/system/material/audio/list',
    method: 'get',
    params: query
  })
}

export function listMaterialVideo(query) {
  return request({
    url: '/system/material/video/list',
    method: 'get',
    params: query
  })
}

export function listMaterialImage(query) {
  return request({
    url: '/system/material/image/list',
    method: 'get',
    params: query
  })
}

export function getMaterialAsset(assetId) {
  return request({
    url: '/system/material/' + assetId,
    method: 'get'
  })
}

export function delMaterialAudio(assetIds) {
  return request({
    url: '/system/material/audio/' + assetIds,
    method: 'delete'
  })
}

export function delMaterialVideo(assetIds) {
  return request({
    url: '/system/material/video/' + assetIds,
    method: 'delete'
  })
}

export function delMaterialImage(assetIds) {
  return request({
    url: '/system/material/image/' + assetIds,
    method: 'delete'
  })
}

export function existsMaterialAudio(originalName) {
  return request({
    url: '/system/material/audio/exists',
    method: 'get',
    params: { originalName }
  })
}

export function existsMaterialVideo(originalName) {
  return request({
    url: '/system/material/video/exists',
    method: 'get',
    params: { originalName }
  })
}

export function existsMaterialImage(originalName) {
  return request({
    url: '/system/material/image/exists',
    method: 'get',
    params: { originalName }
  })
}

export function uploadMaterialAudio(file, durationMs, overwrite, onUploadProgress) {
  // 關鍵流程：使用 FormData 走 multipart 上傳，方便後續接入錄音檔 Blob
  const formData = new FormData()
  formData.append('file', file)
  if (durationMs !== undefined && durationMs !== null && durationMs !== '') {
    formData.append('durationMs', durationMs)
  }
  formData.append('overwrite', overwrite ? true : false)
  return request({
    url: '/system/material/audio/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

export function uploadMaterialVideo(file, durationMs, overwrite, onUploadProgress) {
  // 關鍵流程：使用 FormData 走 multipart 上傳，方便後續接入手機拍攝的檔案
  const formData = new FormData()
  formData.append('file', file)
  if (durationMs !== undefined && durationMs !== null && durationMs !== '') {
    formData.append('durationMs', durationMs)
  }
  formData.append('overwrite', overwrite ? true : false)
  return request({
    url: '/system/material/video/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

export function uploadMaterialImage(file, overwrite, onUploadProgress) {
  // 關鍵流程：使用 FormData 走 multipart 上傳
  const formData = new FormData()
  formData.append('file', file)
  formData.append('overwrite', overwrite ? true : false)
  return request({
    url: '/system/material/image/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}
