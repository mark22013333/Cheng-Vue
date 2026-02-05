/**
 * 圖片路徑處理工具
 * 統一處理前端圖片 URL 的產生，確保在各種環境下都能正確顯示圖片
 *
 * 重要：圖片路徑不需要加 API 前綴（如 /dev-api 或 /prod-api）
 * 因為圖片由 Nginx 的 location /profile/ 直接處理，不經過 API 代理
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

  // 如果路徑已經是以 /profile 開頭的完整路徑，直接返回
  // Nginx 會透過 location /profile/ 處理這些請求
  if (imagePath.startsWith('/profile')) {
    return imagePath;
  }

  // 處理本機檔案系統的絕對路徑
  // 例如: /Users/cheng/uploadPath/book-covers/xxx.jpg 或 /opt/cool-apps/uploadFile/book-covers/xxx.jpg
  if (imagePath.includes('/book-covers/') || imagePath.includes('/avatar/') || imagePath.includes('/upload/')) {
    // 找到 profile 之後的路徑
    const profileIndex = imagePath.indexOf('/profile/');
    if (profileIndex !== -1) {
      return imagePath.substring(profileIndex);
    }

    // 如果找不到 /profile/，嘗試從常見的目錄開始擷取
    const parts = imagePath.split('/');
    const knownDirs = ['book-covers', 'avatar', 'upload'];
    for (const dir of knownDirs) {
      const dirIndex = parts.indexOf(dir);
      if (dirIndex !== -1) {
        // 從該目錄開始重建路徑
        const subPath = parts.slice(dirIndex).join('/');
        return `/profile/${subPath}`;
      }
    }
  }

  // 其他情況，假設是相對路徑，加上 /profile/ 前綴
  const cleanPath = imagePath.startsWith('/') ? imagePath.substring(1) : imagePath;
  return `/profile/${cleanPath}`;
}

/**
 * 取得 API 基礎 URL（防禦性處理 undefined）
 * @returns {string} API 基礎 URL
 */
export function getBaseApiUrl() {
  return import.meta.env.VITE_APP_BASE_API || '';
}

export function getStaticBaseUrl() {
  const staticUrl = import.meta.env.VITE_APP_STATIC_URL || '';
  if (staticUrl) {
    return staticUrl.endsWith('/') ? staticUrl.slice(0, -1) : staticUrl;
  }
  if (typeof window !== 'undefined' && window.location && window.location.origin) {
    return window.location.origin;
  }
  return '';
}

export function getProfileApiUrl(relativePath) {
  if (!relativePath) return '';
  const clean = String(relativePath).replace(/^\/+/, '');
  return '/profile/' + clean;
}

export function getProfileStaticUrl(relativePath) {
  if (!relativePath) return '';
  const base = getStaticBaseUrl();
  const clean = String(relativePath).replace(/^\/+/, '');
  const path = '/profile/' + clean;
  return base ? base + path : path;
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
