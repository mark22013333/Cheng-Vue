import { ElMessage } from 'element-plus'
import { getOAuthAuthorizeUrl } from '@/api/shop/auth'

/**
 * 社群登入共用邏輯（LINE / Google OAuth）
 * @param {import('vue').Ref<string>} redirect - 登入後重導路徑
 */
export function useSocialLogin(redirect) {
  async function handleOAuthLogin(provider) {
    try {
      const redirectUri = `${window.location.origin}/oauth/callback`

      sessionStorage.setItem('oauth_provider', provider)
      sessionStorage.setItem('oauth_redirect', redirect.value)
      sessionStorage.setItem('oauth_redirect_uri', redirectUri)

      const res = await getOAuthAuthorizeUrl(provider, redirectUri)
      const authorizeUrl = res.data || res.authorizeUrl || res

      if (!authorizeUrl) {
        ElMessage.error('無法取得授權連結，請稍後再試')
        return
      }

      window.location.href = authorizeUrl
    } catch (error) {
      ElMessage.error(error?.msg || `取得 ${provider} 登入連結失敗`)
    }
  }

  function handleGoogleLogin() {
    return handleOAuthLogin('GOOGLE')
  }

  function handleLineLogin() {
    return handleOAuthLogin('LINE')
  }

  return { handleGoogleLogin, handleLineLogin }
}
