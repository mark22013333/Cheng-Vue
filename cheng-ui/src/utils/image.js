/**
 * 圖片路徑處理工具
 * 統一處理前端圖片 URL 的產生，確保在各種環境下都能正確顯示圖片
 */

/**
 * 取得完整的圖片 URL
 * @param {string} imagePath - 後端返回的圖片路徑
 * @returns {string} 完整的圖片 URL
 */
export function getImageUrl(imagePath) {
  if (!imagePath) return '';

  // 如果是完整的 HTTP/HTTPS URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath;
  }

  // 取得 API 基礎路徑，防止 undefined
  const baseApi = process.env.VUE_APP_BASE_API || '';

  // 如果路徑已經是以 /profile 開頭的完整路徑
  if (imagePath.startsWith('/profile')) {
    // 如果 baseApi 為空，直接返回路徑（Nginx 會處理）
    // 如果 baseApi 有值且不等於路徑本身，加上前綴
    return baseApi && !imagePath.startsWith(baseApi) ? baseApi + imagePath : imagePath;
  }

  // 處理本機檔案系統的絕對路徑
  // 例如: /Users/cheng/uploadPath/book-covers/xxx.jpg 或 /opt/cool-apps/uploadFile/book-covers/xxx.jpg
  if (imagePath.includes('/book-covers/') || imagePath.includes('/avatar/') || imagePath.includes('/upload/')) {
    // 擷取相對路徑部分
    let relativePath = imagePath;

    // 找到 profile 之後的路徑
    const profileIndex = imagePath.indexOf('/profile/');
    if (profileIndex !== -1) {
      relativePath = imagePath.substring(profileIndex);
      return baseApi ? baseApi + relativePath : relativePath;
    }

    // 如果找不到 /profile/，嘗試從常見的目錄開始擷取
    const parts = imagePath.split('/');
    const knownDirs = ['book-covers', 'avatar', 'upload'];
    for (const dir of knownDirs) {
      const dirIndex = parts.indexOf(dir);
      if (dirIndex !== -1) {
        // 從該目錄開始重建路徑
        const subPath = parts.slice(dirIndex).join('/');
        return baseApi ? `${baseApi}/profile/${subPath}` : `/profile/${subPath}`;
      }
    }
  }

  // 其他情況，假設是相對路徑，加上 /profile/ 前綴
  const fullPath = imagePath.startsWith('/') ? imagePath : '/' + imagePath;
  return baseApi ? `${baseApi}/profile${fullPath}` : `/profile${fullPath}`;
}

/**
 * 取得 API 基礎 URL（防禦性處理 undefined）
 * @returns {string} API 基礎 URL
 */
export function getBaseApiUrl() {
  return process.env.VUE_APP_BASE_API || '';
}

/**
 * 判斷路徑是否為外部 URL
 * @param {string} path - 路徑
 * @returns {boolean} 是否為外部 URL
 */
export function isExternalUrl(path) {
  if (!path) return false;
  return path.startsWith('http://') || path.startsWith('https://');
}
